package api.lingo.uz.api.lingo.uz.service;

import api.lingo.uz.api.lingo.uz.dto.sms.SmsAuthDTO;
import api.lingo.uz.api.lingo.uz.dto.sms.SmsRequestDTO;
import api.lingo.uz.api.lingo.uz.dto.sms.SmsSendResponseDTO;
import api.lingo.uz.api.lingo.uz.entity.SmsProviderTokenHolderEntity;
import api.lingo.uz.api.lingo.uz.enums.AppLanguage;
import api.lingo.uz.api.lingo.uz.enums.SmsType;
import api.lingo.uz.api.lingo.uz.exps.AppBadException;
import api.lingo.uz.api.lingo.uz.repository.SmsProviderTokenHolderRepository;
import api.lingo.uz.api.lingo.uz.util.RandomUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class SmsSendService {
    @Autowired
    private RestTemplate restTemplate;
    @Value("${eskiz.api}")
    private String smsUrl;
    @Value("${eskiz.login}")
    private String accountLogin;
    @Value("${eskiz.password}")
    private String accountPassword;

    @Autowired
    private SmsProviderTokenHolderRepository smsProviderTokenHolderRepository;
    @Autowired
    private SmsHistoryService smsHistoryService;
    @Autowired
    private ResourceBundleMessageService resourceBundleMessageService;
    private final Long smsLimit = 3l;

    public void sendRegistrationSms(String phone, AppLanguage language) {
        String message = resourceBundleMessageService.getMessage("sms.confirm.registration.code", language);
        String code = RandomUtil.getRandomSmsCode();
        sendSms(phone, message, code, SmsType.REGISTRATION);
    }
    public void sendPasswordResetSms(String phone, AppLanguage language) {
        String message = resourceBundleMessageService.getMessage("sms.reset.password.code", language);
        String code = RandomUtil.getRandomSmsCode();
        sendSms(phone, message, code, SmsType.RESET_PASSWORD);
    }

    public void sendUsernameChangeConfirmSms(String username, AppLanguage lang) {
        String message = resourceBundleMessageService.getMessage("sms.change.username.code", lang);
        String code = RandomUtil.getRandomSmsCode();
        sendSms(username, message, code, SmsType.CHANGE_USERNAME);
    }

    private SmsSendResponseDTO sendSms(String phoneNumber, String message, String code, SmsType smsType) {
        Long count = smsHistoryService.getSmsCount(phoneNumber);
        if (count > smsLimit) {
            log.warn("Sms limit reached phone number: {}", phoneNumber);
            throw new AppBadException("Try again later");
        }
        SmsSendResponseDTO result = sendSms(phoneNumber, message);
        smsHistoryService.createSmsHistory(phoneNumber, message, code, smsType);
        return result;
    }

    private SmsSendResponseDTO sendSms(String phoneNumber, String message) {
        //header
        String token = getToken();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + token);
        //body
        SmsRequestDTO smsRequestDTO = new SmsRequestDTO();
        smsRequestDTO.setMobilePhone(phoneNumber);
        smsRequestDTO.setMessage(message);
        smsRequestDTO.setFrom("4546");
        //send request
        try {
            HttpEntity<SmsRequestDTO> entity = new HttpEntity<>(smsRequestDTO, headers);
            ResponseEntity<SmsSendResponseDTO> response = restTemplate.exchange(smsUrl + "/message/sms/send", HttpMethod.POST, entity, SmsSendResponseDTO.class);
            return response.getBody();
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Send Sms failed phone :{}, message: {}, error: {}",phoneNumber, message,e.getMessage());
            throw new RuntimeException(e);
        }

    }

    private String getToken() {
        Optional<SmsProviderTokenHolderEntity> optional = smsProviderTokenHolderRepository.findFirstByOrderByIdDesc();
        if (optional.isEmpty()) {
            String token = getTokenFromProvider();
            SmsProviderTokenHolderEntity smsProviderTokenHolderEntity = new SmsProviderTokenHolderEntity();
            smsProviderTokenHolderEntity.setToken(token);
            smsProviderTokenHolderEntity.setCreatedDate(LocalDateTime.now());
            smsProviderTokenHolderEntity.setExpiredDate(LocalDateTime.now().plusMonths(1));
            smsProviderTokenHolderRepository.save(smsProviderTokenHolderEntity);
            return token;
        }
        SmsProviderTokenHolderEntity entity = optional.get();
        if (!entity.getCreatedDate().isBefore(entity.getExpiredDate())) {
            String token = getTokenFromProvider();
            entity.setToken(token);
            entity.setCreatedDate(LocalDateTime.now());
            entity.setExpiredDate(LocalDateTime.now().plusMonths(1));
            smsProviderTokenHolderRepository.save(entity);
            return token;
        }
        return entity.getToken();

    }
    private String getTokenFromProvider() {
        SmsAuthDTO smsAuthDTO = new SmsAuthDTO();
        smsAuthDTO.setEmail(accountLogin);
        smsAuthDTO.setPassword(accountPassword);

        String response = restTemplate.postForObject(smsUrl + "/auth/login", smsAuthDTO, String.class);
        try {
            JsonNode parent = new ObjectMapper().readTree(response);
            JsonNode child = parent.get("data");
            String token = child.get("token").asText();
            return token;
        } catch (JsonProcessingException e) {
            log.error("Get token :{}, error: {}",accountLogin, e.getMessage());
            throw new RuntimeException(e);
        }

    }



}
