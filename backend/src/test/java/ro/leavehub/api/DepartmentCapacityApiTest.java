package ro.leavehub.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DepartmentCapacityApiTest {

    private static final Pattern TOKEN = Pattern.compile("\\\"token\\\":\\\"([^\\\"]+)\\\"");
    private static final Pattern ID = Pattern.compile("\\\"id\\\":(\\d+)");

    @Autowired
    MockMvc mockMvc;

    @Test
    void approvalIsBlockedWhenDepartmentAbsenceLimitWouldBeExceeded() throws Exception {
        var admin = login("admin@leavehub.ro");
        var anaRequest = createAndSubmit(login("ana.popescu@leavehub.ro"), "Ana");
        var mihaiRequest = createAndSubmit(login("mihai.ionescu@leavehub.ro"), "Mihai");
        var managerRequest = createAndSubmit(login("manager@leavehub.ro"), "Manager");

        approve(admin, anaRequest).andExpect(status().isOk());
        approve(admin, mihaiRequest).andExpect(status().isOk());
        approve(admin, managerRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("limita departamentului este de 2")));
    }

    private String login(String email) throws Exception {
        var response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Demo123!\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return extract(TOKEN, response);
    }

    private long createAndSubmit(String token, String employee) throws Exception {
        var response = mockMvc.perform(post("/api/leave-requests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "leaveTypeId": 1,
                                  "startDate": "2027-08-16",
                                  "endDate": "2027-08-20",
                                  "reason": "Test limita departament %s"
                                }
                                """.formatted(employee)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        var id = Long.parseLong(extract(ID, response));
        mockMvc.perform(post("/api/leave-requests/{id}/submit", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        return id;
    }

    private org.springframework.test.web.servlet.ResultActions approve(String token, long requestId) throws Exception {
        return mockMvc.perform(post("/api/leave-requests/{id}/decision", requestId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"decision\":\"APPROVED\",\"comment\":\"Aprobare test\"}"));
    }

    private String extract(Pattern pattern, String value) {
        var matcher = pattern.matcher(value);
        if (!matcher.find()) {
            throw new AssertionError("Campul asteptat lipseste din raspuns.");
        }
        return matcher.group(1);
    }
}
