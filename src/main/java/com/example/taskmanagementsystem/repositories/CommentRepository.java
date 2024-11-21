package com.example.taskmanagementsystem.repositories;

import com.example.taskmanagementsystem.models.Comment;
import com.example.taskmanagementsystem.models.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    void deleteAllByTask(Task task);
}
