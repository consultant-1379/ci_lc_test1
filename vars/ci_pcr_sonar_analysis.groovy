#!/usr/bin/env groovy

def call() {
    withSonarQubeEnv(env.SQ_SERVER) {
        withMaven(jdk: env.JDK_HOME, maven: env.MVN_HOME) {
            sh "mvn ${env.MVN_SONAR}"
        }
    }
}
