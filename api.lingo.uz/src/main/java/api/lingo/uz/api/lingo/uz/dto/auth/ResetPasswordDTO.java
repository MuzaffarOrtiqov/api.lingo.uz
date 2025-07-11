package api.lingo.uz.api.lingo.uz.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordDTO {
    @NotBlank(message = "Username is required")
    private String username;
}
