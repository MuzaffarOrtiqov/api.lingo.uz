package api.lingo.uz.api.lingo.uz.dto.profile;

import api.lingo.uz.api.lingo.uz.enums.GeneralStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileStatusDTO {
    @NotNull(message = "Status is required")
    private GeneralStatus status;
}
