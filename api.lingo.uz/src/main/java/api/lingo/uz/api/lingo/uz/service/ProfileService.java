package api.lingo.uz.api.lingo.uz.service;


import api.lingo.uz.api.lingo.uz.dto.AppResponse;
import api.lingo.uz.api.lingo.uz.dto.CodeConfirmDTO;
import api.lingo.uz.api.lingo.uz.dto.ProfilePhotoUpdateDTO;
import api.lingo.uz.api.lingo.uz.dto.profile.*;
import api.lingo.uz.api.lingo.uz.entity.ProfileEntity;
import api.lingo.uz.api.lingo.uz.entity.ProfileRoleEntity;
import api.lingo.uz.api.lingo.uz.enums.AppLanguage;
import api.lingo.uz.api.lingo.uz.enums.ChatStatus;
import api.lingo.uz.api.lingo.uz.enums.ProfileRole;
import api.lingo.uz.api.lingo.uz.exps.AppBadException;
import api.lingo.uz.api.lingo.uz.mapper.ProfileDetailMapper;
import api.lingo.uz.api.lingo.uz.repository.ProfileRepository;
import api.lingo.uz.api.lingo.uz.repository.ProfileRoleRepository;
import api.lingo.uz.api.lingo.uz.util.JwtUtil;
import api.lingo.uz.api.lingo.uz.util.SpringSecurityUtil;
import api.lingo.uz.api.lingo.uz.util.ValidityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Service
@Slf4j
public class ProfileService {
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private ResourceBundleMessageService resourceBundleMessageService;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private EmailSendingService emailSendingService;
    @Autowired
    private SmsSendService smsSendService;
    @Autowired
    private SmsHistoryService smsHistoryService;
    @Autowired
    private EmailHistoryService emailHistoryService;
    @Autowired
    private ProfileRoleRepository profileRoleRepository;
    @Autowired
    private AttachService attachService;

    public ProfileEntity findProfileById(String id, AppLanguage lang) {
        log.error("No profile found with id: {}", id);
        return profileRepository.findByIdAndVisibleTrue(id).orElseThrow(() -> new AppBadException(resourceBundleMessageService.getMessage("profile.not.found", lang)));

    }

    public AppResponse<String> updateDetail(ProfileDetailUpdateDTO profile, AppLanguage lang) {
        String userId = SpringSecurityUtil.getCurrentUserId();
        profileRepository.updateProfileName(profile.getName(), userId);
        return new AppResponse<>(resourceBundleMessageService.getMessage("profile.name.updated", lang));
    }

    public AppResponse<String> updatePassword(ProfileUpdatePasswordDTO profileDTO, AppLanguage lang) {
        String userId = SpringSecurityUtil.getCurrentUserId();
        ProfileEntity profileEntity = findProfileById(userId, lang);
        if (!bCryptPasswordEncoder.matches(profileDTO.getCurrentPassword(), profileEntity.getPassword())) {
            log.warn("Password mismatch: userId:{}", userId);
            throw new AppBadException(resourceBundleMessageService.getMessage("password.not.match", lang));
        }
        profileRepository.updatePassword(bCryptPasswordEncoder.encode(profileDTO.getNewPassword()), userId);
        return new AppResponse<>(resourceBundleMessageService.getMessage("password.update.success", lang));
    }

    public AppResponse<String> updateUsername(ProfileUpdateUsernameDTO profileDTO, AppLanguage lang) {
        //check
        Optional<ProfileEntity> optional = profileRepository.findByUsernameAndVisibleTrue(profileDTO.getUsername());
        if (optional.isPresent()) {
            log.info("Username already in use: {}", profileDTO.getUsername());
            throw new AppBadException(resourceBundleMessageService.getMessage("email.phone.exists", lang));
        }

        //send confirm code
        if (ValidityUtil.isValidEmail(profileDTO.getUsername())) {
            emailSendingService.sendUsernameChangeConfirmEmail(profileDTO.getUsername(), lang);
        }
        if (ValidityUtil.isValidPhone(profileDTO.getUsername())) {
            smsSendService.sendUsernameChangeConfirmSms(profileDTO.getUsername(), lang);
        }

        String userId = SpringSecurityUtil.getCurrentUserId();
        profileRepository.updateTempUsername(profileDTO.getUsername(), userId);

        return new AppResponse<>(resourceBundleMessageService.getMessage("confirm.code.sent", lang, profileDTO.getUsername()));
    }

    public AppResponse<String> updateUsernameConfirm(CodeConfirmDTO dto, AppLanguage lang) {
        String userId = SpringSecurityUtil.getCurrentUserId();
        ProfileEntity profileEntity = findProfileById(userId, lang);
        String tempUsername = profileEntity.getTempUsername();
        // check if email valid
        if (ValidityUtil.isValidEmail(tempUsername)) {
            emailHistoryService.check(tempUsername, dto.getCode(), lang);
        }
        //check if phone valid
        if (ValidityUtil.isValidPhone(tempUsername)) {
            smsHistoryService.check(tempUsername, dto.getCode(), lang);
        }
        //update username after checking
        profileRepository.updateUsername(userId, tempUsername);

        List<ProfileRole> roleList = profileRoleRepository.getAllRoles(profileEntity.getId());
        String jwt = JwtUtil.encode(userId, tempUsername, roleList);
        return new AppResponse<>(jwt, resourceBundleMessageService.getMessage("username.update.success", lang));
    }

