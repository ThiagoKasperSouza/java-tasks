package com.example.taskmanager.service;

import com.example.taskmanager.domain.Priority;
import com.example.taskmanager.domain.Task;
import com.example.taskmanager.domain.TaskStatus;
import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.repository.TaskRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Task create(TaskRequest request) {
        Task task = new Task(
                request.title().trim(),
                request.description(),
                request.status() == null ? TaskStatus.PENDING : request.status(),
                request.priority() == null ? Priority.MEDIUM : request.priority(),
                request.assignee(),
                request.dueDate());
        return repository.save(task);
    }

    @Transactional(readOnly = true)
    public List<Task> findAll(TaskStatus status, Priority priority, String assignee, String text) {
        return repository.search(
                status,
                priority,
                (assignee == null || assignee.isBlank()) ? null : assignee.trim(),
                (text == null || text.isBlank()) ? null : text.trim());
    }

    @Transactional(readOnly = true)
    public Task findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa " + id + " não encontrada"));
    }

    @Transactional
    public Task update(Long id, TaskRequest request) {
        Task task = findById(id);
        task.setTitle(request.title().trim());
        task.setDescription(request.description());
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        if (request.priority() != null) {
            task.setPriority(request.priority());
        }
        task.setAssignee(request.assignee());
        task.setDueDate(request.dueDate());
        return repository.save(task);
    }

    @Transactional
    public Task changeStatus(Long id, TaskStatus status) {
        Task task = findById(id);
        task.setStatus(status);
        return repository.save(task);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Tarefa " + id + " não encontrada");
        }
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long countByStatus(TaskStatus status) {
        return repository.countByStatus(status);
    }
}