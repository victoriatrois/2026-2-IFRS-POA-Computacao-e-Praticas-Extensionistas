package ifrs.edu.avaliacao_mnr.security;

import ifrs.edu.avaliacao_mnr.authorization.Permission;
import ifrs.edu.avaliacao_mnr.authorization.RolePermissions;
import ifrs.edu.avaliacao_mnr.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public final class AuthenticatedUser implements UserDetails {

    private final User user;
    private final List<GrantedAuthority> authorities;

    public AuthenticatedUser(User user) {
        this.user = user;
        List<GrantedAuthority> mappedAuthorities = new ArrayList<>();
        mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        for (Permission permission : RolePermissions.forRole(user.getRole())) {
            mappedAuthorities.add(new SimpleGrantedAuthority(permission.name()));
        }
        this.authorities = List.copyOf(mappedAuthorities);
    }

    public UUID getId() { return user.getId(); }
    public String getEmail() { return user.getEmail(); }
    public String getName() { return user.getName(); }
    public String getSurname() { return user.getSurname(); }
    public String getCpf() { return user.getCpf(); }
    public String getRole() { return user.getRole().name(); }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override
    public String getPassword() { return user.getPasswordHash(); }

    @Override
    public String getUsername() { return user.getEmail(); }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return user.isActive(); }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return user.isActive(); }
}