    public AppResponse<String> updateProfilePhoto(ProfilePhotoUpdateDTO profileUpdateDTO, AppLanguage lang) {
        String userId = SpringSecurityUtil.getCurrentUserId();
        ProfileEntity profileEntity = findProfileById(userId, lang);
        String currentPhotoId = profileEntity.getPhotoId();
        if (profileUpdateDTO.getPhotoId() != null && profileUpdateDTO.getPhotoId() != profileEntity.getPhotoId()) { // check if profile photo is being renewed
            if (currentPhotoId != null) { // check if current photo exists before deleting
                attachService.delete(currentPhotoId);
            }
        }
        profileRepository.updateProfilePhoto(userId, profileUpdateDTO.getPhotoId());
        return new AppResponse<>(resourceBundleMessageService.getMessage("profile.photo.updated", lang));
    }

    public Page<ProfileDTO> filterProfile(ProfileFilterDTO profileFilterDTO, AppLanguage lang, int page, Integer size) {
        Page<ProfileDetailMapper> profileDetailMapperPage = null;
        Pageable pageable = PageRequest.of(page, size);
        if (profileFilterDTO.getQuery() == null) {
            profileDetailMapperPage = profileRepository.filterProfile(pageable);
        } else {
            profileDetailMapperPage = profileRepository.filterProfile("%" + profileFilterDTO.getQuery().toLowerCase() + "%", pageable);
        }
        List<ProfileDTO> profileDTOList = Objects.requireNonNull(profileDetailMapperPage).stream().map(this::toDTO).toList();
        return new PageImpl<>(profileDTOList, pageable, profileDetailMapperPage.getTotalElements());
    }

    public AppResponse<String> changeProfileStatus(String userId, ProfileStatusDTO profileStatusDTO, AppLanguage lang) {
        profileRepository.updateStatus(userId, profileStatusDTO.getStatus());
        return new AppResponse<>(resourceBundleMessageService.getMessage("profile.update.success", lang));
    }

    public AppResponse<String> deleteProfile(String userId, AppLanguage lang) {
        profileRoleRepository.deleteProfile(userId);
        return new AppResponse<>(resourceBundleMessageService.getMessage("profile.delete.success", lang));
    }

    public void updateChatStatus(String email, ChatStatus toStatus) {
        profileRepository.updateChatStatus(email, toStatus);
    }

    public List<ProfileDTO> getOnlineProfileList() {
        //TODO Alter it so only users in contact and online be visible
        List<ProfileEntity> entityList = profileRepository.onlineProfileList(ChatStatus.ONLINE);
        List<ProfileDTO> dtoList = entityList.stream().map(item -> toDTO(item)).toList();
        return dtoList;
    }

    // util methods
    private ProfileDTO toDTO(ProfileEntity profileEntity) {
        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setId(profileEntity.getId());
        profileDTO.setName(profileEntity.getName());
        profileDTO.setUsername(profileEntity.getUsername());
        if (profileEntity.getRoles() != null) {
            List<ProfileRole> profileRoleList = profileEntity.getRoles().stream().map(ProfileRoleEntity::getRoles).toList();
            profileDTO.setRole(profileRoleList);
        }
        profileDTO.setAttachDTO(attachService.attachDTO(profileEntity.getPhotoId()));
        profileDTO.setStatus(profileEntity.getStatus());
        profileDTO.setCreatedDate(profileEntity.getCreatedDate());
        return profileDTO;
    }

    private ProfileDTO toDTO(ProfileDetailMapper mapper) {
        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setId(mapper.getId());
        profileDTO.setName(mapper.getName());
        profileDTO.setPostCount(mapper.getPostCount());
        profileDTO.setUsername(mapper.getUsername());
        if (mapper.getProfileRole() != null) {
            String roles = mapper.getProfileRole();
            String[] rolesArray = roles.split("'");
            List<ProfileRole> profileRoleList = new ArrayList<>();
            for (String role : rolesArray) {
                ProfileRole profileRole = ProfileRole.valueOf(role);
                profileRoleList.add(profileRole);
            }
            profileDTO.setRole(profileRoleList);
        }
        profileDTO.setAttachDTO(attachService.attachDTO(mapper.getPhotoId()));
        profileDTO.setStatus(mapper.getStatus());
        profileDTO.setCreatedDate(mapper.getCreatedDate());
        return profileDTO;
    }

    public ProfileDTO toProfileShortInfoDTO(String profileId, AppLanguage lang) {
        ProfileEntity profile = findProfileById(profileId, lang);
        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setId(profile.getId());
        profileDTO.setName(profile.getName());
        profileDTO.setAttachDTO(attachService.attachDTO(profile.getPhotoId()));
        return profileDTO;
    }


}
