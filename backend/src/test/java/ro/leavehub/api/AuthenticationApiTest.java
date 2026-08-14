package ro.leavehub.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationApiTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void employeeCanAuthenticateAndReadDashboard() throws Exception {
        var loginBody = """
                {"email":"ana.popescu@leavehub.ro","password":"Demo123!"}
                """;
        var response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("EMPLOYEE"))
                .andReturn().getResponse().getContentAsString();

        var matcher = Pattern.compile("\\\"token\\\":\\\"([^\\\"]+)\\\"").matcher(response);
        org.assertj.core.api.Assertions.assertThat(matcher.find()).isTrue();
        var token = matcher.group(1);
        mockMvc.perform(get("/api/dashboard").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance.annual").value(21))
                .andExpect(jsonPath("$.recentRequests").isArray());
    }

    @Test
    void invalidPasswordIsRejected() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@leavehub.ro\",\"password\":\"gresit\"}"))
                .andExpect(status().isUnauthorized());
    }
}
