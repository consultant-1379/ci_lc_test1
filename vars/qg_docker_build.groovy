#!/usr/bin/env groovy

def call(args = null) {

    def fileArg = ""
    if (args?.composeFile) fileArg = " -f ${args.composeFile}"
    sh "docker-compose ${fileArg} pull"
    sh "docker-compose ${fileArg} build --pull"

}