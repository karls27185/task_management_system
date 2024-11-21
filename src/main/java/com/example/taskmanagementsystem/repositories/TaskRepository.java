package com.example.taskmanagementsystem.repositories;

import com.example.taskmanagementsystem.models.Task;
import com.example.taskmanagementsystem.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByAuthor(User author);
    List<Task> findAllByAssigneesContains(User assignee);
}
