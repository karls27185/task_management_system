package com.example.taskmanagementsystem.services;

import com.example.taskmanagementsystem.dto.comment.CommentDto;
import com.example.taskmanagementsystem.dto.task.TaskDto;
import com.example.taskmanagementsystem.models.Task;
import com.example.taskmanagementsystem.models.User;

import java.util.List;

public interface CommentService {
    CommentDto findCommentById(Long id);
    CommentDto createComment(CommentDto commentDto);
    void deleteCommentById(Long id, User commentatorOrTaskAuthor);
    Task deleteAllCommentsInTask(Task task);
    CommentDto updateText(Long id, String text, User commentator);
}
