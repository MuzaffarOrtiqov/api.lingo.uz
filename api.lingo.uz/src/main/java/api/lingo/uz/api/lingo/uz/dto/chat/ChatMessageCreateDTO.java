package api.lingo.uz.api.lingo.uz.dto.chat;

import api.lingo.uz.api.lingo.uz.enums.ChatMessageType;
import api.lingo.uz.api.lingo.uz.enums.ChatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageCreateDTO {
    @NotBlank(message = "Content shouldn't be blank" )
    private String content;
    private String receiverId; // ProfileId or GroupId
    @NotNull(message = "chatType should be given")
    private ChatType chatType;
    private String repliedMessageId;
    private ChatMessageType chatMessageType;
    private String capture;
    private String attachId;
}
