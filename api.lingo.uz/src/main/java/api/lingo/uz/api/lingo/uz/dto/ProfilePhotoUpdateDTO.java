package api.lingo.uz.api.lingo.uz.dto;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfilePhotoUpdateDTO {
    @Column(name = "photoId is required")
    private String photoId;
}
