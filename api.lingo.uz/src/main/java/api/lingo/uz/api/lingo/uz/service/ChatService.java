package api.lingo.uz.api.lingo.uz.service;

import api.lingo.uz.api.lingo.uz.config.CustomUserDetails;
import api.lingo.uz.api.lingo.uz.dto.chat.ChatDto;
import api.lingo.uz.api.lingo.uz.dto.chat.ChatMessageDto;
import api.lingo.uz.api.lingo.uz.dto.profile.ProfileDTO;
import api.lingo.uz.api.lingo.uz.enums.ChatStatus;
import api.lingo.uz.api.lingo.uz.enums.ChatType;
import api.lingo.uz.api.lingo.uz.mapper.ChatAndGroupMapper;
import api.lingo.uz.api.lingo.uz.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
public class ChatService {
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private AttachService attachService;


    public Page<ChatDto> getChatList(Principal principal, int page, int size) {
        CustomUserDetails currentProfile = (CustomUserDetails) ((Authentication) principal).getPrincipal();
        PageRequest pageable = PageRequest.of(page, size);
        Page<ChatAndGroupMapper> profileAndGroupMappers = profileRepository.getChatAndGroupList(currentProfile.getId(), currentProfile.getCreatedDate(), pageable);
        List<ChatDto> chatDtoList = profileAndGroupMappers.stream().map((profileAndGroupMapper) -> getChatDto(profileAndGroupMapper)).toList();
        return new PageImpl<>(chatDtoList, pageable, profileAndGroupMappers.getTotalElements());

    }

    private ChatDto getChatDto(ChatAndGroupMapper chatAndGroupMapper) {
        ChatDto chatDto = new ChatDto();
        chatDto.setId(chatAndGroupMapper.getChatId());
        chatDto.setName(chatAndGroupMapper.getChatName());
        chatDto.setPhoto(attachService.attachDTO(chatAndGroupMapper.getPhotoId()));
      //  chatDto.setChatStatus(ChatStatus.valueOf(chatAndGroupMapper.getChatStatus()));
        //chatMessageDTO
        ChatMessageDto chatMessageDto = new ChatMessageDto();
        chatMessageDto.setContent(chatAndGroupMapper.getMessageContent());
        chatMessageDto.setIsReceiverRead(chatAndGroupMapper.getReceiverRead());
        chatMessageDto.setCreatedDate(chatAndGroupMapper.getMessageCreatedDate());
        chatMessageDto.setReceiverId(chatAndGroupMapper.getReceiverId());
        //ProfileDTO
        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setId(chatAndGroupMapper.getSenderId());
        profileDTO.setName(chatAndGroupMapper.getSenderName());
        chatMessageDto.setSender(profileDTO);
        chatDto.setMessage(chatMessageDto);

        chatDto.setChatType(ChatType.valueOf(chatAndGroupMapper.getChatType()));
        chatDto.setUnreadMessageCount(chatAndGroupMapper.getUnreadMessageCount());
        return chatDto;
    }
}
