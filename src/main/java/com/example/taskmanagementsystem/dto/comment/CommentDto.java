package com.example.taskmanagementsystem.dto.comment;

import com.example.taskmanagementsystem.models.Task;
import com.example.taskmanagementsystem.models.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentDto {
    private Long id;
    private String text;
    private LocalDateTime dateTime;
    private User commentator;
    private Task task;
}
