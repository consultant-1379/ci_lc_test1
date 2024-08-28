#!/usr/bin/env groovy

def call() {
    def pom = readMavenPom file: "${WORKSPACE}/pom.xml"
    return [groupId: pom.groupId, artifactId: pom.artifactId, version: pom.version]
}