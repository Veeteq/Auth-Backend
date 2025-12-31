package com.veeteq.auth.authservice.security;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.veeteq.auth.authservice.entity.AuthUser;

public class AuthUserPrincipal implements UserDetails {
	private static final long serialVersionUID = 1L;

	private final AuthUser authUser;

	public AuthUserPrincipal(AuthUser authUser) {
		this.authUser = authUser;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authUser.getRoles().stream()
				.map(SimpleGrantedAuthority::new)
				.collect(Collectors.toList());
	}

	@Override
	public String getPassword() {
		return authUser.getPassword();
	}

	@Override
	public String getUsername() {
		return authUser.getUsername();
	}

	public AuthUser getAuthUser() {
		return authUser;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	} // adapt if you add fields

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}
