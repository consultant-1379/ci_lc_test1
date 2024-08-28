#!/usr/bin/env groovy

def call(){
    withSonarQubeEnv(env.SQ_SERVER) {
        withMaven(maven: env.MVN_HOME) {
            sh "mvn -U -V clean jacoco:prepare-agent install sonar:sonar -Dsonar.login=${SONAR_AUTH_TOKEN} -Dsonar.host.url=${SONAR_HOST_URL} -Dsonar.projectVersion=${env.BUILD_VERSION}"
        }
    }
}