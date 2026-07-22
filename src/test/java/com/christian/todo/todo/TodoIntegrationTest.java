package com.christian.todo.todo;

import com.example.todos.dto.TodoRequest;
import com.example.todos.repository.TodoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

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

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

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
        request.setId("11111111-1111-1111-1111-111111111111");
        request.setTitle("Buy milk");
        request.setDescription("2 liters, semi-skimmed");
        request.setCompleted(false);
        request.setCreatedAt(LocalDateTime.now());

        mockMvc.perform(put("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(request.getId())))
                .andExpect(jsonPath("$.title", is("Buy milk")))
                .andExpect(jsonPath("$.completed", is(false)));

        mockMvc.perform(get("/todos/{id}", request.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Buy milk")));
    }

    @Test
    void shouldReturn404WhenTodoDoesNotExist() throws Exception {
        mockMvc.perform(get("/todos/{id}", "does-not-exist"))
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
        request.setId("44444444-4444-4444-4444-444444444444");
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
        done.setId("22222222-2222-2222-2222-222222222222");
        done.setTitle("Done task");
        done.setCompleted(true);
        done.setCreatedAt(LocalDateTime.now());

        TodoRequest pending = new TodoRequest();
        pending.setId("33333333-3333-3333-3333-333333333333");
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
