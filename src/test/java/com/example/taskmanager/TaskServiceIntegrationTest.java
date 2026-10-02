package com.example.taskmanager;

import com.example.taskmanager.domain.Priority;
import com.example.taskmanager.domain.Task;
import com.example.taskmanager.domain.TaskStatus;
import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.service.TaskService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:tasktest;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class TaskServiceIntegrationTest {

    @Autowired
    private TaskService service;

    @Test
    void criaTarefaComDefaults() {
        var task = service.create(new TaskRequest("Escrever relatório", "Detalhe",
                null, null, "thiag", LocalDateTime.now().plusDays(2)));

        assertThat(task.getId()).isNotNull();
        assertThat(task.getStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(task.getPriority()).isEqualTo(Priority.MEDIUM);
        assertThat(task.getTitle()).isEqualTo("Escrever relatório");
    }

    @Test
    void alteraStatusParaConcluida() {
        var task = service.create(new TaskRequest("Revisar PR", null,
                TaskStatus.PENDING, Priority.HIGH, null, null));

        var updated = service.changeStatus(task.getId(), TaskStatus.COMPLETED);

        assertThat(updated.getStatus()).isEqualTo(TaskStatus.COMPLETED);
    }

    @Test
    void filtrarPorStatusETexto() {
        service.create(new TaskRequest("Tarefa alpha", null, TaskStatus.PENDING, null, "ana", null));
        service.create(new TaskRequest("Tarefa beta", null, TaskStatus.COMPLETED, null, "bruno", null));

        List<?> result = service.findAll(TaskStatus.PENDING, null, null, "alpha");

        assertThat(result).hasSize(1);
    }

    @Test
    void ordenaPorPrioridadeSemantica() {
        String tag = "ordem-" + java.util.UUID.randomUUID();
        service.create(new TaskRequest("Baixa", null, null, Priority.LOW, tag, null));
        service.create(new TaskRequest("Alta", null, null, Priority.HIGH, tag, null));
        service.create(new TaskRequest("Media", null, null, Priority.MEDIUM, tag, null));

        List<Task> result = service.findAll(null, null, tag, null);

        assertThat(result.stream().map(Task::getTitle).toList())
                .containsExactly("Alta", "Media", "Baixa");
    }

    @Test
    void tarefaInexistenteLancaErro() {
        assertThatThrownBy(() -> service.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void deletaTarefa() {
        var task = service.create(new TaskRequest("Temporária", null, null, null, null, null));
        service.delete(task.getId());
        assertThatThrownBy(() -> service.findById(task.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}