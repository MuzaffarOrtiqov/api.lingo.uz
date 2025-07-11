package api.lingo.uz.api.lingo.uz.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "group_message_read", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"chat_message_id", "profile_id"})
})
public class GroupMessageReadEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(name = "chat_message_id")
    private String chatMessageId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_message_id", updatable = false, insertable = false)
    private ChatMessageEntity chatMessage;

    @Column(name = "profile_id")
    private String profileId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", updatable = false, insertable = false)
    private ProfileEntity profile;
    @Column
    @CreationTimestamp
    private LocalDateTime createdDate;
}
