#!/usr/bin/env groovy

def call() {
	killJbossProcess()
	runIntegrationTest()
}

def killJbossProcess() {
   sh 'pkill -9 -f jboss | true'
}
   
def runIntegrationTest() {   
    if (env.MVN_PCR_INT) {
        env.MAVEN_COMMAND =  env.MVN_PCR_INT
    } else {
        env.MAVEN_COMMAND = "-V -U jacoco:prepare-agent install jacoco:report pmd:pmd"
    }
    withMaven(jdk: env.JDK_HOME, maven: env.MVN_HOME, options: [junitPublisher(healthScaleFactor: 1.0)]) {
        sh "mvn ${MAVEN_COMMAND}"
    }
}