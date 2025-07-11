package api.lingo.uz.api.lingo.uz.service;

import api.lingo.uz.api.lingo.uz.config.CustomUserDetails;
import api.lingo.uz.api.lingo.uz.entity.ProfileEntity;
import api.lingo.uz.api.lingo.uz.repository.ProfileRepository;
import api.lingo.uz.api.lingo.uz.repository.ProfileRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private ProfileRoleRepository profileRoleRepository;

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<ProfileEntity> optional = profileRepository.findByUsernameAndVisibleTrue(username);
        if (optional.isEmpty()) {
            throw new UsernameNotFoundException(username);
        }
        ProfileEntity profile = optional.get();
        CustomUserDetails userDetails = new CustomUserDetails(profile,profileRoleRepository.getAllRoles(profile.getId()));
        return userDetails;
    }
}
