package api.lingo.uz.api.lingo.uz.mapper;

import java.time.LocalDateTime;

public interface ChatAndGroupMapper {
    String getChatId();

    String getChatName();

    String getPhotoId();

    String getChatType();

    String getChatStatus();

    String getMessageContent();

    Boolean getReceiverRead();

    LocalDateTime getMessageCreatedDate();

    String getSenderId();

    String getReceiverId();

    String getSenderName();

    Long getUnreadMessageCount();

}
