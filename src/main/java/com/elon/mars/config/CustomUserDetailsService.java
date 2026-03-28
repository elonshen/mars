package com.elon.mars.config;

import com.elon.mars.domain.User;
import com.elon.mars.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 只有在首次用用户名密码登录时才会执行loadUserByUsername方法,默认登录第一个租户
        User user = userRepository.findFirstByAuth_Username(username).orElseThrow(() -> new UsernameNotFoundException("username is not found"));
        user.getRoles().stream().flatMap(role -> role.getPermissions().stream()).forEach(permission -> System.out.println(permission.getCode()));

        return new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return user.getRoles().stream()
                        .flatMap(role -> role.getPermissions().stream())
                        .map(permission -> (GrantedAuthority) permission::getCode)
                        .distinct()
                        .collect(Collectors.toList());
            }

            @Override
            public String getPassword() {
                return user.getAuth().getPassword();
            }

            @Override
            public String getUsername() {
                return user.getAuth().getUsername();
            }

            @Override
            public boolean isAccountNonExpired() {
                return true;
            }

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
        };
    }
}
