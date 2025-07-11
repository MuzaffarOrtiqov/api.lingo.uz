package api.lingo.uz.api.lingo.uz.service;

import api.lingo.uz.api.lingo.uz.config.CustomUserDetails;
import api.lingo.uz.api.lingo.uz.dto.chat.ChatMessageCreateDTO;
import api.lingo.uz.api.lingo.uz.dto.chat.ChatMessageDto;
import api.lingo.uz.api.lingo.uz.dto.chat.ChatMessageUpdateDTO;
import api.lingo.uz.api.lingo.uz.dto.profile.ProfileDTO;
import api.lingo.uz.api.lingo.uz.entity.ChatMessageEntity;
import api.lingo.uz.api.lingo.uz.entity.GroupEntity;
import api.lingo.uz.api.lingo.uz.entity.ProfileEntity;
import api.lingo.uz.api.lingo.uz.enums.AppLanguage;
import api.lingo.uz.api.lingo.uz.enums.ChatMessageType;
import api.lingo.uz.api.lingo.uz.enums.ChatType;
import api.lingo.uz.api.lingo.uz.exps.AppBadException;
import api.lingo.uz.api.lingo.uz.repository.ChatMessageRepository;
import api.lingo.uz.api.lingo.uz.util.SpringSecurityUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ChatMessageService {
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    @Autowired
    private ProfileService profileService;
    @Autowired
    private ResourceBundleMessageBundleService resourceBundleMessageBundleService;
    @Autowired
    private AttachService attachService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private GroupMessageReadService groupMessageReadService;
    @Autowired
    @Lazy
    private ChatSocketService chatSocketService;

    public ChatMessageEntity getById(String messageId, AppLanguage lang) {
        return chatMessageRepository.findById(messageId).orElseThrow(() -> new AppBadException(resourceBundleMessageBundleService.getMessage("message.not.found", lang)));
    }

    public void markMessageAsRead(String messageId) {
        chatMessageRepository.markMessageAsRead(messageId);
    }

    public ChatMessageDto createChatMessage(ChatMessageCreateDTO dto, AppLanguage lang) {
        CustomUserDetails sender = SpringSecurityUtil.getCurrentProfile();
        if (dto.getChatMessageType() == ChatMessageType.TEXT && (dto.getContent() == null) || (dto.getContent().isBlank())) {
            throw new AppBadException("Message cannot be empty");
        }
        ChatMessageDto response = createChatMessage(dto, sender.getId(), lang);

        // send notification
        ProfileDTO senderDto = new ProfileDTO();
        senderDto.setId(sender.getId());
        senderDto.setName(sender.getName());
        senderDto.setSurname(sender.getSurname());
        response.setSender(senderDto);
        if (dto.getChatType().equals(ChatType.GROUP)) {
            GroupEntity group = groupService.get(dto.getReceiverId());
            groupMessageReadService.create(response.getId(), sender.getId());
            chatSocketService.sendChatUpdateToGroup(group.getId(), response);
        } else {
            ProfileEntity receiver = profileService.findProfileById(dto.getReceiverId(), lang);
            chatSocketService.sendChatUpdateToUser(receiver.getUsername(), response);

        }
        return response;
    }

    public Boolean updateChatMessage(String messageId, ChatMessageUpdateDTO dto, AppLanguage lang) {
        CustomUserDetails currentProfile = SpringSecurityUtil.getCurrentProfile();
        ChatMessageEntity chatMessageEntity = getById(messageId, lang);
        if (!chatMessageEntity.getSenderId().equals(currentProfile.getId())) {
            throw new AppBadException("Not allowed to update");
        }
        chatMessageRepository.updateChatMessage(dto.getContent(), messageId);  //update

        //send notification
        ChatMessageDto response = toDTO(chatMessageEntity);
        response.setSocketMessageType("UpdateMessage");
        ProfileDTO senderDto = new ProfileDTO();
        senderDto.setId(currentProfile.getId());
        senderDto.setName(currentProfile.getName());
        senderDto.setSurname(currentProfile.getSurname());
        response.setSender(senderDto);

        if (dto.getChatType().equals(ChatType.GROUP)) {
            GroupEntity group = groupService.get(chatMessageEntity.getReceiverId());
            chatSocketService.sendChatUpdateToGroup(group.getId(), response);
        } else {
            ProfileEntity receiver = profileService.findProfileById(chatMessageEntity.getReceiverId(), lang);
            chatSocketService.sendChatUpdateToUser(receiver.getUsername(), response);
        }
        return Boolean.TRUE;
    }

    public Boolean deleteChatMessage(String messageId, AppLanguage lang) {
        CustomUserDetails currentProfile = SpringSecurityUtil.getCurrentProfile();
        ChatMessageEntity chatMessageEntity = getById(messageId, lang);
        if (!chatMessageEntity.getSenderId().equals(currentProfile.getId())) {
            throw new AppBadException("Not allowed to update");
        }
        chatMessageRepository.deleteChatMessage(messageId);
        //send notification
        ChatMessageDto response = toDTO(chatMessageEntity);
        response.setSocketMessageType("DeleteMessage");
        ProfileDTO senderDto = new ProfileDTO();
        senderDto.setId(currentProfile.getId());
        senderDto.setName(currentProfile.getName());
        senderDto.setSurname(currentProfile.getSurname());
        response.setSender(senderDto);
        chatSocketService.sendChatUpdateToUser(currentProfile.getUsername(), response); // send notification

        return Boolean.TRUE;

    }

    //utils

    public ChatMessageDto createChatMessage(ChatMessageCreateDTO dto, String senderId, AppLanguage lang) {
        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setContent(dto.getContent());
        entity.setCapture(dto.getCapture());
        entity.setReceiverId(dto.getReceiverId());
        entity.setChatMessageType(dto.getChatMessageType()); // TEXT,PHOTO,...
        entity.setChatType(dto.getChatType());
        entity.setSenderId(senderId);
        entity.setRepliedMessageId(dto.getRepliedMessageId());
        entity.setCapture(dto.getCapture());
        entity.setAttachId(dto.getAttachId());
        entity.setIsReceiverRead(Boolean.FALSE);
        entity.setVisible(Boolean.TRUE);
        chatMessageRepository.save(entity);
        if (dto.getRepliedMessageId() != null) {
            entity.setRepliedMessage(getById(dto.getRepliedMessageId(), lang));
        }

        return toDTO(entity);
    }

    public ChatMessageDto toDTO(ChatMessageEntity entity) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(entity.getId());
        dto.setContent(entity.getContent());
        dto.setCapture(entity.getCapture());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setChatMessageType(entity.getChatMessageType());
        dto.setSenderId(entity.getSenderId());
        dto.setChatType(entity.getChatType());
        dto.setReceiverId(entity.getReceiverId());
        dto.setIsReceiverRead(entity.getIsReceiverRead());
        if (entity.getRepliedMessage() != null) {
            ChatMessageDto repliedMessage = new ChatMessageDto();
            repliedMessage.setId(entity.getRepliedMessageId());
            repliedMessage.setContent(entity.getRepliedMessage().getContent());
            dto.setRepliedMessage(repliedMessage);
        }
        if (entity.getAttachId() != null) {
            dto.setAttach(attachService.attachDTO(entity.getAttachId()));
        }
        return dto;
    }


}
