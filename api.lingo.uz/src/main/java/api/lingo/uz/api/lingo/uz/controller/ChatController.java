package api.lingo.uz.api.lingo.uz.controller;

import api.lingo.uz.api.lingo.uz.dto.chat.ChatDto;
import api.lingo.uz.api.lingo.uz.service.ChatService;
import api.lingo.uz.api.lingo.uz.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {
    @Autowired
    private ChatService chatService;

    @GetMapping("")
    public ResponseEntity<Page<ChatDto>> getChatList(@RequestParam(value = "page", defaultValue = "1") int page,
                                                     @RequestParam(value = "size", defaultValue = "10") int size,
                                                     Principal principal) {
        Page<ChatDto> response = chatService.getChatList(principal, PageUtil.giveProperPageNumbering(page), size);
        return ResponseEntity.ok(response);
    }
}
