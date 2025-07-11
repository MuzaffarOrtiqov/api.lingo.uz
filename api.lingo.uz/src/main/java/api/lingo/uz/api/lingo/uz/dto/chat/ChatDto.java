package api.lingo.uz.api.lingo.uz.dto.chat;

import api.lingo.uz.api.lingo.uz.dto.attach.AttachDTO;
import api.lingo.uz.api.lingo.uz.enums.ChatStatus;
import api.lingo.uz.api.lingo.uz.enums.ChatType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatDto {
    private String id;
    private String name;
    private AttachDTO photo;
    private ChatStatus chatStatus;
    private ChatMessageDto message;
    private ChatType chatType; // CHAT,GROUP
    private Long unreadMessageCount;
}
