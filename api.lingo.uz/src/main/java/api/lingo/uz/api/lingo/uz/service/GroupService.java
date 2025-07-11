package api.lingo.uz.api.lingo.uz.service;

import api.lingo.uz.api.lingo.uz.config.CustomUserDetails;
import api.lingo.uz.api.lingo.uz.dto.GroupDto;
import api.lingo.uz.api.lingo.uz.entity.GroupEntity;
import api.lingo.uz.api.lingo.uz.exps.AppBadException;
import api.lingo.uz.api.lingo.uz.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.security.Principal;

@Service
public class GroupService {
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private AttachService attachService;

    public GroupDto createNewGroup(GroupDto groupDto, Principal principal) {
        CustomUserDetails currentUser = (CustomUserDetails) ((Authentication) principal).getPrincipal();
        GroupEntity groupEntity = new GroupEntity();
        groupEntity.setName(groupDto.getName());
        groupEntity.setPhotoId(groupDto.getPhotoId());
        groupEntity.setOwnerId(currentUser.getId());
        groupRepository.save(groupEntity);
        return toDto(groupEntity);
    }

    public Boolean updateGroup(String id, GroupDto groupDto, Principal principal) {
        CustomUserDetails currentUser = (CustomUserDetails) ((Authentication) principal).getPrincipal();
        GroupEntity groupEntity = get(id);
        if (!groupEntity.getOwnerId().equals(currentUser.getId())) {
            throw new AppBadException("Not allowed to update group");
        }
        groupEntity.setName(groupDto.getName());
        groupEntity.setPhotoId(groupDto.getPhotoId());
        groupRepository.save(groupEntity);
        return Boolean.TRUE;
    }

    public Boolean deleteGroup(String id, Principal principal) {
        CustomUserDetails currentUser = (CustomUserDetails) ((Authentication) principal).getPrincipal();
        GroupEntity groupEntity = get(id);
        if (!groupEntity.getOwnerId().equals(currentUser.getId())) {
            throw new AppBadException("Not allowed to update group");
        }
        groupRepository.deleteGroup(id);
        return Boolean.TRUE;
    }

    //util
    private GroupDto toDto(GroupEntity groupEntity) {
        GroupDto groupDto = new GroupDto();
        groupDto.setId(groupEntity.getId());
        groupDto.setName(groupEntity.getName());
        groupDto.setPhoto(attachService.attachDTO(groupEntity.getPhotoId()));
        return groupDto;
    }

    public GroupEntity get(String chatId) {
        return groupRepository.findById(chatId).orElseThrow(() -> new AppBadException("No group was found with the id"));
    }


}
