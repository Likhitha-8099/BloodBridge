package com.bloodbridge.security;

import com.bloodbridge.entity.User;
import com.bloodbridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Implementation of Spring Security's {@link UserDetailsService}.
 * Loads user credentials and roles from the database using {@link UserRepository}.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final com.bloodbridge.repository.HospitalRepository hospitalRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        boolean enabled = Boolean.TRUE.equals(user.getActive());
        if (user.getRole() == com.bloodbridge.enums.Role.HOSPITAL) {
            java.util.Optional<com.bloodbridge.entity.Hospital> hospOpt = hospitalRepository.findByUserId(user.getId());
            if (hospOpt.isPresent() && hospOpt.get().isApprovedOrVerified()) {
                enabled = true;
            }
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                enabled,
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
