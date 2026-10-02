#!/usr/bin/env bash
B=http://localhost:8080/api/tasks
echo "== CREATE =="
curl -s -X POST $B -H 'Content-Type: application/json' -d '{"title":"Revisar contrato","description":"Com financeiro","priority":"HIGH","assignee":"thiag","dueDate":"2026-10-05T18:00:00"}'
echo
curl -s -X POST $B -H 'Content-Type: application/json' -d '{"title":"Atualizar dependencias","priority":"LOW"}'
echo
echo "== LIST =="
curl -s $B
echo
echo "== FILTER status=PENDING =="
curl -s "$B?status=PENDING"
echo
echo "== PATCH status =="
PATCH_ID=$(curl -s -X POST $B -H 'Content-Type: application/json' \
  -d '{"title":"Tarefa para PATCH","priority":"MEDIUM"}' \
  | python3 -c 'import sys,json; print(json.load(sys.stdin)["id"])')
curl -s -X PATCH "$B/$PATCH_ID/status?status=IN_PROGRESS"
echo
curl -s -X PATCH "$B/$PATCH_ID/status?status=COMPLETED"
echo
echo "== VALIDATION (title vazio) =="
curl -s -X POST $B -H 'Content-Type: application/json' -d '{"title":""}'
echo
echo "== 404 =="
curl -s $B/999
echo
echo "== SUMMARY =="
curl -s $B/summary
echo
echo "== DELETE (id extraido da criacao) =="
NEW_ID=$(curl -s -X POST $B -H 'Content-Type: application/json' \
  -d '{"title":"Tarefa temporaria"}' | python3 -c 'import sys,json; print(json.load(sys.stdin)["id"])')
curl -s -o /dev/null -w "delete id=$NEW_ID -> status=%{http_code}\n" -X DELETE $B/$NEW_ID
curl -s -o /dev/null -w 're-delete -> status=%{http_code}\n' -X DELETE $B/$NEW_ID
curl -s -o /dev/null -w 'delete inexistente -> status=%{http_code}\n' -X DELETE $B/999999
echo
echo "== CONCORRENCIA: escrita na porta 8081, leitura na 8080 =="
curl -s -X POST http://localhost:8081/api/tasks -H 'Content-Type: application/json' \
  -d '{"title":"Escrita via 2a JVM","priority":"HIGH"}'
echo
echo "-- leitura na 8080 (mesmo arquivo de banco) --"
curl -s http://localhost:8080/api/tasks | python3 -c 'import sys,json; print([t["title"] for t in json.load(sys.stdin)])'
echo