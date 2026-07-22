package com.christian.todo.todo;

import com.christian.todo.dto.TodoRequest;
import com.christian.todo.repository.TodoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TodoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TodoRepository todoRepository;

    // Jackson 3's JsonMapper handles LocalDateTime natively, no extra module needed.
    private final JsonMapper objectMapper = JsonMapper.builder().build();

    @AfterEach
    void cleanUp() {
        todoRepository.deleteAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoTodos() throws Exception {
        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldCreateTodoViaPutThenFetchItById() throws Exception {
        TodoRequest request = new TodoRequest();
        request.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        request.setTitle("Buy milk");
        request.setDescription("2 liters, semi-skimmed");
        request.setCompleted(false);
        request.setCreatedAt(LocalDateTime.now());

        mockMvc.perform(put("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(request.getId().toString())))
                .andExpect(jsonPath("$.title", is("Buy milk")))
                .andExpect(jsonPath("$.completed", is(false)));

        mockMvc.perform(get("/todos/{id}", request.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Buy milk")));
    }

    @Test
    void shouldReturn404WhenTodoDoesNotExist() throws Exception {
        // Must be a syntactically valid UUID that simply doesn't exist,
        // otherwise Spring rejects the path variable conversion with a 400
        // before the controller/service ever gets to raise a 404.
        UUID unknownId = UUID.fromString("00000000-0000-0000-0000-000000000000");

        mockMvc.perform(get("/todos/{id}", unknownId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenIdOrCreatedAtIsMissing() throws Exception {
        TodoRequest request = new TodoRequest();
        request.setTitle("Missing id and createdAt");
        request.setCompleted(false);
        // id and createdAt intentionally left null

        mockMvc.perform(put("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenTitleIsBlank() throws Exception {
        TodoRequest request = new TodoRequest();
        request.setId(UUID.fromString("44444444-4444-4444-4444-444444444444"));
        request.setTitle("   ");
        request.setCreatedAt(LocalDateTime.now());

        mockMvc.perform(put("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFilterTodosByCompletedStatus() throws Exception {
        TodoRequest done = new TodoRequest();
        done.setId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        done.setTitle("Done task");
        done.setCompleted(true);
        done.setCreatedAt(LocalDateTime.now());

        TodoRequest pending = new TodoRequest();
        pending.setId(UUID.fromString("33333333-3333-3333-3333-333333333333"));
        pending.setTitle("Pending task");
        pending.setCompleted(false);
        pending.setCreatedAt(LocalDateTime.now());

        mockMvc.perform(put("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(done)))
                .andExpect(status().isOk());
        mockMvc.perform(put("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pending)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/todos").param("completed", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Done task")));

        mockMvc.perform(get("/todos").param("completed", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Pending task")));

        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}