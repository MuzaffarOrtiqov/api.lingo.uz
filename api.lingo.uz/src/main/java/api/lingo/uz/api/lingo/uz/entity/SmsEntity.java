package api.lingo.uz.api.lingo.uz.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "sms")
public class SmsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(name = "code")
    private String code;
    @Column(name = "message", columnDefinition = "text")
    private String message;
    @Column(name = "phone")
    private String phone;
    @CreationTimestamp
    @Column(name = "created_date")
    private LocalDateTime createdDate;



}
