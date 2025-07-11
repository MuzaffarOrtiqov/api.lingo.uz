package api.lingo.uz.api.lingo.uz.dto.sms;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SmsVerificationDTO {
    @NotBlank(message = "Phone is required")
    private String phone;
    @NotBlank(message = "Code is required")
    private String code;
}
