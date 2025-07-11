package api.lingo.uz.api.lingo.uz.controller;

import api.lingo.uz.api.lingo.uz.dto.chat.ChatMessageCreateDTO;
import api.lingo.uz.api.lingo.uz.dto.chat.ChatMessageDto;
import api.lingo.uz.api.lingo.uz.dto.chat.ChatMessageUpdateDTO;
import api.lingo.uz.api.lingo.uz.enums.AppLanguage;
import api.lingo.uz.api.lingo.uz.service.ChatMessageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat-message")
public class ChatMessageController {
    @Autowired
    private ChatMessageService chatMessageService;

    @PostMapping("")
    public ResponseEntity<ChatMessageDto> createChatMessage(@Valid @RequestBody ChatMessageCreateDTO dto,
                                                            @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage lang) { //ChatMessage
        return ResponseEntity.ok(chatMessageService.createChatMessage(dto,lang));
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<Boolean> updateMessage(@PathVariable(name = "messageId") String messageId,
                                                 @RequestBody ChatMessageUpdateDTO dto,
                                                 @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage lang) { //ChatMessage
        return ResponseEntity.ok(chatMessageService.updateChatMessage(messageId, dto,lang));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Boolean> deleteMessage(@PathVariable(name = "messageId") String messageId,
                                                 @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage lang) { //ChatMessage
        return ResponseEntity.ok(chatMessageService.deleteChatMessage(messageId,lang));
    }
}
