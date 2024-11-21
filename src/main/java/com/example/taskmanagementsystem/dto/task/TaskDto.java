package com.example.taskmanagementsystem.dto.task;

import com.example.taskmanagementsystem.models.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskDto {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private User author;
    private List<User> assignees = new ArrayList<>();
    private List<Comment> comments = new ArrayList<>();
}
