package api.lingo.uz.api.lingo.uz.repository;

import api.lingo.uz.api.lingo.uz.entity.ChatMessageEntity;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, String> {
    @Transactional
    @Modifying
    @Query("UPDATE ChatMessageEntity SET isReceiverRead=TRUE WHERE id=?1")
    void markMessageAsRead(String messageId);

    @Transactional
    @Modifying
    @Query("UPDATE ChatMessageEntity SET content=?1 WHERE id=?2 AND visible=TRUE ")
    void updateChatMessage(@NotBlank String content, String messageId);

    @Transactional
    @Modifying
    @Query("UPDATE ChatMessageEntity SET visible=FALSE WHERE id=?1 ")
    void deleteChatMessage(String messageId);
}
