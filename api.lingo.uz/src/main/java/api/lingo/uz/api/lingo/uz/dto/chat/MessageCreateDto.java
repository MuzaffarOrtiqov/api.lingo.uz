package api.lingo.uz.api.lingo.uz.dto.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageCreateDto {
    @NotBlank(message = "content")
    private String message;

}
