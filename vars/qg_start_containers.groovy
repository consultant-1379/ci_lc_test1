#!/usr/bin/env groovy

def call(args) {

    def fileArg = ""
    if (args.composeFile) fileArg = " -f ${args.composeFile}"
    if (!args.timeout) args.timeout = 200
    sh "export COMPOSE_HTTP_TIMEOUT=${args.timeout}; " +
            "docker-compose ${fileArg} up --force-recreate -d"

}
