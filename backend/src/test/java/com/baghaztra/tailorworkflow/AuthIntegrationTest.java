package com.baghaztra.tailorworkflow;

import com.baghaztra.tailorworkflow.security.AuthCookie;
import com.baghaztra.tailorworkflow.entity.Worker;
import com.baghaztra.tailorworkflow.repository.WorkerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AuthIntegrationTest extends IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WorkerRepository workers;

    @Test
    void loginRefreshLogoutAndRevocationWork() throws Exception {
        MvcResult login = mockMvc.perform(loginRequest("test-password"))
                .andExpect(status().isOk())
                .andReturn();

        Cookie access = login.getResponse().getCookie(AuthCookie.ACCESS_NAME);
        Cookie refresh = login.getResponse().getCookie(AuthCookie.REFRESH_NAME);
        assertNotNull(access);
        assertNotNull(refresh);
        assertTrue(access.isHttpOnly());
        assertTrue(refresh.isHttpOnly());
        assertEquals(900, access.getMaxAge());
        assertEquals(28800, refresh.getMaxAge());

        MvcResult me = mockMvc.perform(get("/api/auth/me").cookie(access))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals("admin", objectMapper.readTree(me.getResponse().getContentAsString())
                .get("username")
                .asText());

        MvcResult refreshed = mockMvc.perform(post("/api/auth/refresh").cookie(refresh))
                .andExpect(status().isOk())
                .andReturn();
        Cookie refreshedAccess = refreshed.getResponse().getCookie(AuthCookie.ACCESS_NAME);
        assertNotNull(refreshedAccess);
        mockMvc.perform(get("/api/auth/me").cookie(refreshedAccess))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/logout").cookie(refresh))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/auth/me").cookie(access))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/refresh").cookie(refresh))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsInvalidCredentials() throws Exception {
        mockMvc.perform(loginRequest("wrong-password"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void paginatedWorkersReturnPageShape() throws Exception {
        workers.save(new Worker("Budi", Worker.Specialization.CUTTING));
        workers.save(new Worker("Siti", Worker.Specialization.SEWING));
        workers.save(new Worker("Ayu", Worker.Specialization.FINISHING));

        Cookie access = mockMvc.perform(loginRequest("test-password"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getCookie(AuthCookie.ACCESS_NAME);

        MvcResult page = mockMvc.perform(get("/api/workers")
                        .param("page", "0")
                        .param("size", "2")
                        .cookie(access))
                .andExpect(status().isOk())
                .andReturn();
        var json = objectMapper.readTree(page.getResponse().getContentAsString());
        assertEquals(3, json.get("totalElements").asInt());
        assertEquals(2, json.get("content").size());
    }

    @Test
    void rateLimitsRepeatedFailedLogins() throws Exception {
        for (int attempt = 1; attempt <= 5; attempt++) {
            mockMvc.perform(loginRequest("wrong-password"))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(loginRequest("wrong-password"))
                .andExpect(status().isTooManyRequests())
                .andExpect(result -> assertNotNull(result.getResponse().getHeader("Retry-After")));
    }

    private MockHttpServletRequestBuilder loginRequest(String password) {
        return post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"username":"admin","password":"%s"}
                        """.formatted(password));
    }
}
