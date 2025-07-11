package api.lingo.uz.api.lingo.uz.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GroupMemberIsTypingDto {
    private String senderId;
    private String name;
}
