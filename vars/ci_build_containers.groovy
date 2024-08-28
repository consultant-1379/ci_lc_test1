#!/usr/bin/env groovy

def call() {
    if (env.COMPOSE_FILE) {
        if (env.COMPOSE_FILE.contains(',')) {
            env.DOCKER_COMPOSE_FILE = env.COMPOSE_FILE.replace(",", " -f ")
        } else {
            env.DOCKER_COMPOSE_FILE = env.COMPOSE_FILE
        }
    } else {
        if (env.TESTWARE_ROOT_DIR) {
            env.DOCKER_COMPOSE_FILE = "./${TESTWARE_ROOT_DIR}/testsuite/docker-compose.yml"
        } else {
            env.DOCKER_COMPOSE_FILE = "./testsuite/docker-compose.yml"
        }
    }

    sh "docker-compose  -f ${DOCKER_COMPOSE_FILE} rm -f"
    sh "docker-compose  -f ${DOCKER_COMPOSE_FILE} pull"
    sh "docker-compose  -f ${DOCKER_COMPOSE_FILE} build --no-cache"
    sh "docker ps"
}