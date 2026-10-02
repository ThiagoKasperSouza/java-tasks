# Task Manager — Spring Boot 3

Sistema de gerenciamento de tarefas (CRUD + filtros + status) em Java 21, rodando no WSL Ubuntu.

## Stack

- Java 21 (SDKMAN: `21.0.12-amzn`)
- Spring Boot 3.3.5 (Web, Validation, Data JPA)
- H2 em arquivo (`./data/taskdb.mv.db`) — dados persistem entre execuções
- JUnit 5 + MockMvc
- Maven 3.9.16

## Como rodar

```bash
cd /home/thiag/java-example

bash run.sh build      # compila e gera target/task-manager-1.0.0.jar
bash run.sh test       # roda os testes
bash run.sh start      # sobe em background (log: /tmp/task-manager.log)
bash run.sh status     # mostra se está rodando
bash run.sh stop       # encerra
bash run.sh reset      # encerra e apaga o banco (data/)
bash run.sh run        # sobe em primeiro plano (Ctrl+C para sair)
```

### Só uma instância por vez

O H2 em arquivo usa lock exclusivo. Para evitar o erro
`Database may be already in use`, o `run.sh` **recusa** uma segunda instância:

```
$ bash run.sh run
ERRO: a aplicacao ja esta rodando (PID 1071303).
Use 'bash run.sh stop' antes, ou 'bash run.sh stop && bash run.sh run'.
```

Se precisar de duas JVMs ao mesmo tempo (ex.: app + console H2), use o modo
servidor do H2, já habilitado na configuração:

```bash
java -jar target/task-manager-1.0.0.jar \
  --server.port=8081 \
  --spring.datasource.url='jdbc:h2:file:./data/taskdb;AUTO_SERVER=TRUE'
```

Nessa situação ambas enxergam os mesmos dados (verificado: escrita na porta 8081
aparece na leitura da porta 8080).

## Modelo

| Campo | Tipo | Observações |
|---|---|---|
| `id` | Long | gerado pelo banco |
| `title` | String | obrigatório, máx. 200 |
| `description` | String | máx. 1000 |
| `status` | `PENDING` / `IN_PROGRESS` / `COMPLETED` | padrão `PENDING` |
| `priority` | `LOW` / `MEDIUM` / `HIGH` | padrão `MEDIUM` |
| `assignee` | String | responsável |
| `dueDate` | ISO-8601 | ex.: `2026-10-05T18:00:00` |
| `overdue` | boolean | calculado: prazo vencido e não concluída |
| `createdAt` / `updatedAt` | ISO-8601 | gerenciados pelo JPA |

## Endpoints

Base: `http://localhost:8080/api/tasks`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/` | Cria tarefa → `201` |
| `GET` | `/` | Lista com filtros `status`, `priority`, `assignee`, `q` |
| `GET` | `/{id}` | Busca por id → `404` se não existir |
| `PUT` | `/{id}` | Atualização completa |
| `PATCH` | `/{id}/status?status=COMPLETED` | Muda apenas o status |
| `DELETE` | `/{id}` | Remove → `204` |
| `GET` | `/summary` | Contagem por status |

Listagem vem ordenada por prioridade (HIGH → LOW), depois por prazo (mais antigo primeiro).

### Exemplos

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H 'Content-Type: application/json' \
  -d '{"title":"Revisar contrato","priority":"HIGH","assignee":"thiag"}'

curl 'http://localhost:8080/api/tasks?status=PENDING&assignee=thiag'
curl -X PATCH 'http://localhost:8080/api/tasks/1/status?status=COMPLETED'
```

Teste rápido de todos os endpoints: `bash smoke-test.sh` (com a aplicação no ar).

### Erros

Formato único:

```json
{
  "timestamp": "2026-10-02T19:20:43",
  "status": 400,
  "error": "Bad Request",
  "message": "Dados inválidos",
  "fields": { "title": "title é obrigatório" }
}
```

## Testes

9 testes cobrindo criação com defaults, troca de status, filtros, ordenação por prioridade,
remoção, fluxo completo da API e validação de entrada.

## Estrutura

```
src/main/java/com/example/taskmanager/
├── TaskManagerApplication.java
├── domain/      Task, TaskStatus, Priority
├── dto/         TaskRequest, TaskResponse
├── repository/  TaskRepository (Spring Data JPA)
├── service/     TaskService
├── web/         TaskController, ApiExceptionHandler
└── exception/   ResourceNotFoundException, BusinessRuleException
```

## Notas

- Console H2 em `http://localhost:8080/h2-console`. Use a URL **com** `AUTO_SERVER=TRUE`
  (`jdbc:h2:file:./data/taskdb;AUTO_SERVER=TRUE`, user `sa`, senha vazia) — sem isso o
  console falha com `Database may be already in use` enquanto o app estiver rodando.
- Para zerar os dados: `bash run.sh reset` (ou apague a pasta `data/`).
- O `run.sh` fixa `JAVA_HOME` e o PATH do Maven porque o `java` do PATH do WSL é o do
  Windows e não há `mvn` instalado.
- `smoke-test.sh` é idempotente: cria e remove as próprias tarefas, e a parte de
  concorrência exige uma segunda instância na porta 8081.