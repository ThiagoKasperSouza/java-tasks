#!/usr/bin/env bash
set -euo pipefail
export JAVA_HOME="$HOME/.sdkman/candidates/java/21.0.12-amzn"
#!/usr/bin/env bash
set -euo pipefail
export JAVA_HOME="$HOME/.sdkman/candidates/java/21.0.12-amzn"
export PATH="$JAVA_HOME/bin:$HOME/.m2/wrapper/dists/apache-maven-3.9.16/56ba1f9f/bin:$PATH"
cd /home/thiag/java-example
JAR=target/task-manager-1.0.0.jar

if [ ! -f "$JAR" ]; then
  mvn -B -q package -DskipTests
fi

case "${1:-run}" in
  run)
    exec java -jar "$JAR"
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