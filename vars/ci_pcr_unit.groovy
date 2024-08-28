#!/usr/bin/env groovy

def call() {
    if (env.MVN_PCR) {
                  env.MAVEN_COMMAND =  env.MVN_PCR
    } else {
                  env.MAVEN_COMMAND = "-V -U jacoco:prepare-agent install jacoco:report pmd:pmd"
    }
    withMaven(jdk: env.JDK_HOME, maven: env.MVN_HOME, options: [junitPublisher(healthScaleFactor: 1.0)]) {
        sh "mvn ${MAVEN_COMMAND}"
    }
}

