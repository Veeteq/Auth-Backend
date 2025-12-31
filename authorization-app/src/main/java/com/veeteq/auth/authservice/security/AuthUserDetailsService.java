package com.veeteq.auth.authservice.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.veeteq.auth.authservice.entity.AuthUser;
import com.veeteq.auth.authservice.repository.AuthUserRepository;

@Service
public class AuthUserDetailsService implements UserDetailsService {

    private final AuthUserRepository authUserRepository;

    public AuthUserDetailsService(AuthUserRepository authUserRepository) {
        this.authUserRepository = authUserRepository;
    }
    
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AuthUser authUser = authUserRepository.findByUsername(username)
        		.or(() -> authUserRepository.findByEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        return new AuthUserPrincipal(authUser);
	}

}
