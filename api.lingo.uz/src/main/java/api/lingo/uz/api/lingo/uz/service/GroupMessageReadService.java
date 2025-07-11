package api.lingo.uz.api.lingo.uz.service;

import api.lingo.uz.api.lingo.uz.entity.GroupMessageReadEntity;
import api.lingo.uz.api.lingo.uz.repository.GroupMessageReadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GroupMessageReadService {
    @Autowired
    private GroupMessageReadRepository groupMessageReadRepository;

    public void create(String messageId, String profileId) {
        GroupMessageReadEntity groupMessageReadEntity = new GroupMessageReadEntity();
        groupMessageReadEntity.setChatMessageId(messageId);
        groupMessageReadEntity.setProfileId(profileId);
        groupMessageReadRepository.save(groupMessageReadEntity);
    }
}
