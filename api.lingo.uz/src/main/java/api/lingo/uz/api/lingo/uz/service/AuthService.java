package api.lingo.uz.api.lingo.uz.service;


import api.lingo.uz.api.lingo.uz.dto.AppResponse;
import api.lingo.uz.api.lingo.uz.dto.auth.AuthDTO;
import api.lingo.uz.api.lingo.uz.dto.auth.RegistrationDTO;
import api.lingo.uz.api.lingo.uz.dto.auth.ResetPasswordConfirmDTO;
import api.lingo.uz.api.lingo.uz.dto.auth.ResetPasswordDTO;
import api.lingo.uz.api.lingo.uz.dto.profile.ProfileResponseDTO;
import api.lingo.uz.api.lingo.uz.dto.sms.SmsResendDTO;
import api.lingo.uz.api.lingo.uz.dto.sms.SmsVerificationDTO;
import api.lingo.uz.api.lingo.uz.entity.ProfileEntity;
import api.lingo.uz.api.lingo.uz.enums.AppLanguage;
import api.lingo.uz.api.lingo.uz.enums.GeneralStatus;
import api.lingo.uz.api.lingo.uz.enums.ProfileRole;
import api.lingo.uz.api.lingo.uz.exps.AppBadException;
import api.lingo.uz.api.lingo.uz.repository.ProfileRepository;
import api.lingo.uz.api.lingo.uz.repository.ProfileRoleRepository;
import api.lingo.uz.api.lingo.uz.util.JwtUtil;
import api.lingo.uz.api.lingo.uz.util.ValidityUtil;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class AuthService {
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private ProfileRoleService profileRoleService;
    @Autowired
    private EmailSendingService emailSendingService;
    @Autowired
    private SmsSendService smsSendService;
    @Autowired
    private ProfileService profileService;
    @Autowired
    private AttachService attachService;
    @Autowired
    private ProfileRoleRepository profileRoleRepository;
    @Autowired
    private ResourceBundleMessageService resourceBundleMessageService;
    @Autowired
    private SmsHistoryService smsHistoryService;
    @Autowired
    private EmailHistoryService emailHistoryService;


    public AppResponse<String> registration(RegistrationDTO dto, AppLanguage lang) {
        Optional<ProfileEntity> optional = profileRepository.findByUsernameAndVisibleTrue(dto.getUsername());
        if (optional.isPresent()) {
            ProfileEntity profile = optional.get();
            if (profile.getStatus().equals(GeneralStatus.IN_REGISTRATION)) {
                profileRoleService.deleteRoles(profile.getId());
                profileRepository.delete(profile);
            } else {
                log.warn("Profile {} already exists", profile.getId());
                throw new AppBadException(resourceBundleMessageService.getMessage("email.phone.exists", lang));
            }
        }

        ProfileEntity profileEntity = new ProfileEntity();
        profileEntity.setName(dto.getName());
        profileEntity.setUsername(dto.getUsername());
        profileEntity.setPassword(passwordEncoder.encode(dto.getPassword()));
        profileEntity.setStatus(GeneralStatus.IN_REGISTRATION);
        profileRepository.save(profileEntity);
        profileRoleService.create(profileEntity.getId(), ProfileRole.ROLE_USER);
        // check if email valid
        if (ValidityUtil.isValidEmail(dto.getUsername())) {
            emailSendingService.sendRegistrationEmail(dto.getUsername(), profileEntity.getId(), lang.name());
            return new AppResponse<>(resourceBundleMessageService.getMessage("email.confirm.sent", lang));
        }
        //check if phone valid
        if (ValidityUtil.isValidPhone(dto.getUsername())) {
            smsSendService.sendRegistrationSms(dto.getUsername(), lang);
            return new AppResponse<>(resourceBundleMessageService.getMessage("phone.confirm.sent", lang));
        }
        throw new AppBadException(resourceBundleMessageService.getMessage("email.phone.invalid", lang));
    }

    public ProfileResponseDTO regVerificationEmail(String token, AppLanguage lang) {
        try {
            String profileId = JwtUtil.decodeRegVerToken(token);
            ProfileEntity profileEntity = profileService.findProfileById(profileId, lang);
            if (profileEntity.getStatus().equals(GeneralStatus.IN_REGISTRATION)) {
                profileRepository.updateStatus(profileId, GeneralStatus.ACTIVE);
                return getLoginResponse(profileEntity);
            }
        } catch (JwtException e) {
            throw new AppBadException(resourceBundleMessageService.getMessage("token.invalid.expired", lang));
        }
        log.info("Email verification failed {}", token);
        throw new AppBadException(resourceBundleMessageService.getMessage("verification.failed", lang));
    }

    public ProfileResponseDTO regVerificationSms(SmsVerificationDTO dto, AppLanguage lang) {
        //check if exits
        Optional<ProfileEntity> optional = profileRepository.findByUsernameAndVisibleTrue(dto.getPhone());
        if (optional.isEmpty()) {
            log.info("Profile not found {}", dto.getPhone());
            throw new AppBadException(resourceBundleMessageService.getMessage("profile.not.found", lang));
        }
        // check for  status
        ProfileEntity profileEntity = optional.get();
        if (!profileEntity.getStatus().equals(GeneralStatus.IN_REGISTRATION)) {
            log.info("Verification failed: {}", dto.getPhone());
            throw new AppBadException(resourceBundleMessageService.getMessage("verification.failed", lang));
        }

        // check if code matches
        smsHistoryService.check(dto.getPhone(), dto.getCode(), lang);
        //update
        profileRepository.updateStatus(profileEntity.getId(), GeneralStatus.ACTIVE);

        return getLoginResponse(profileEntity);

    }

    public ProfileResponseDTO login(AuthDTO authDTO, AppLanguage lang) {
        Optional<ProfileEntity> optional = profileRepository.findByUsernameAndVisibleTrue(authDTO.getUsername());
        if (optional.isEmpty()) {
            log.info("Login failed username: {}", authDTO.getUsername());
            throw new AppBadException(resourceBundleMessageService.getMessage("wrong.password.username", lang));
        }
        ProfileEntity profileEntity = optional.get();
        if (!passwordEncoder.matches(authDTO.getPassword(), profileEntity.getPassword())) {
            log.info("Login failed username: {}", authDTO.getUsername());
            throw new AppBadException(resourceBundleMessageService.getMessage("wrong.password.username", lang));
        }
        if (!profileEntity.getStatus().equals(GeneralStatus.ACTIVE)) {
            log.warn("Wrong status username {}", authDTO.getUsername());
            throw new AppBadException(resourceBundleMessageService.getMessage("wrong.status", lang));
        }
        return getLoginResponse(profileEntity);

    }

    public AppResponse<String> resetPassword(ResetPasswordDTO dto, AppLanguage lang) {
        //check if exits
        Optional<ProfileEntity> optional = profileRepository.findByUsernameAndVisibleTrue(dto.getUsername());
        if (optional.isEmpty()) {
            log.info("Profile not found username {}", dto.getUsername());
            throw new AppBadException(resourceBundleMessageService.getMessage("profile.not.found", lang));
        }
        // check for  status
        ProfileEntity profileEntity = optional.get();
        if (!profileEntity.getStatus().equals(GeneralStatus.ACTIVE)) {
            log.info("Wrong status {}", dto.getUsername());
            throw new AppBadException(resourceBundleMessageService.getMessage("wrong.status", lang));
        }
        //check if email valid
        if (ValidityUtil.isValidEmail(dto.getUsername())) {
            emailSendingService.sendPasswordResetEmail(dto.getUsername(), lang);

        }
        //check if phone valid
        if (ValidityUtil.isValidPhone(dto.getUsername())) {
            smsSendService.sendPasswordResetSms(dto.getUsername(), lang);

        }
        return new AppResponse<>(resourceBundleMessageService.getMessage("reset.password.username.sent", lang));

    }

    public AppResponse<String> resetPasswordConfirm(ResetPasswordConfirmDTO dto, AppLanguage lang) {
        //check if exits
        Optional<ProfileEntity> optional = profileRepository.findByUsernameAndVisibleTrue(dto.getUsername());
        if (optional.isEmpty()) {
            log.info("Profile not found username {}", dto.getUsername());
            throw new AppBadException(resourceBundleMessageService.getMessage("profile.not.found", lang));
        }
        ProfileEntity profileEntity = optional.get();
        if (!profileEntity.getStatus().equals(GeneralStatus.ACTIVE)) {
            log.info("Wrong status {}", dto.getUsername());
            throw new AppBadException(resourceBundleMessageService.getMessage("wrong.status", lang));
        }
        // check if email valid
        if (ValidityUtil.isValidEmail(dto.getUsername())) {
            emailHistoryService.check(dto.getUsername(), dto.getConfirmCode(), lang);
        }
        //check if phone valid
        if (ValidityUtil.isValidPhone(dto.getUsername())) {
            smsHistoryService.check(dto.getUsername(), dto.getConfirmCode(), lang);
        }
        //update password
        profileRepository.updatePassword(profileEntity.getId(), passwordEncoder.encode(dto.getPassword()));
        //response
        return new AppResponse<>(resourceBundleMessageService.getMessage("reset.password.success", lang));

    }

    public AppResponse<String> regVerificationSmsResend(SmsResendDTO dto, AppLanguage lang) {
        //check if exits
        Optional<ProfileEntity> optional = profileRepository.findByUsernameAndVisibleTrue(dto.getPhone());
        if (optional.isEmpty()) {
            log.info("Profile not found username {}", dto.getPhone());
            throw new AppBadException(resourceBundleMessageService.getMessage("profile.not.found", lang));
        }
        // check for  status
        ProfileEntity profileEntity = optional.get();
        if (!profileEntity.getStatus().equals(GeneralStatus.IN_REGISTRATION)) {
            log.info("Verification failed", dto.getPhone());
            throw new AppBadException(resourceBundleMessageService.getMessage("verification.failed", lang));
        }
        //check if phone valid
        smsSendService.sendRegistrationSms(dto.getPhone(), lang);
        return new AppResponse<>(resourceBundleMessageService.getMessage("sms.resend", lang));

    }

    private ProfileResponseDTO getLoginResponse(ProfileEntity profileEntity) {
        ProfileResponseDTO profileResponseDTO = new ProfileResponseDTO();
        profileResponseDTO.setName(profileEntity.getName());
        profileResponseDTO.setUsername(profileEntity.getUsername());
        profileResponseDTO.setRoleList(profileRoleRepository.getAllRoles(profileEntity.getId()));
        profileResponseDTO.setAttachDTO(attachService.attachDTO(profileEntity.getPhotoId()));

        profileResponseDTO.setJwt(JwtUtil.encode(profileEntity.getId(), profileEntity.getUsername(), profileResponseDTO.getRoleList()));
        return profileResponseDTO;
    }
}
