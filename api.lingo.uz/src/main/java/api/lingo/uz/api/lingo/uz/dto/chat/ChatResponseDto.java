package api.lingo.uz.api.lingo.uz.dto.chat;

import api.lingo.uz.api.lingo.uz.dto.profile.ProfileDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
public class ChatResponseDto {
    private String id;
    private String message;
    private ProfileDTO profile;
    private LocalDateTime createdDate;
}
