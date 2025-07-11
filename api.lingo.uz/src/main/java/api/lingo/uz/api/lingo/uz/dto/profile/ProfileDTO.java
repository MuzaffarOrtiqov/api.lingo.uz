package api.lingo.uz.api.lingo.uz.dto.profile;


import api.lingo.uz.api.lingo.uz.dto.attach.AttachDTO;
import api.lingo.uz.api.lingo.uz.enums.GeneralStatus;
import api.lingo.uz.api.lingo.uz.enums.ProfileRole;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileDTO {
    private String id;
    private String name;
    private String surname;
    private String username;
    private String password;
    private GeneralStatus status;
    private List<ProfileRole> role;
    private Boolean visible;
    private LocalDateTime createdDate;
    private AttachDTO attachDTO;
    private Long postCount;
    private String roles;

}
