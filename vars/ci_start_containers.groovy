#!/usr/bin/env groovy

def call() {
    sh "docker-compose  -f ${DOCKER_COMPOSE_FILE} up --force-recreate -d"
    sh "docker ps"
}

