package api.lingo.uz.api.lingo.uz.repository;

import api.lingo.uz.api.lingo.uz.entity.GroupEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface GroupRepository extends JpaRepository<GroupEntity, String> {

    @Transactional
    @Modifying
    @Query("UPDATE GroupEntity SET visible=FALSE WHERE id=?1")
    void deleteGroup(String id);
}
