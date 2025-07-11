package api.lingo.uz.api.lingo.uz.service;

import api.lingo.uz.api.lingo.uz.config.CustomUserDetails;
import api.lingo.uz.api.lingo.uz.dto.chat.ChatMessageDto;
import api.lingo.uz.api.lingo.uz.dto.chat.GroupMemberIsTypingDto;
import api.lingo.uz.api.lingo.uz.dto.profile.ProfileDTO;
import api.lingo.uz.api.lingo.uz.entity.ChatMessageEntity;
import api.lingo.uz.api.lingo.uz.entity.GroupEntity;
import api.lingo.uz.api.lingo.uz.entity.ProfileEntity;
import api.lingo.uz.api.lingo.uz.enums.AppLanguage;
import api.lingo.uz.api.lingo.uz.enums.ChatType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
public class ChatSocketService {
    @Autowired
    private ProfileService profileService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private ChatMessageService chatMessageService;
    @Autowired
    private GroupMessageReadService groupMessageReadService;
    @Autowired
    private GroupService groupService;

    public void sendChatToUser(String username) {
        // send update user status to other users.
        List<ProfileDTO> userList = profileService.getOnlineProfileList();
        messagingTemplate.convertAndSendToUser(username, "/topic/getUpdate", userList);
    }

    public void markMessageAsRead(String messageId, Principal principal, AppLanguage lang) {
        CustomUserDetails customUserDetails = (CustomUserDetails) ((Authentication) principal).getPrincipal();
        ChatMessageEntity chatMessageEntity = chatMessageService.getById(messageId, lang);
        //change status read
        chatMessageService.markMessageAsRead(messageId);
        if (chatMessageEntity.getChatType().equals(ChatType.GROUP)) {
            groupMessageReadService.create(messageId, customUserDetails.getId());
        }
        // send notification
        ChatMessageDto chatMessageDto = new ChatMessageDto();
        chatMessageDto.setId(chatMessageEntity.getId());
        if (chatMessageEntity.getChatType().equals(ChatType.USER)) {
            ProfileEntity sender = profileService.findProfileById(chatMessageEntity.getSenderId(), lang);
            messagingTemplate.convertAndSendToUser(sender.getUsername(), "/queue/chatMessageRead/", chatMessageDto);
        } else {
            messagingTemplate.convertAndSend("/topic/chatMessageRead/" + chatMessageDto.getReceiverId(), chatMessageDto);
        }
    }


    public void userTyping(String chatId, ChatType chatType, Principal principal, AppLanguage lang) {
        CustomUserDetails profile = (CustomUserDetails) ((Authentication) principal).getPrincipal();
        if (chatType.equals(ChatType.USER)) {
            ProfileEntity receiver = profileService.findProfileById(chatId, lang);
            messagingTemplate.convertAndSendToUser(receiver.getUsername(), "/queue/userTyping/", "");
        } else {
            GroupEntity groupEntity = groupService.get(chatId);
            GroupMemberIsTypingDto groupMemberIsTypingDto = new GroupMemberIsTypingDto(profile.getId(), profile.getName());
            messagingTemplate.convertAndSend("topic/userTyping/" + groupEntity.getId(), groupMemberIsTypingDto);
        }

    }

    public void sendChatUpdateToGroup(String chatId, ChatMessageDto response) {
        messagingTemplate.convertAndSend("/topic/getChatUpdate/" + chatId, response);
    }

    public void sendChatUpdateToUser(String email, ChatMessageDto response) {
        messagingTemplate.convertAndSendToUser(email,"/queue/getChatUpdate/" , response);
    }
}
