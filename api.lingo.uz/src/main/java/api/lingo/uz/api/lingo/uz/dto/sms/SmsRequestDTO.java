package api.lingo.uz.api.lingo.uz.dto.sms;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsRequestDTO {
    private String mobilePhone;
    private String message;
    private String from;

}
