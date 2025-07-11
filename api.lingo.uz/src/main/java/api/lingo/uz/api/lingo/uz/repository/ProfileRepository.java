package api.lingo.uz.api.lingo.uz.repository;

import api.lingo.uz.api.lingo.uz.entity.ProfileEntity;
import api.lingo.uz.api.lingo.uz.enums.ChatStatus;
import api.lingo.uz.api.lingo.uz.enums.GeneralStatus;
import api.lingo.uz.api.lingo.uz.mapper.ChatAndGroupMapper;
import api.lingo.uz.api.lingo.uz.mapper.ProfileDetailMapper;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<ProfileEntity, String>, PagingAndSortingRepository<ProfileEntity, String> {

    Optional<ProfileEntity> findByIdAndVisibleTrue(String id);

    @Transactional
    @Modifying
    @Query("UPDATE ProfileEntity AS p SET p.status=?2 WHERE p.id=?1")
    void updateStatus(String profileId, GeneralStatus generalStatus);

    @Query("FROM ProfileEntity AS p WHERE p.username=?1 AND p.visible=TRUE")
    Optional<ProfileEntity> findByUsernameAndVisibleTrue(String username);


    @Modifying
    @Transactional
    @Query("UPDATE ProfileEntity p SET p.password=?1 WHERE p.id=?2")
    void updatePassword(String password, String id);

    @Modifying
    @Transactional
    @Query("UPDATE ProfileEntity SET name=?1 WHERE id=?2")
    void updateProfileName(String name, String id);

    @Modifying
    @Transactional
    @Query("UPDATE ProfileEntity SET tempUsername=?1 WHERE id=?2")
    void updateTempUsername(String username, String userId);

    @Modifying
    @Transactional
    @Query("UPDATE ProfileEntity SET username=?2 WHERE id=?1")
    void updateUsername(String userId, String tempUsername);


    @Modifying
    @Transactional
    @Query("UPDATE ProfileEntity SET photoId=?2 WHERE id=?1")
    void updateProfilePhoto(String userId, String photoId);

    @Query("SELECT p.id as id, p.name as name, p.username as username, p.photoId as photoId, p.status as status, p.createdDate as createdDate,  " +
            "(select count (post) FROM PostEntity AS post WHERE p.id=post.profileId ) as postCount, " +
            "(select string_agg(pr.roles,',') FROM ProfileRoleEntity pr WHERE p.id= pr.profileId) as profileRole   " +
            "FROM ProfileEntity p  " +
            "WHERE p.visible = true ORDER BY p.createdDate DESC")
    Page<ProfileDetailMapper> filterProfile(Pageable pageable);

    /*  @Query("SELECT p FROM ProfileEntity p INNER JOIN FETCH p.roles  WHERE p.visible = true ORDER BY p.createdDate DESC")
    Page<ProfileEntity> filterProfile(Pageable pageable);*/

    @Query("SELECT p.id as id, p.name as name, p.username as username, p.photoId as photoId, p.status as status, p.createdDate as createdDate,  " +
            "(select count (post) FROM PostEntity AS post WHERE p.id=post.profileId ) as postCount, " +
            "(select string_agg(pr.roles,',') FROM ProfileRoleEntity pr WHERE p.id= pr.profileId) as profileRole   " +
            "FROM ProfileEntity p  " +
            "WHERE (p.id = ?1 OR lower(p.username) LIKE ?1 OR lower(p.name) LIKE ?1) AND p.visible = true ORDER BY p.createdDate DESC")
    Page<ProfileDetailMapper> filterProfile(String search, Pageable pageable);


    @Modifying
    @Transactional
    @Query("UPDATE ProfileEntity SET chatStatus=?2 WHERE id=?1")
    void updateChatStatus(String email, ChatStatus toStatus);


    @Query("FROM ProfileEntity AS p WHERE p.chatStatus=?1 AND p.visible=true ")
    List<ProfileEntity> onlineProfileList(ChatStatus online);

    @Query(value = """
    SELECT 
        p.id AS chatId,
        CONCAT(p.name, ' ', p.surname) AS chatName,
        p.photo_id AS chatPhotoId,
        'USER' AS chatType,
        lm.content AS messageContent,
        lm.is_receiver_read AS isReceiverRead,
        lm.created_date AS createdDate,
        lm.sender_id AS senderId,
        lm.receiver_id AS receiverId,
        sender.name AS senderName,
        (
            SELECT COUNT(*) 
            FROM chat_message 
            WHERE 
                is_receiver_read = FALSE 
                AND receiver_id = :profileId 
                AND sender_id = p.id
        ) AS unreadMessageCount
    FROM profile AS p
    LEFT JOIN LATERAL (
        SELECT * FROM chat_message AS cm 
        WHERE 
            ((cm.sender_id = p.id AND cm.receiver_id = :profileId) OR 
             (cm.sender_id = :profileId AND cm.receiver_id = p.id))
            AND cm.visible = true
        ORDER BY cm.created_date DESC
        LIMIT 1
    ) AS lm ON TRUE
    LEFT JOIN profile AS sender ON sender.id = lm.sender_id
    WHERE 
        p.id != :profileId
        AND p.visible = true 
        AND p.status = 'ACTIVE'

    UNION ALL

    SELECT 
        g.id AS chatId,
        g.name AS chatName,
        g.photo_id AS chatPhotoId,
        'GROUP' AS chatType,
        lm.content AS messageContent,
        lm.is_receiver_read AS isReceiverRead,
        lm.created_date AS createdDate,
        lm.sender_id AS senderId,
        lm.receiver_id AS receiverId,
        p.name AS senderName,
        (
            SELECT COUNT(*) 
            FROM chat_message cm3 
            WHERE cm3.created_date > (
                SELECT cm2.created_date
                FROM chat_message cm2
                INNER JOIN group_message_read gmr ON gmr.chat_message_id = cm2.id
                WHERE 
                    cm2.visible = true 
                    AND cm2.receiver_id = g.id
                    AND gmr.profile_id = :profileId
                    AND cm2.created_date >= :profileJoinedDate
                ORDER BY cm2.created_date DESC 
                LIMIT 1
            )
            AND cm3.receiver_id = g.id                                                              
        ) AS unreadMessageCount
    FROM groups g
    LEFT JOIN LATERAL (
        SELECT *
        FROM chat_message cm
        WHERE cm.receiver_id = g.id AND cm.visible = true
        ORDER BY cm.created_date DESC
        LIMIT 1
    ) lm ON TRUE
    LEFT JOIN profile p ON p.id = lm.sender_id
    WHERE g.visible = true 

    ORDER BY createdDate DESC NULLS LAST
""", nativeQuery = true)
    Page<ChatAndGroupMapper> getChatAndGroupList(String profileId, LocalDateTime profileJoinedDate, PageRequest pageable);
}
