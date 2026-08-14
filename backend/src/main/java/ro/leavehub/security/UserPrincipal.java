package ro.leavehub.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ro.leavehub.model.Employee;

import java.util.Collection;
import java.util.List;

public record UserPrincipal(
        Long id,
        String email,
        String password,
        boolean active,
        Collection<? extends GrantedAuthority> authorities) implements UserDetails {

    public static UserPrincipal from(Employee employee) {
        return new UserPrincipal(
                employee.getId(),
                employee.getEmail(),
                employee.getPasswordHash(),
                employee.getActive(),
                List.of(new SimpleGrantedAuthority("ROLE_" + employee.getRole().name())));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
