#!/usr/bin/env bash
set -euo pipefail
export JAVA_HOME="$HOME/.sdkman/candidates/java/21.0.12-amzn"
#!/usr/bin/env bash
set -euo pipefail
export JAVA_HOME="$HOME/.sdkman/candidates/java/21.0.12-amzn"
export PATH="$JAVA_HOME/bin:$HOME/.m2/wrapper/dists/apache-maven-3.9.16/56ba1f9f/bin:$PATH"
cd /home/thiag/java-example
JAR=target/task-manager-1.0.0.jar
PIDFILE=data/app.pid

running_pid() {
  pgrep -f "java -jar $JAR" 2>/dev/null | head -1 || true
}

case "${1:-run}" in
  run)
    PID=$(running_pid)
    if [ -n "$PID" ]; then
      echo "ERRO: a aplicacao ja esta rodando (PID $PID)."
      echo "Use 'bash run.sh stop' antes, ou 'bash run.sh stop && bash run.sh run'."
      exit 1
    fi
    if [ ! -f "$JAR" ]; then
      mvn -B -q package -DskipTests
    fi
    mkdir -p data
    exec java -jar "$JAR"
    ;;
  start)
    PID=$(running_pid)
    if [ -n "$PID" ]; then
      echo "A aplicacao ja esta rodando (PID $PID)."
      exit 0
    fi
    if [ ! -f "$JAR" ]; then
      mvn -B -q package -DskipTests
    fi
    mkdir -p data
    nohup java -jar "$JAR" > /tmp/task-manager.log 2>&1 &
    echo $! > "$PIDFILE"
    echo "Iniciada (PID $!). Log: /tmp/task-manager.log"
    ;;
  stop)
    PID=$(running_pid)
    if [ -z "$PID" ]; then
      echo "Nada rodando."
      rm -f "$PIDFILE"
      exit 0
    fi
    echo "Encerrando PID $PID..."
    kill "$PID"
    for _ in $(seq 1 30); do
      sleep 1
      kill -0 "$PID" 2>/dev/null || break
    done
    kill -0 "$PID" 2>/dev/null && kill -9 "$PID" 2>/dev/null || true
    rm -f "$PIDFILE"
    echo "Encerrada."
    ;;
  status)
    PID=$(running_pid)
    if [ -n "$PID" ]; then
      echo "Rodando (PID $PID) em http://localhost:8080"
    else
      echo "Parada."
    fi
    ;;
  reset)
    bash "$0" stop >/dev/null 2>&1 || true
    rm -rf data
    echo "Banco de dados removido (data/)."
    ;;
  test)
    shift
    exec mvn -B test "$@"
    ;;
  build)
    shift
    exec mvn -B package "$@"
    ;;
  *)
    exec "$@"
    ;;
esac