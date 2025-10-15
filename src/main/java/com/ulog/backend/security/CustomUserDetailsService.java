package com.ulog.backend.security;

import com.ulog.backend.domain.user.User;
import com.ulog.backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByPhone(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found for phone: " + username));
        return new UserPrincipal(user);
    }

    public UserDetails loadUserById(Long id) {
        User user = userRepository.findActiveById(id)
            .orElseThrow(() -> new UsernameNotFoundException("User not found for id: " + id));
        return new UserPrincipal(user);
    }
}
