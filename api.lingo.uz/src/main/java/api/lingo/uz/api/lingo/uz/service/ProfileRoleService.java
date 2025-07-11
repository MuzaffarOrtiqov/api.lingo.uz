package api.lingo.uz.api.lingo.uz.service;


import api.lingo.uz.api.lingo.uz.entity.ProfileRoleEntity;
import api.lingo.uz.api.lingo.uz.enums.ProfileRole;
import api.lingo.uz.api.lingo.uz.repository.ProfileRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfileRoleService {
    @Autowired
    private ProfileRoleRepository profileRoleRepository;

    public void create (String id, ProfileRole role) {
        ProfileRoleEntity profileRoleEntity = new ProfileRoleEntity();
        profileRoleEntity.setProfileId(id);
        profileRoleEntity.setRoles(role);
        profileRoleRepository.save(profileRoleEntity);
    }

    public void deleteRoles(String profileId) {
        profileRoleRepository.deleteRole(profileId);
    }


}
