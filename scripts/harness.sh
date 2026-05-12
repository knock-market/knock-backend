#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_DIR="${ROOT_DIR}/knock-backend"
DOCS_DIR="${ROOT_DIR}/docs"
COMPOSE_FILE="${BACKEND_DIR}/docker/local/docker-compose.yml"

usage() {
  cat <<'USAGE'
Usage: ./scripts/harness.sh <command>

Commands:
  doctor             Check required tools and workspace axes.
  verify             Run the default harness quality gates.
  deps:up            Start local dependencies from backend docker-compose.
  deps:down          Stop local dependencies.
  backend:run        Run the Spring Boot API.
  backend:test       Run backend tests.
  backend:unit       Run backend unit tests.
  backend:context    Run backend context tests.
  backend:restdocs   Run backend REST Docs tests.
  docs:serve         Serve docs at http://localhost:8088.
  frontend:install   Install frontend dependencies when knock-frontend exists.
  frontend:run       Run frontend dev server when knock-frontend exists.
  frontend:test      Run frontend tests when knock-frontend exists.
USAGE
}

log() {
  printf '[harness] %s\n' "$*"
}

has_cmd() {
  command -v "$1" >/dev/null 2>&1
}

frontend_dir() {
  local candidate
  for candidate in "${ROOT_DIR}/knock-frontend" "${ROOT_DIR}/../knock-frontend"; do
    if [[ -d "$candidate" ]]; then
      (cd "$candidate" && pwd)
      return 0
    fi
  done
  return 1
}

docker_compose() {
  if has_cmd docker && docker compose version >/dev/null 2>&1; then
    docker compose -f "$COMPOSE_FILE" "$@"
  elif has_cmd docker-compose; then
    docker-compose -f "$COMPOSE_FILE" "$@"
  else
    log "docker compose is not installed."
    return 1
  fi
}

run_backend() {
  local task="$1"
  if [[ ! -x "${BACKEND_DIR}/gradlew" ]]; then
    log "Backend Gradle wrapper is missing or not executable: ${BACKEND_DIR}/gradlew"
    return 1
  fi
  (cd "$BACKEND_DIR" && ./gradlew "$task")
}

run_frontend() {
  local command_name="$1"
  local dir
  if ! dir="$(frontend_dir)"; then
    log "Skipping frontend:${command_name}; knock-frontend was not found."
    return 0
  fi

  case "$command_name" in
    install) (cd "$dir" && npm install) ;;
    run) (cd "$dir" && npm run dev) ;;
    test) (cd "$dir" && npm test) ;;
    *)
      log "Unknown frontend command: ${command_name}"
      return 2
      ;;
  esac
}

doctor() {
  local failed=0

  [[ -d "$BACKEND_DIR" ]] && log "backend: ${BACKEND_DIR}" || { log "missing backend directory"; failed=1; }
  [[ -d "$DOCS_DIR" ]] && log "docs: ${DOCS_DIR}" || { log "missing docs directory"; failed=1; }

  if dir="$(frontend_dir)"; then
    log "frontend: ${dir}"
  else
    log "frontend: optional, not present"
  fi

  for tool in java docker; do
    if has_cmd "$tool"; then
      log "${tool}: $(command -v "$tool")"
    else
      log "${tool}: not found"
      [[ "$tool" == "java" ]] && failed=1
    fi
  done

  if [[ -f "$COMPOSE_FILE" ]]; then
    log "compose: ${COMPOSE_FILE}"
  else
    log "compose file missing: ${COMPOSE_FILE}"
  fi

  return "$failed"
}

verify() {
  doctor
  run_backend unitTest
  run_frontend test
}

cmd="${1:-}"
case "$cmd" in
  doctor) doctor ;;
  verify) verify ;;
  deps:up) docker_compose up -d ;;
  deps:down) docker_compose down ;;
  backend:run) run_backend :core:core-api:bootRun ;;
  backend:test) run_backend test ;;
  backend:unit) run_backend unitTest ;;
  backend:context) run_backend contextTest ;;
  backend:restdocs) run_backend restDocsTest ;;
  docs:serve) (cd "$ROOT_DIR" && python3 -m http.server 8088 --directory docs) ;;
  frontend:install) run_frontend install ;;
  frontend:run) run_frontend run ;;
  frontend:test) run_frontend test ;;
  -h|--help|help|"") usage ;;
  *)
    usage
    log "Unknown command: ${cmd}"
    exit 2
    ;;
esac
