package api.lingo.uz.api.lingo.uz.entity;


import api.lingo.uz.api.lingo.uz.enums.ChatStatus;
import api.lingo.uz.api.lingo.uz.enums.GeneralStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "profile")
public class ProfileEntity {
    @Id
    @GeneratedValue(generator = "uuid-hibernate-generator")
    @GenericGenerator(name = "uuid-hibernate-generator", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "surname")
    private String surname;

    @Column(name = "username")
    private String username;

    @Column(name = "temp_username")
    private String tempUsername;

    @Column(name = "password")
    private String password;

    @Column(name = "photo_id")
    private String photoId;
    @JoinColumn(name = "photo_id",insertable=false, updatable=false)
    @ManyToOne(fetch = FetchType.LAZY)
    private AttachEntity photo;


    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private GeneralStatus status = GeneralStatus.ACTIVE;

    @Column(name = "visible")
    private boolean visible = true;
    @Column(name = "created_date")
    private LocalDateTime createdDate = LocalDateTime.now();

    @OneToMany(mappedBy = "profileEntity", fetch = FetchType.LAZY)
    private List<ProfileRoleEntity> roles;

    @Enumerated(EnumType.STRING)
    @Column(name = "chat_status")
    private ChatStatus chatStatus;
}
