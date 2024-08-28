#!/usr/bin/env groovy

def call() {

    sh 'docker kill \$(docker ps -a -q) || true'
    sh 'docker rm -f \$(docker ps -a -q) || true'
    sh "docker ps -a"

}
