package com.example.taskmanagementsystem.controllers;

import com.example.taskmanagementsystem.dto.task.TaskDtoConverter;
import com.example.taskmanagementsystem.dto.task.TaskRequest;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@Transactional
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TaskDtoConverter taskDtoConverter;

    @Autowired
    private UserRepository userRepository;

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
    private TaskRequest taskRequest;


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
        this.taskRequest = createTaskRequest();
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

    private List<User> createUsers(){
        String password = passwordEncoder.encode("Password");
        return userRepository.saveAll(List.of(
                User.builder().name("maksim1").email("maksim1@mail.test").password(password).build(),
                User.builder().name("maksim2").email("maksim2@mail.test").password(password).build(),
                User.builder().name("maksim3").email("maksim3@mail.test").password(password).build()
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
                        .assignees(new ArrayList<>(List.of(users.get(0), users.get(2))))
                        .comments(new ArrayList<>())
                        .build()
        ));
    }

    private TaskRequest createTaskRequest(){
        return TaskRequest.builder()
                .title("TestTask")
                .description("testing task")
                .statusValue(1)
                .priorityValue(2)
                .assigneesEmail(List.of(users.get(1).getEmail()))
                .assigneesId(List.of(users.get(2).getId()))
                .build();
    }

    @Test
    void getAllTasks_ShouldReturnListOfTaskResponses() throws Exception {
        List<TaskResponse> taskResponses = tasks.stream()
                .map(taskDtoConverter::convertEntityToDto)
                .map(taskDtoConverter::convertDtoToResponse)
                .toList();

        mockMvc.perform(get("/api/tasks/")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(taskResponses)));

    }

    @Test
    void getAllTasks_UnauthorisedRequest_ShouldReturnForbiddenStatus() throws Exception {
        mockMvc.perform(get("/api/tasks/"))
                .andExpect(status().isForbidden());

    }

    @Test
    void getTaskById_WhenTaskFound_ShouldTaskResponse() throws Exception {
        TaskResponse taskResponse = taskDtoConverter.convertDtoToResponse(taskDtoConverter.convertEntityToDto(tasks.get(0)));

        mockMvc.perform(get("/api/tasks/{id}", tasks.get(0).getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(taskResponse)));

    }

    @Test
    void getTaskById_WhenTaskNotFound_ShouldReturnNotFoundStatus() throws Exception {
        long id = Long.MAX_VALUE;

        mockMvc.perform(get("/api/tasks/{id}", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTaskById_UnauthorisedRequest_ShouldReturnForbiddenStatus() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}", tasks.get(0)))
                .andExpect(status().isForbidden());
    }

    @Test
    void addTask_WhenTaskRequestWithValidData_ShouldReturnCreatedStatusAndTaskResponse() throws Exception {
        String responseContent  = mockMvc.perform(post("/api/tasks/")
                        .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        TaskResponse response = objectMapper.readValue(responseContent, TaskResponse.class);

        Optional<Task> createdTask = taskRepository.findById(response.getId());

        assertTrue(createdTask.isPresent());
        assertEquals(taskRequest.getTitle(), createdTask.get().getTitle());
        assertEquals(taskRequest.getDescription(), createdTask.get().getDescription());
        assertEquals(taskRequest.getStatusValue(), createdTask.get().getStatus().getValue());
        assertEquals(taskRequest.getPriorityValue(), createdTask.get().getPriority().getValue());
        assertEquals(users.get(0), createdTask.get().getAuthor());
        assertTrue(createdTask.get().getAssignees().contains(users.get(1)));
        assertTrue(createdTask.get().getAssignees().contains(users.get(2)));
        assertEquals(2, createdTask.get().getAssignees().size());
    }

    @Test
    void addTask_UnauthorisedRequest_ShouldReturnForbiddenStatus() throws Exception {
        mockMvc.perform(post("/api/tasks/")
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void addTask_WhenTaskRequestWithoutTitle_ShouldReturnBadRequestStatus() throws Exception {
        taskRequest.setTitle(null);

        mockMvc.perform(post("/api/tasks/")
                        .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addTask_WhenTaskRequestWithBlankTitle_ShouldReturnBadRequestStatus() throws Exception {
        taskRequest.setTitle("");

        mockMvc.perform(post("/api/tasks/")
                        .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addTask_WhenTaskRequestWithoutDescription_ShouldReturnCreatedStatusAndTaskResponse() throws Exception {
        taskRequest.setDescription(null);

        String responseContent  = mockMvc.perform(post("/api/tasks/")
                        .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        TaskResponse response = objectMapper.readValue(responseContent, TaskResponse.class);

        Optional<Task> createdTask = taskRepository.findById(response.getId());

        assertTrue(createdTask.isPresent());
        assertEquals(taskRequest.getTitle(), createdTask.get().getTitle());
        assertEquals("", createdTask.get().getDescription());
        assertEquals(taskRequest.getStatusValue(), createdTask.get().getStatus().getValue());
        assertEquals(taskRequest.getPriorityValue(), createdTask.get().getPriority().getValue());
        assertEquals(users.get(0), createdTask.get().getAuthor());
        assertTrue(createdTask.get().getAssignees().contains(users.get(1)));
        assertTrue(createdTask.get().getAssignees().contains(users.get(2)));
        assertEquals(2, createdTask.get().getAssignees().size());
    }

    @Test
    void addTask_WhenTaskRequestWithInvalidStatus_ShouldReturnBadRequestStatus() throws Exception {
        taskRequest.setStatusValue(5);

        mockMvc.perform(post("/api/tasks/")
                        .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addTask_WhenTaskRequestWithoutStatus_ShouldReturnCreatedStatusAndTaskResponse() throws Exception {
        taskRequest.setStatusValue(null);

        String responseContent  = mockMvc.perform(post("/api/tasks/")
                        .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        TaskResponse response = objectMapper.readValue(responseContent, TaskResponse.class);

        Optional<Task> createdTask = taskRepository.findById(response.getId());

        assertTrue(createdTask.isPresent());
        assertEquals(taskRequest.getTitle(), createdTask.get().getTitle());
        assertEquals(taskRequest.getDescription(), createdTask.get().getDescription());
        assertEquals(TaskStatus.PENDING, createdTask.get().getStatus());
        assertEquals(taskRequest.getPriorityValue(), createdTask.get().getPriority().getValue());
        assertEquals(users.get(0), createdTask.get().getAuthor());
        assertTrue(createdTask.get().getAssignees().contains(users.get(1)));
        assertTrue(createdTask.get().getAssignees().contains(users.get(2)));
        assertEquals(2, createdTask.get().getAssignees().size());
    }

    @Test
    void addTask_WhenTaskRequestWithInvalidPriority_ShouldReturnBadRequestStatus() throws Exception {
        taskRequest.setPriorityValue(5);

        mockMvc.perform(post("/api/tasks/")
                        .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addTask_WhenTaskRequestWithoutPriority_ShouldReturnCreatedStatusAndTaskResponse() throws Exception {
        taskRequest.setPriorityValue(null);

        String responseContent  = mockMvc.perform(post("/api/tasks/")
                        .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        TaskResponse response = objectMapper.readValue(responseContent, TaskResponse.class);

        Optional<Task> createdTask = taskRepository.findById(response.getId());

        assertTrue(createdTask.isPresent());
        assertEquals(taskRequest.getTitle(), createdTask.get().getTitle());
        assertEquals(taskRequest.getDescription(), createdTask.get().getDescription());
        assertEquals(taskRequest.getStatusValue(), createdTask.get().getStatus().getValue());
        assertEquals(TaskPriority.LOW, createdTask.get().getPriority());
        assertEquals(users.get(0), createdTask.get().getAuthor());
        assertTrue(createdTask.get().getAssignees().contains(users.get(1)));
        assertTrue(createdTask.get().getAssignees().contains(users.get(2)));
        assertEquals(2, createdTask.get().getAssignees().size());
    }

    @Test
    void addTask_WhenTaskRequestWithoutAssigneesEmail_ShouldReturnCreatedStatusAndTaskResponse() throws Exception {
        taskRequest.setAssigneesEmail(null);
        taskRequest.setAssigneesId(List.of(users.get(1).getId(), users.get(2).getId()));

        String responseContent  = mockMvc.perform(post("/api/tasks/")
                        .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        TaskResponse response = objectMapper.readValue(responseContent, TaskResponse.class);

        Optional<Task> createdTask = taskRepository.findById(response.getId());

        assertTrue(createdTask.isPresent());
        assertEquals(taskRequest.getTitle(), createdTask.get().getTitle());
        assertEquals(taskRequest.getDescription(), createdTask.get().getDescription());
        assertEquals(taskRequest.getStatusValue(), createdTask.get().getStatus().getValue());
        assertEquals(taskRequest.getPriorityValue(), createdTask.get().getPriority().getValue());
        assertEquals(users.get(0), createdTask.get().getAuthor());
        assertTrue(createdTask.get().getAssignees().contains(users.get(1)));
        assertTrue(createdTask.get().getAssignees().contains(users.get(2)));
        assertEquals(2, createdTask.get().getAssignees().size());
    }

    @Test
    void addTask_WhenTaskRequestWithoutAssigneesId_ShouldReturnCreatedStatusAndTaskResponse() throws Exception {
        taskRequest.setAssigneesEmail(List.of(users.get(1).getEmail(), users.get(2).getEmail()));
        taskRequest.setAssigneesId(null);

        String responseContent  = mockMvc.perform(post("/api/tasks/")
                        .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        TaskResponse response = objectMapper.readValue(responseContent, TaskResponse.class);

        Optional<Task> createdTask = taskRepository.findById(response.getId());

        assertTrue(createdTask.isPresent());
        assertEquals(taskRequest.getTitle(), createdTask.get().getTitle());
        assertEquals(taskRequest.getDescription(), createdTask.get().getDescription());
        assertEquals(taskRequest.getStatusValue(), createdTask.get().getStatus().getValue());
        assertEquals(taskRequest.getPriorityValue(), createdTask.get().getPriority().getValue());
        assertEquals(users.get(0), createdTask.get().getAuthor());
        assertTrue(createdTask.get().getAssignees().contains(users.get(1)));
        assertTrue(createdTask.get().getAssignees().contains(users.get(2)));
        assertEquals(2, createdTask.get().getAssignees().size());
    }

    @Test
    void addTask_WhenSameUsersSpecifiedInAssigneesIdAndAssigneesEmail_ShouldReturnCreatedStatusAndTaskResponse() throws Exception {
        taskRequest.setAssigneesId(List.of(users.get(1).getId(), users.get(2).getId()));
        taskRequest.setAssigneesEmail(List.of(users.get(1).getEmail(), users.get(2).getEmail()));

        String responseContent  = mockMvc.perform(post("/api/tasks/")
                        .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        TaskResponse response = objectMapper.readValue(responseContent, TaskResponse.class);

        Optional<Task> createdTask = taskRepository.findById(response.getId());

        assertTrue(createdTask.isPresent());
        assertEquals(taskRequest.getTitle(), createdTask.get().getTitle());
        assertEquals(taskRequest.getDescription(), createdTask.get().getDescription());
        assertEquals(taskRequest.getStatusValue(), createdTask.get().getStatus().getValue());
        assertEquals(taskRequest.getPriorityValue(), createdTask.get().getPriority().getValue());
        assertEquals(users.get(0), createdTask.get().getAuthor());
        assertTrue(createdTask.get().getAssignees().contains(users.get(1)));
        assertTrue(createdTask.get().getAssignees().contains(users.get(2)));
        assertEquals(2, createdTask.get().getAssignees().size());
    }

    @Test
    void addTask_WhenAssigneesIdContainsNonExistentUser_ShouldReturnNotFoundStatus() throws Exception {
        taskRequest.setAssigneesId(List.of(users.get(2).getId(), Long.MAX_VALUE));

        mockMvc.perform(post("/api/tasks/")
                        .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addTask_WhenAssigneesEmailContainsNonExistentUser_ShouldReturnNotFoundStatus() throws Exception {
        taskRequest.setAssigneesEmail(List.of(users.get(1).getEmail(), "TEst NonExistent USer Email"));

        mockMvc.perform(post("/api/tasks/")
                        .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addTask_WhenTaskRequestWithoutAssignees_ShouldReturnCreatedStatusAndTaskResponse() throws Exception {
        taskRequest.setAssigneesEmail(null);
        taskRequest.setAssigneesId(null);

        String responseContent  = mockMvc.perform(post("/api/tasks/")
                        .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        TaskResponse response = objectMapper.readValue(responseContent, TaskResponse.class);

        Optional<Task> createdTask = taskRepository.findById(response.getId());

        assertTrue(createdTask.isPresent());
        assertEquals(taskRequest.getTitle(), createdTask.get().getTitle());
        assertEquals(taskRequest.getDescription(), createdTask.get().getDescription());
        assertEquals(taskRequest.getStatusValue(), createdTask.get().getStatus().getValue());
        assertEquals(taskRequest.getPriorityValue(), createdTask.get().getPriority().getValue());
        assertEquals(users.get(0), createdTask.get().getAuthor());
        assertEquals(0, createdTask.get().getAssignees().size());
    }

    @Test
    void deleteTask_ShouldReturnOkStatus () throws Exception {
        mockMvc.perform(delete("/api/tasks/{id}", tasks.get(0).getId())
                .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertEquals(tasks.size() - 1, taskRepository.findAll().size());
    }

    @Test
    void deleteTask_UnauthorisedRequest_ShouldReturnForbiddenStatus () throws Exception {
        mockMvc.perform(delete("/api/tasks/{id}", tasks.get(0).getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteTask_WhenDeleteAnotherUserTask_ShouldReturnBadRequestStatus () throws Exception {
        mockMvc.perform(delete("/api/tasks/{id}", tasks.get(1).getId())
                        .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteTask_WhenTaskNotFound_ShouldReturnNotFoundStatus () throws Exception {
        mockMvc.perform(delete("/api/tasks/{id}", Long.MAX_VALUE)
                .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}