package api.lingo.uz.api.lingo.uz.controller;

import api.lingo.uz.api.lingo.uz.dto.chat.ChatMessageReadDto;
import api.lingo.uz.api.lingo.uz.enums.AppLanguage;
import api.lingo.uz.api.lingo.uz.enums.ChatStatus;
import api.lingo.uz.api.lingo.uz.enums.ChatType;
import api.lingo.uz.api.lingo.uz.service.ChatSocketService;
import api.lingo.uz.api.lingo.uz.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;

import java.security.Principal;

@Controller
public class ChatSocketController {
    @Autowired
    private ChatSocketService chatSocketService;
    @Autowired
    private ProfileService profileService;

    @MessageMapping("/chat.userOnline")
//    @SendTo("/topic/user-online") // when user is online
    public String userOnline(Principal principal) {
        // mark user as online
        profileService.updateChatStatus(principal.getName(), ChatStatus.ONLINE);
        // send update notification
        chatSocketService.sendChatToUser(principal.getName());
        return "Done";
    }

    @MessageMapping("/chat.markMessageAsRead")
    public String markMessageAsRead(@Payload ChatMessageReadDto dto, Principal principal,
                                    @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage lang) {
        chatSocketService.markMessageAsRead(dto.getMessageId(), principal, lang);
        return "DONE";
    }

    @MessageMapping("/chat.userTyping.{chatId}.{chatType}")
    public String userTyping(@DestinationVariable String chatId,
                             @DestinationVariable ChatType chatType,
                             Principal principal,
                             @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage lang) {
        chatSocketService.userTyping(chatId, chatType, principal, lang);
        return "DONE";
    }


}
