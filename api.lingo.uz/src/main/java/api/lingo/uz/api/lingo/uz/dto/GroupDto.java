package api.lingo.uz.api.lingo.uz.dto;

import api.lingo.uz.api.lingo.uz.dto.attach.AttachDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroupDto {
    private String id;
    @NotBlank(message = "Name is required")
    private String name;
    @NotNull
    private String photoId;
    private AttachDTO photo;
}
