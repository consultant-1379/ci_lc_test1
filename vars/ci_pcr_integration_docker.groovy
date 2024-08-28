#!/usr/bin/env groovy

def call() {
    runDockerIntegrationTest()
}

def runDockerIntegrationTest() {
    if (env.MVN_DOCKER_INT) {
        env.MAVEN_COMMAND = env.MVN_DOCKER_INT
    } else {
        if (env.TESTWARE_ROOT_DIR) {
            env.MAVEN_COMMAND = "-f ${TESTWARE_ROOT_DIR}/pom.xml -V -U install -Pdocker"
        } else {
            env.MAVEN_COMMAND = "-V -U install -Pdocker"
        }
    }
    withMaven(jdk: env.JDK_HOME, maven: env.MVN_HOME, options: [junitPublisher(healthScaleFactor: 1.0)]) {
        sh "mvn ${MAVEN_COMMAND}"
    }
}