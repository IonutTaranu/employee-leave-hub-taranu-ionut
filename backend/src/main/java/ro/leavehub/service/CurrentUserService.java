package ro.leavehub.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ro.leavehub.model.Employee;
import ro.leavehub.repository.EmployeeRepository;
import ro.leavehub.security.UserPrincipal;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final EmployeeRepository employeeRepository;

    public Employee get() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw ApiException.forbidden("Autentificare necesara.");
        }
        return employeeRepository.findById(principal.id())
                .orElseThrow(() -> ApiException.notFound("Utilizatorul autentificat nu mai exista."));
    }
}
