package com.example.taskmanager.dto;

import com.example.taskmanager.domain.Priority;
import com.example.taskmanager.domain.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record TaskRequest(
        @NotBlank(message = "title é obrigatório")
        @Size(max = 200, message = "title deve ter no máximo 200 caracteres")
        String title,

        @Size(max = 1000, message = "description deve ter no máximo 1000 caracteres")
        String description,

        TaskStatus status,

        Priority priority,

        @Size(max = 100, message = "assignee deve ter no máximo 100 caracteres")
        String assignee,

        LocalDateTime dueDate) {
}