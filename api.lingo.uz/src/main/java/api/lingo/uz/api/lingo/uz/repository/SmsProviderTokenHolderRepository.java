package api.lingo.uz.api.lingo.uz.repository;

import api.lingo.uz.api.lingo.uz.entity.SmsProviderTokenHolderEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SmsProviderTokenHolderRepository extends CrudRepository<SmsProviderTokenHolderEntity, Integer> {
    Optional<SmsProviderTokenHolderEntity> findFirstByOrderByIdDesc();

}
