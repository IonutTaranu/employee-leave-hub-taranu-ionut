package ro.leavehub.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ro.leavehub.api.ApiDtos.AuthResponse;
import ro.leavehub.api.ApiDtos.LoginRequest;
import ro.leavehub.api.ApiDtos.UserSummary;
import ro.leavehub.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserSummary me() {
        return authService.me();
    }
}
