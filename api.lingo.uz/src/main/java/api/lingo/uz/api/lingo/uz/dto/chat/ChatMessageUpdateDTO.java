package api.lingo.uz.api.lingo.uz.dto.chat;

import api.lingo.uz.api.lingo.uz.enums.ChatType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageUpdateDTO {
    @NotBlank
    private String content;
    private ChatType chatType;
}
