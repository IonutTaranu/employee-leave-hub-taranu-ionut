package ro.leavehub.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.leavehub.api.ApiDtos.AuthResponse;
import ro.leavehub.api.ApiDtos.LoginRequest;
import ro.leavehub.api.ApiDtos.UserSummary;
import ro.leavehub.repository.EmployeeRepository;
import ro.leavehub.security.JwtService;
import ro.leavehub.security.UserPrincipal;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final EmployeeRepository employeeRepository;
    private final JwtService jwtService;
    private final ApiMapper mapper;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password()));
        var principal = (UserPrincipal) authentication.getPrincipal();
        var employee = employeeRepository.findById(principal.id())
                .orElseThrow(() -> ApiException.notFound("Utilizatorul nu exista."));
        return new AuthResponse(jwtService.generate(principal), mapper.user(employee));
    }

    @Transactional(readOnly = true)
    public UserSummary me() {
        return mapper.user(currentUserService.get());
    }
}
