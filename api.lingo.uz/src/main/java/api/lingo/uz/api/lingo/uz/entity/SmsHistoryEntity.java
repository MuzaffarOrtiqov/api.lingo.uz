package api.lingo.uz.api.lingo.uz.entity;


import api.lingo.uz.api.lingo.uz.enums.SmsType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "sms_history")
public class SmsHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(name = "phone")
    private String phone;
    @Column(name = "message", columnDefinition = "text")
    private String message;
    @Column(name = "code")
    private String code;
    @Column(name = "created_date")
    @CreationTimestamp
    private LocalDateTime createdDate;
    @Column(name = "sms_type")
    @Enumerated(EnumType.STRING)
    private SmsType smsType;
    @Column(name = "attempt_count")
    private Integer attemptCount=0;
}
