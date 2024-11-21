package com.example.taskmanagementsystem.controllers;

import com.example.taskmanagementsystem.dto.task.TaskProperty;
import com.example.taskmanagementsystem.dto.user.UserResponse;
import com.example.taskmanagementsystem.dto.task.TaskDtoConverter;
import com.example.taskmanagementsystem.dto.task.TaskResponse;
import com.example.taskmanagementsystem.models.*;
import com.example.taskmanagementsystem.repositories.CommentRepository;
import com.example.taskmanagementsystem.repositories.TaskRepository;
import com.example.taskmanagementsystem.repositories.UserRepository;
import com.example.taskmanagementsystem.security.JwtProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase

@Transactional
class TaskFieldControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskDtoConverter taskDtoConverter;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    private String token;
    private List<User> users;
    private List<Task> tasks;
    private List<Comment> comments;
    private TaskResponse taskResponse;

    @BeforeEach
    void setUp() {
        this.users = createUsers();
        this.token = jwtProvider.generateToken(users.get(0).getEmail());
        this.tasks = createTasks();
        comments = createComments();
        for (Comment comment : comments) {
            comment.getTask().getComments().add(comment);
        }
        comments = commentRepository.saveAll(comments);
        this.taskResponse = createTaskResponse();
    }

    private List<User> createUsers(){
        String password = passwordEncoder.encode("Password");
        return userRepository.saveAll(List.of(
                User.builder().name("maksim1").email("maksim1@mail.test").password(password).build(),
                User.builder().name("maksim2").email("maksim2@mail.test").password(password).build(),
                User.builder().name("maksim3").email("maksim3@mail.test").password(password).build(),
                User.builder().name("maksim4").email("maksim4@mail.test").password(password).build()
        ));
    }

    private List<Comment> createComments() {
        return commentRepository.saveAll(List.of(
                Comment.builder()
                        .task(tasks.get(0))
                        .dateTime(LocalDateTime.now().plusMinutes(5))
                        .commentator(users.get(0))
                        .text("Comment 1")
                        .build(),
                Comment.builder()
                        .task(tasks.get(0))
                        .dateTime(LocalDateTime.now().plusMinutes(2))
                        .commentator(users.get(1))
                        .text("Comment 2")
                        .build(),
                Comment.builder()
                        .task(tasks.get(0))
                        .dateTime(LocalDateTime.now().plusMinutes(1))
                        .commentator(users.get(2))
                        .text("Comment 3")
                        .build(),
                Comment.builder()
                        .task(tasks.get(1))
                        .dateTime(LocalDateTime.now().plusMinutes(3))
                        .commentator(users.get(2))
                        .text("Comment 4")
                        .build(),
                Comment.builder()
                        .task(tasks.get(1))
                        .dateTime(LocalDateTime.now().plusMinutes(6))
                        .commentator(users.get(1))
                        .text("Comment 5")
                        .build(),
                Comment.builder()
                        .task(tasks.get(1))
                        .dateTime(LocalDateTime.now().plusMinutes(4))
                        .commentator(users.get(0))
                        .text("Comment 6")
                        .build()
        ));
    }

    private List<Task> createTasks(){
        return taskRepository.saveAll(List.of(
                Task.builder()
                        .title("TestTask1")
                        .description("task 1")
                        .priority(TaskPriority.MEDIUM)
                        .status(TaskStatus.IN_PROGRESS)
                        .author(users.get(0))
                        .assignees(new ArrayList<>(List.of(users.get(1), users.get(2))))
                        .comments(new ArrayList<>())
                        .build(),
                Task.builder()
                        .title("TestTask2")
                        .description("task 2")
                        .priority(TaskPriority.LOW)
                        .status(TaskStatus.COMPLETED)
                        .author(users.get(1))
                        .assignees(new ArrayList<>(List.of(users.get(2))))
                        .comments(new ArrayList<>())
                        .build()
        ));
    }

    private TaskResponse createTaskResponse() {
        return taskDtoConverter.convertDtoToResponse(
                taskDtoConverter.convertEntityToDto(tasks.get(0)));
    }

    @Test
    void getId_ShouldReturnOkStatusAndTaskId() throws Exception {
        Map<String, Long> response = new HashMap<>();
        response.put("id", tasks.get(1).getId());

        mockMvc.perform(get("/api/tasks/{id}/id", tasks.get(1).getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    void getId_WhenTaskNotFound_ShouldReturnNotFoundStatus() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}/id", Long.MAX_VALUE)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void getId_UnauthorisedRequest_ShouldReturnForbiddenStatus() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}/id", tasks.get(1).getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getTitle_ShouldReturnOkStatusAndTaskTitle() throws Exception {
        Map<String, String> response = new HashMap<>();
        response.put("title", tasks.get(1).getTitle());

        mockMvc.perform(get("/api/tasks/{id}/title", tasks.get(1).getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    void getTitle_WhenTaskNotFound_ShouldReturnNotFoundStatus() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}/title", Long.MAX_VALUE)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTitle_UnauthorisedRequest_ShouldReturnForbiddenStatus() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}/title", tasks.get(1).getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getDescription_ShouldReturnOkStatusAndTaskDescription() throws Exception {
        Map<String, String> response = new HashMap<>();
        response.put("description", tasks.get(1).getDescription());

        mockMvc.perform(get("/api/tasks/{id}/description", tasks.get(1).getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    void getDescription_WhenTaskNotFound_ShouldReturnNotFoundStatus() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}/description", Long.MAX_VALUE)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void getDescription_UnauthorisedRequest_ShouldReturnForbiddenStatus() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}/description", tasks.get(1).getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getStatus_ShouldReturnOkStatusAndTaskStatus() throws Exception {
        Map<String, TaskStatus> response = new HashMap<>();
        response.put("status", tasks.get(1).getStatus());

        mockMvc.perform(get("/api/tasks/{id}/status", tasks.get(1).getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    void getStatus_WhenTaskNotFound_ShouldReturnNotFoundStatus() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}/status", Long.MAX_VALUE)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void getStatus_UnauthorisedRequest_ShouldReturnForbiddenStatus() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}/status", tasks.get(1).getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPriority_ShouldReturnOkStatusAndTaskPriority() throws Exception {
        Map<String, TaskPriority> response = new HashMap<>();
        response.put("priority", tasks.get(1).getPriority());

        mockMvc.perform(get("/api/tasks/{id}/priority", tasks.get(1).getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    void getPriority_WhenTaskNotFound_ShouldReturnNotFoundStatus() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}/priority", Long.MAX_VALUE)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPriority_UnauthorisedRequest_ShouldReturnForbiddenStatus() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}/priority", tasks.get(1).getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAuthor_ShouldReturnOkStatusAndTaskAuthor() throws Exception {
        TaskResponse task = taskDtoConverter.convertDtoToResponse(
                taskDtoConverter.convertEntityToDto(tasks.get(1))
        );
        Map<String, UserResponse> response = new HashMap<>();
        response.put("author", task.getAuthor());

        mockMvc.perform(get("/api/tasks/{id}/author", tasks.get(1).getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    void getAuthor_WhenTaskNotFound_ShouldReturnNotFoundStatus() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}/author", Long.MAX_VALUE)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAuthor_UnauthorisedRequest_ShouldReturnForbiddenStatus() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}/author", tasks.get(1).getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAssignees_ShouldReturnOkStatusAndTaskAssignees() throws Exception {
        TaskResponse task = taskDtoConverter.convertDtoToResponse(
                taskDtoConverter.convertEntityToDto(tasks.get(1))
        );
        Map<String, List<UserResponse>> response = new HashMap<>();
        response.put("assignees", task.getAssignees());

        mockMvc.perform(get("/api/tasks/{id}/assignees", tasks.get(1).getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    void getAssignees_WhenTaskNotFound_ShouldReturnNotFoundStatus() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}/assignees", Long.MAX_VALUE)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAssignees_UnauthorisedRequest_ShouldReturnForbiddenStatus() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}/assignees`", tasks.get(1).getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateTitle_WhenValidTitleInput_ShouldReturnOkStatusAndTaskResponseWithNewTitle() throws Exception {
        String title = "New Test Title";
        taskResponse.setTitle(title);

        mockMvc.perform(put("/api/tasks/{id}/title", tasks.get(0).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("title", title))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(taskResponse)));

    }

    @Test
    void updateTitle_WithEmptyTitleInput_ShouldReturnBadRequestStatus() throws Exception {
        String title = null;

        mockMvc.perform(put("/api/tasks/{id}/title", tasks.get(0).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("title", title))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateTitle_WithBlankTitleInput_ShouldReturnBadRequestStatus() throws Exception {
        String title = "";
        taskResponse.setTitle(title);

        mockMvc.perform(put("/api/tasks/{id}/title", tasks.get(0).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("title", title))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateTitle_WhenNotFoundTask_ShouldReturnBadRequestStatus() throws Exception {
        String title = "New Test Title";

        mockMvc.perform(put("/api/tasks/{id}/title", Long.MAX_VALUE)
                        .header("Authorization", "Bearer " + token)
                        .param("title", title))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTitle_WhenUpdateTitleAnotherUserTask_ShouldReturnBadRequestStatus() throws Exception {
        String title = "New Test Title";

        mockMvc.perform(put("/api/tasks/{id}/title", tasks.get(1).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("title", title))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateTitle_UnauthorisedRequest_ShouldReturnForbiddenStatus() throws Exception {
        String title = "New Test Title";

        mockMvc.perform(put("/api/tasks/{id}/title", tasks.get(0).getId())
                        .param("title", title))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateDescription_WhenValidDescriptionInput_ShouldReturnOkStatusAndTaskResponseWithNewDescription() throws Exception {
        String description = "New Test description";
        taskResponse.setDescription(description);

        mockMvc.perform(put("/api/tasks/{id}/description", tasks.get(0).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("description", description))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(taskResponse)));
    }

    @Test
    void updateDescription_WithEmptyDescriptionInput_ShouldReturnBadRequestStatus() throws Exception {
        String description = null;

        mockMvc.perform(put("/api/tasks/{id}/description", tasks.get(0).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("description", description))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateDescription_WhenNotFoundTask_ShouldReturnBadRequestStatus() throws Exception {
        String description = "New Test description";

        mockMvc.perform(put("/api/tasks/{id}/description", Long.MAX_VALUE)
                        .header("Authorization", "Bearer " + token)
                        .param("description", description))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateDescription_WhenUpdateDescriptionAnotherUserTask_ShouldReturnBadRequestStatus() throws Exception {
        String description = "New Test description";

        mockMvc.perform(put("/api/tasks/{id}/description", tasks.get(1).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("description", description))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateDescription_UnauthorisedRequest_ShouldReturnForbiddenStatus() throws Exception {
        String description = "New Test description";

        mockMvc.perform(put("/api/tasks/{id}/description", tasks.get(0).getId())
                        .param("description", description))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateStatus_WhenValidStatusValueInput_ShouldReturnOkStatusAndTaskResponseWithNewStatus() throws Exception {
        String statusValue = "3";
        taskResponse.setStatus(new TaskProperty(TaskStatus.getByValue(Integer.parseInt(statusValue))));

        mockMvc.perform(put("/api/tasks/{id}/status", tasks.get(0).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("status-value", statusValue))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(taskResponse)));
    }

    @Test
    void updateStatus_WithEmptyStatusValueInput_ShouldReturnBadRequestStatus() throws Exception {
        String statusValue = null;

        mockMvc.perform(put("/api/tasks/{id}/status", tasks.get(0).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("status-value", statusValue))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatus_WithInvalidStatusValueInput_ShouldReturnBadRequestStatus() throws Exception {
        String statusValue = "5";

        mockMvc.perform(put("/api/tasks/{id}/status", tasks.get(0).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("status-value", statusValue))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatus_WhenNotFoundTask_ShouldReturnBadRequestStatus() throws Exception {
        String statusValue = "3";

        mockMvc.perform(put("/api/tasks/{id}/status", Long.MAX_VALUE)
                        .header("Authorization", "Bearer " + token)
                        .param("status-value", statusValue))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStatus_WhenUpdateStatusAnotherUsersTask_ShouldReturnBadRequestStatus() throws Exception {
        String statusValue = "3";

        mockMvc.perform(put("/api/tasks/{id}/status", tasks.get(1).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("status-value", statusValue))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatus_UnauthorisedRequest_ShouldReturnForbiddenStatus() throws Exception {
        String statusValue = "3";

        mockMvc.perform(put("/api/tasks/{id}/status", tasks.get(0).getId())
                        .param("status-value", statusValue))
                .andExpect(status().isForbidden());
    }

    @Test
    void updatePriority_WhenValidPriorityValueInput_ShouldReturnOkStatusAndTaskResponseWithNewPriority() throws Exception {
        String priorityValue = "1";
        taskResponse.setPriority(new TaskProperty(TaskPriority.getByValue(Integer.parseInt(priorityValue))));

        mockMvc.perform(put("/api/tasks/{id}/priority", tasks.get(0).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("priority-value", priorityValue))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(taskResponse)));
    }

    @Test
    void updatePriority_WithEmptyPriorityValueInput_ShouldReturnBadRequestStatus() throws Exception {
        String priorityValue = null;

        mockMvc.perform(put("/api/tasks/{id}/priority", tasks.get(0).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("priority-value", priorityValue))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatePriority_WithInvalidPriorityValueInput_ShouldReturnBadRequestStatus() throws Exception {
        String priorityValue = "5";

        mockMvc.perform(put("/api/tasks/{id}/priority", tasks.get(0).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("priority-value", priorityValue))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatePriority_WhenNotFoundTask_ShouldReturnBadRequestStatus() throws Exception {
        String priorityValue = "3";

        mockMvc.perform(put("/api/tasks/{id}/priority", Long.MAX_VALUE)
                        .header("Authorization", "Bearer " + token)
                        .param("priority-value", priorityValue))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePriority_WhenUpdatePriorityAnotherUserTask_ShouldReturnBadRequestStatus() throws Exception {
        String priorityValue = "3";

        mockMvc.perform(put("/api/tasks/{id}/priority", tasks.get(1).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("priority-value", priorityValue))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatePriority_UnauthorisedRequest_ShouldReturnForbiddenStatus() throws Exception {
        String priorityValue = "3";

        mockMvc.perform(put("/api/tasks/{id}/priority", tasks.get(0).getId())
                        .param("priority-value", priorityValue))
                .andExpect(status().isForbidden());
    }

    @Test
    void addAssignee_WhenValidAssignIdInput_ShouldReturnOkStatusAndTaskResponseWithNewAssignee() throws Exception {
        String assigneeId = users.get(3).getId().toString();
        taskResponse.getAssignees().add(UserResponse.builder()
                .id(users.get(3).getId())
                .email(users.get(3).getEmail())
                .name(users.get(3).getName())
                .build());

        mockMvc.perform(post("/api/tasks/{id}/assignees", tasks.get(0).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("assignee-id", assigneeId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(taskResponse)));
    }

    @Test
    void addAssignee_WhenAssignIdInputAndAssignExist_ShouldReturnOkStatusAndTaskResponseWithOldListOfAssignees() throws Exception {
        String assigneeId = users.get(2).getId().toString();

        mockMvc.perform(post("/api/tasks/{id}/assignees", tasks.get(0).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("assignee-id", assigneeId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(taskResponse)));
    }

    @Test
    void addAssignee_WhenInvalidAssignIdInput_ShouldReturnBadRequestStatus() throws Exception {
        String assigneeId = users.get(3).getEmail();

        mockMvc.perform(post("/api/tasks/{id}/assignees", tasks.get(0).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("assignee-id", assigneeId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addAssignee_WhenEmptyAssignIdInput_ShouldReturnBadRequestStatus() throws Exception {
        String assigneeId = null;

        mockMvc.perform(post("/api/tasks/{id}/assignees", tasks.get(0).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("assignee-id", assigneeId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addAssignee_WhenAssignIdInputAndAddAssigneeAnotherUserTask_ShouldReturnBadRequestStatus() throws Exception {
        String assigneeId = users.get(3).getId().toString();

        mockMvc.perform(post("/api/tasks/{id}/assignees", tasks.get(1).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("assignee-id", assigneeId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addAssignee_WhenAssignIdInputAndAssigneeNotFound_ShouldReturnNotFoundStatus() throws Exception {
        String assigneeId = String.valueOf(Long.MAX_VALUE);

        mockMvc.perform(post("/api/tasks/{id}/assignees", tasks.get(0).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("assignee-id", assigneeId))
                .andExpect(status().isNotFound());
    }

    @Test
    void addAssignee_WhenAssignIdInputAndTaskNotFound_ShouldReturnNotFoundStatus() throws Exception {
        String assigneeId = users.get(3).getId().toString();

        mockMvc.perform(post("/api/tasks/{id}/assignees", Long.MAX_VALUE)
                        .header("Authorization", "Bearer " + token)
                        .param("assignee-id", assigneeId))
                .andExpect(status().isNotFound());
    }

    @Test
    void addAssignee_WhenValidAssignEmailInput_ShouldReturnOkStatusAndTaskResponseWithNewAssignee() throws Exception {
        String assigneeEmail = users.get(3).getEmail();
        taskResponse.getAssignees().add(UserResponse.builder()
                .id(users.get(3).getId())
                .email(users.get(3).getEmail())
                .name(users.get(3).getName())
                .build());

        mockMvc.perform(post("/api/tasks/{id}/assignees", tasks.get(0).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("assignee-email", assigneeEmail))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(taskResponse)));
    }

    @Test
    void addAssignee_WhenAssignEmailInputAndAssignExist_ShouldReturnOkStatusAndTaskResponseWithNewAssignee() throws Exception {
        String assigneeEmail = users.get(3).getEmail();
        taskResponse.getAssignees().add(UserResponse.builder()
                .id(users.get(3).getId())
                .email(users.get(3).getEmail())
                .name(users.get(3).getName())
                .build());

        mockMvc.perform(post("/api/tasks/{id}/assignees", tasks.get(0).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("assignee-email", assigneeEmail))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(taskResponse)));
    }

    @Test
    void addAssignee_WhenEmptyAssignEmailInput_ShouldReturnBadRequestStatus() throws Exception {
        String assigneeEmail = null;

        mockMvc.perform(post("/api/tasks/{id}/assignees", tasks.get(0).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("assignee-email", assigneeEmail))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addAssignee_WhenAssignEmailInputAndAddAssigneeAnotherUserTask_ShouldReturnBadRequestStatus() throws Exception {
        String assigneeEmail = users.get(3).getEmail();

        mockMvc.perform(post("/api/tasks/{id}/assignees", tasks.get(1).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("assignee-email", assigneeEmail))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addAssignee_WhenAssignEmailInputAndAssigneeNotFound_ShouldReturnNotFoundStatus() throws Exception {
        String assigneeEmail = "emilKotorogoNigdeNet@mail.test";

        mockMvc.perform(post("/api/tasks/{id}/assignees", tasks.get(0).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("assignee-email", assigneeEmail))
                .andExpect(status().isNotFound());
    }

    @Test
    void addAssignee_WhenAssignEmailInputAndTaskNotFound_ShouldReturnNotFoundStatus() throws Exception {
        String assigneeEmail = users.get(3).getEmail();

        mockMvc.perform(post("/api/tasks/{id}/assignees", Long.MAX_VALUE)
                        .header("Authorization", "Bearer " + token)
                        .param("assignee-email", assigneeEmail))
                .andExpect(status().isNotFound());
    }

    @Test
    void addAssignee_UnauthorisedRequest_ShouldReturnForbiddenStatus() throws Exception {
        String assigneeEmail = users.get(3).getEmail();

        mockMvc.perform(post("/api/tasks/{id}/assignees", tasks.get(0).getId())
                        .param("assignee-email", assigneeEmail))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteAssignee_WhenValidAssignIdInput_ShouldReturnOkStatusAndTaskResponseWithoutRemoteAssignee() throws Exception {
        String assigneeId = users.get(2).getId().toString();
        taskResponse.getAssignees().remove(1);

        mockMvc.perform(delete("/api/tasks/{id}/assignees", tasks.get(0).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("assignee-id", assigneeId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(taskResponse)));
    }

    @Test
    void deleteAssignee_WhenAssignIdInputAndAssignNonExist_ShouldReturnBadRequestStatus() throws Exception {
        String assigneeId = users.get(3).getId().toString();

        mockMvc.perform(delete("/api/tasks/{id}/assignees", tasks.get(0).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("assignee-id", assigneeId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteAssignee_WhenInvalidAssignIdInput_ShouldReturnBadRequestStatus() throws Exception {
        String assigneeId = users.get(3).getEmail();

        mockMvc.perform(delete("/api/tasks/{id}/assignees", tasks.get(0).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("assignee-id", assigneeId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteAssignee_WhenEmptyAssignIdInput_ShouldReturnBadRequestStatus() throws Exception {
        String assigneeId = null;

        mockMvc.perform(delete("/api/tasks/{id}/assignees", tasks.get(0).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("assignee-id", assigneeId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteAssignee_WhenAssignIdInputAndDeleteAssigneeAnotherUserTask_ShouldReturnBadRequestStatus() throws Exception {
        String assigneeId = users.get(2).getId().toString();

        mockMvc.perform(delete("/api/tasks/{id}/assignees", tasks.get(1).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("assignee-id", assigneeId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteAssignee_WhenAssignIdInputAndAssigneeNotFound_ShouldReturnNotFoundStatus() throws Exception {
        String assigneeId = String.valueOf(Long.MAX_VALUE);

        mockMvc.perform(delete("/api/tasks/{id}/assignees", tasks.get(0).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("assignee-id", assigneeId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAssignee_WhenAssignIdInputAndTaskNotFound_ShouldReturnNotFoundStatus() throws Exception {
        String assigneeId = users.get(2).getId().toString();

        mockMvc.perform(delete("/api/tasks/{id}/assignees", Long.MAX_VALUE)
                        .header("Authorization", "Bearer " + token)
                        .param("assignee-id", assigneeId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAssignee_WhenValidAssignEmailInput_ShouldReturnOkStatusAndTaskResponseWithoutRemoteAssignee() throws Exception {
        String assigneeEmail = users.get(2).getEmail();
        taskResponse.getAssignees().remove(1);

        mockMvc.perform(delete("/api/tasks/{id}/assignees", tasks.get(0).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("assignee-email", assigneeEmail))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(taskResponse)));
    }

    @Test
    void deleteAssignee_WhenAssignEmailInputAndAssignNonExist_ShouldReturnBadRequestStatus() throws Exception {
        String assigneeEmail = users.get(3).getEmail();

        mockMvc.perform(delete("/api/tasks/{id}/assignees", tasks.get(0).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("assignee-email", assigneeEmail))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteAssignee_WhenEmptyAssignEmailInput_ShouldReturnBadRequestStatus() throws Exception {
        String assigneeEmail = null;

        mockMvc.perform(delete("/api/tasks/{id}/assignees", tasks.get(0).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("assignee-email", assigneeEmail))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteAssignee_WhenAssignEmailInputAndDeleteAssigneeAnotherUserTask_ShouldReturnBadRequestStatus() throws Exception {
        String assigneeEmail = users.get(2).getEmail();

        mockMvc.perform(post("/api/tasks/{id}/assignees", tasks.get(1).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("assignee-email", assigneeEmail))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteAssignee_WhenAssignEmailInputAndAssigneeNotFound_ShouldReturnNotFoundStatus() throws Exception {
        String assigneeEmail = "emilKotorogoNigdeNet@mail.test";

        mockMvc.perform(delete("/api/tasks/{id}/assignees", tasks.get(0).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("assignee-email", assigneeEmail))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAssignee_WhenAssignEmailInputAndTaskNotFound_ShouldReturnNotFoundStatus() throws Exception {
        String assigneeEmail = users.get(2).getEmail();

        mockMvc.perform(delete("/api/tasks/{id}/assignees", Long.MAX_VALUE)
                        .header("Authorization", "Bearer " + token)
                        .param("assignee-email", assigneeEmail))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAssignee_UnauthorisedRequest_ShouldReturnForbiddenStatus() throws Exception {
        String assigneeEmail = users.get(2).getEmail();

        mockMvc.perform(delete("/api/tasks/{id}/assignees", tasks.get(0).getId())
                        .param("assignee-email", assigneeEmail))
                .andExpect(status().isForbidden());
    }

    @Test
    void addComment_WhenValidCommentTextInput_ShouldReturnOkStatusAndTaskResponseWithNewComment() throws Exception {
        String commentText = "Valid comment";
        int sizeBefore = tasks.get(1).getComments().size();

        String responseContent = mockMvc.perform(post("/api/tasks/{id}/comments", tasks.get(1).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("comment-text", commentText))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().getResponse().getContentAsString();

        TaskResponse response = objectMapper.readValue(responseContent, TaskResponse.class);

        assertNotNull(response);
        assertEquals(sizeBefore + 1, response.getComments().size());
        assertEquals(tasks.get(1).getId(), response.getId());
    }

    @Test
    void addComment_WhenEmptyCommentTextInput_ShouldReturnBadRequestStatus() throws Exception {
        String commentText = null;
        int sizeBefore = tasks.get(1).getComments().size();

        mockMvc.perform(post("/api/tasks/{id}/comments", tasks.get(1).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("comment-text", commentText))
                .andExpect(status().isBadRequest());
        Optional<Task> task = taskRepository.findById(tasks.get(1).getId());

        assertTrue(task.isPresent());
        assertEquals(sizeBefore, task.get().getComments().size());
    }

    @Test
    void addComment_WhenBlankCommentTextInput_ShouldReturnBadRequestStatus() throws Exception {
        String commentText = "";
        int sizeBefore = tasks.get(1).getComments().size();

        mockMvc.perform(post("/api/tasks/{id}/comments", tasks.get(1).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("comment-text", commentText))
                .andExpect(status().isBadRequest());
        Optional<Task> task = taskRepository.findById(tasks.get(1).getId());

        assertTrue(task.isPresent());
        assertEquals(sizeBefore, task.get().getComments().size());
    }

    @Test
    void addComment_UnauthorisedRequest_ShouldReturnForbiddenStatus() throws Exception {
        String commentText = "Valid comment";

        mockMvc.perform(post("/api/tasks/{id}/comments", tasks.get(1).getId())
                        .param("comment-text", commentText))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteComment_WhenValidCommentIdInput_ShouldReturnOkStatusAndTaskResponseWithoutRemoteComment() throws Exception {
        String commentId = String.valueOf(tasks.get(1).getComments().get(2).getId());
        int sizeBefore = tasks.get(1).getComments().size();

        String responseContent = mockMvc.perform(delete("/api/tasks/{id}/comments", tasks.get(1).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("comment-id", commentId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().getResponse().getContentAsString();

        TaskResponse response = objectMapper.readValue(responseContent, TaskResponse.class);

        assertNotNull(response);
        assertEquals(sizeBefore - 1, response.getComments().size());
        assertEquals(tasks.get(1).getId(), response.getId());
    }

    @Test
    void deleteComment_WhenAnotherUserCommentIdInput_ShouldReturnBadRequestStatus() throws Exception {
        String commentId = String.valueOf(tasks.get(1).getComments().get(1).getId());
        int sizeBefore = tasks.get(1).getComments().size();

        mockMvc.perform(delete("/api/tasks/{id}/comments", tasks.get(1).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("comment-id", commentId))
                .andExpect(status().isBadRequest());
        Optional<Task> task = taskRepository.findById(tasks.get(1).getId());

        assertTrue(task.isPresent());
        assertEquals(sizeBefore, task.get().getComments().size());
    }

    @Test
    void deleteComment_WhenEmptyCommentIdInput_ShouldReturnBadRequestStatus() throws Exception {
        String commentId = null;
        int sizeBefore = tasks.get(1).getComments().size();

        mockMvc.perform(delete("/api/tasks/{id}/comments", tasks.get(1).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("comment-id", commentId))
                .andExpect(status().isBadRequest());
        Optional<Task> task = taskRepository.findById(tasks.get(1).getId());

        assertTrue(task.isPresent());
        assertEquals(sizeBefore, task.get().getComments().size());
    }

    @Test
    void deleteComment_WhenBlankCommentIdInput_ShouldReturnBadRequestStatus() throws Exception {
        String commentId = "";
        int sizeBefore = tasks.get(1).getComments().size();

        mockMvc.perform(delete("/api/tasks/{id}/comments", tasks.get(1).getId())
                        .header("Authorization", "Bearer " + token)
                        .param("comment-id", commentId))
                .andExpect(status().isBadRequest());
        Optional<Task> task = taskRepository.findById(tasks.get(1).getId());

        assertTrue(task.isPresent());
        assertEquals(sizeBefore, task.get().getComments().size());
    }

    @Test
    void deleteComment_WhenBlankCommentTextInput_ShouldReturnBadRequestStatus() throws Exception {
        String commentId = String.valueOf(tasks.get(1).getComments().get(2).getId());

        mockMvc.perform(delete("/api/tasks/{id}/comments", tasks.get(1).getId())
                        .param("comment-id", commentId))
                .andExpect(status().isForbidden());
    }
}