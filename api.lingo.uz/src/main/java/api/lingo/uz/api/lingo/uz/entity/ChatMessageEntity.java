package api.lingo.uz.api.lingo.uz.entity;

import api.lingo.uz.api.lingo.uz.enums.ChatMessageType;
import api.lingo.uz.api.lingo.uz.enums.ChatType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "chat_message")
public class ChatMessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "sender_id")
    private String senderId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", insertable = false, updatable = false)
    private ProfileEntity sender;

    private String receiverId; // profileId or groupId

    @Enumerated(EnumType.STRING)
    @Column(name = "chat_type")
    private ChatType chatType; // CHAT,GROUP

    @Column(name = "replied_message_id")
    private String repliedMessageId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replied_message_id", insertable = false, updatable = false)
    private ChatMessageEntity repliedMessage;

    @Column(name = "capture", columnDefinition = "TEXT")
    private String capture;
    @Column(name = "attach_id")
    private String attachId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attach_id", insertable = false, updatable = false)
    private AttachEntity attach;

    @Enumerated(EnumType.STRING)
    @Column(name = "chat_message_type")
    private ChatMessageType chatMessageType;
    @Column(name = "created_date")
    @CreationTimestamp
    private LocalDateTime createdDate;

    @Column(name = "is_receiver_read")
    private Boolean isReceiverRead = Boolean.FALSE;
    @Column(name = "visible")
    private Boolean visible = Boolean.TRUE;

    @OneToMany(mappedBy = "chatMessage", fetch = FetchType.LAZY)
    private List<GroupMessageReadEntity> gmrList;
}