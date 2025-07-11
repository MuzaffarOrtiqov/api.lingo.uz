package api.lingo.uz.api.lingo.uz.dto.chat;

import api.lingo.uz.api.lingo.uz.dto.attach.AttachDTO;
import api.lingo.uz.api.lingo.uz.dto.profile.ProfileDTO;
import api.lingo.uz.api.lingo.uz.enums.ChatMessageType;
import api.lingo.uz.api.lingo.uz.enums.ChatType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatMessageDto {
    private String id;
    private String content;
    private String senderId;
    private String receiverId;
    private ChatType chatType; // CHAT,GROUP
    private ChatMessageType chatMessageType;
    private LocalDateTime createdDate;
    private Boolean isReceiverRead;
    //
    private String socketMessageType; // NewMessage,UpdateMessage,DeleteMessage
    private ChatMessageDto repliedMessage;
    private AttachDTO attach;
    private String capture;

    private ProfileDTO sender;
}
