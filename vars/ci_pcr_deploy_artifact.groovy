#!/usr/bin/env groovy

def call() {
    deployArtifacts()
}

def deployArtifacts() {
    if (env.MVN_DEPLOY) {
        env.MAVEN_COMMAND = env.MVN_DEPLOY
    } else {
        if (env.TESTWARE_ROOT_DIR) {
            env.MAVEN_COMMAND = "-f ${TESTWARE_ROOT_DIR}/pom.xml -V -U install -Pdeploy"
        } else {
            env.MAVEN_COMMAND = "-V -U install -Pdeploy"
        }
    }
    withMaven(jdk: env.JDK_HOME, maven: env.MVN_HOME, options: [junitPublisher(healthScaleFactor: 1.0)]) {
        sh "mvn ${MAVEN_COMMAND}"
    }
}