package com.christian.todo.todo;

import com.christian.todo.dto.TodoRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * "Full" integration test: boots the whole Spring context against a real
 * PostgreSQL database running in Docker (via Testcontainers), instead of H2.
 * This catches things an in-memory DB can hide (SQL dialect quirks, real
 * constraints, driver behaviour, etc.).
 *
 * Requires a running Docker daemon on the machine executing the tests.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class TodoContainerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("tododb")
            .withUsername("todos")
            .withPassword("todos");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    @Autowired
    private MockMvc mockMvc;

    // Jackson 3's JsonMapper handles LocalDateTime natively, no extra module needed.
    private final JsonMapper objectMapper = JsonMapper.builder().build();

    @Test
    void shouldCreateAndRetrieveTodoAgainstRealPostgres() throws Exception {
        TodoRequest request = new TodoRequest();
        request.setId(UUID.fromString("99999999-9999-9999-9999-999999999999"));
        request.setTitle("Integration test against real Postgres");
        request.setDescription("Runs inside a Docker container via Testcontainers");
        request.setCompleted(false);
        request.setCreatedAt(LocalDateTime.now());

        mockMvc.perform(put("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(request.getId().toString())));

        mockMvc.perform(get("/todos/{id}", request.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(request.getTitle())));
    }
}