package com.example.taskmanager.repository;

import com.example.taskmanager.domain.Priority;
import com.example.taskmanager.domain.Task;
import com.example.taskmanager.domain.TaskStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {

    long countByStatus(TaskStatus status);

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByPriority(Priority priority);

    List<Task> findByAssignee(String assignee);

    @Query("""
            SELECT t FROM Task t
            WHERE (:status IS NULL OR t.status = :status)
              AND (:priority IS NULL OR t.priority = :priority)
              AND (:assignee IS NULL OR lower(t.assignee) = lower(:assignee))
              AND (:text IS NULL OR lower(t.title) LIKE lower(concat('%', :text, '%'))
                             OR lower(coalesce(t.description, '')) LIKE lower(concat('%', :text, '%')))
            ORDER BY CASE t.priority
                       WHEN com.example.taskmanager.domain.Priority.HIGH THEN 0
                       WHEN com.example.taskmanager.domain.Priority.MEDIUM THEN 1
                       ELSE 2
                     END,
                     t.dueDate ASC NULLS LAST,
                     t.id DESC
            """)
    List<Task> search(@Param("status") TaskStatus status,
                      @Param("priority") Priority priority,
                      @Param("assignee") String assignee,
                      @Param("text") String text);
}