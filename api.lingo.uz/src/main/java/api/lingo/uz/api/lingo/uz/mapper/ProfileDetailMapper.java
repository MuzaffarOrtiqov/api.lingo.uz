package api.lingo.uz.api.lingo.uz.mapper;
import api.lingo.uz.api.lingo.uz.enums.GeneralStatus;

import java.time.LocalDateTime;

public interface ProfileDetailMapper {
    String getId();
    String getName();
    String getUsername();
    String getPhotoId();
    GeneralStatus getStatus();
    LocalDateTime getCreatedDate();
    Long getPostCount();
    String getProfileRole();
}
