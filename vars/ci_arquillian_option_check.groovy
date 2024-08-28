#!/usr/bin/env groovy

/*
if user inserts a new parameter in ARQUILLIAN_CUCUMBER_OPTIONS field,
it will be added to its list of default values and it will be the first choice.
*/
def call() {
    env.jenkins_instance = DOMAIN_ID.tokenize('.').last()
    env.femToken = getApiToken(jenkins_instance)
    if (!env.ARQUILLIAN_CUCUMBER_OPTIONS_LIST.contains(env.ARQUILLIAN_CUCUMBER_OPTIONS + '\n')) {
        println "Add new ARQUILLIAN_CUCUMBER_OPTIONS parameter to the pipeline_local.cfg file."
        sh "sed -i 's/ARQUILLIAN_CUCUMBER_OPTIONS_LIST=/ARQUILLIAN_CUCUMBER_OPTIONS_LIST=${ARQUILLIAN_CUCUMBER_OPTIONS}\\\\n/g' pipeline_local.cfg"
        sh '''
            #!/bin/bash
            git add pipeline_local.cfg
            git commit -m "No Jira: Add new ARQUILLIAN_CUCUMBER_OPTIONS tag to the pipeline_local.cfg file."
            git push ${GERRIT_CENTRAL}/${REPO} HEAD:master
            curl -s -X POST https://${jenkins_instance}-eiffel004.lmera.ericsson.se:8443/jenkins/job/PCR_Gate_Integration_PM_DSL/build --user ciexadm200:${femToken}
            curl -s -X POST https://${jenkins_instance}-eiffel004.lmera.ericsson.se:8443/jenkins/job/PCR_Gate_Integration_PM_DSL_silent/build --user ciexadm200:${femToken}
        '''
    }
}

def getApiToken(fem) {
    def file = new File("/proj/ciexadm200/tools/utils/scripts/Monitoring_tools/Configuration/ApiTokens.xml")
    for (line in file.readLines()) {
        if (line.contains(fem)) {
            def token = line.split(">")[1].split("<")[0]
            return token
        }
    }
    return null
}