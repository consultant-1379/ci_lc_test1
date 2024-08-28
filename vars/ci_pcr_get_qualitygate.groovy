#!/usr/bin/env groovy

def call() {
    timeout(time: 5, unit: 'MINUTES') {
        withSonarQubeEnv(env.SQ_SERVER) {
            if (fileExists(file: 'target/sonar/report-task.txt')) {
                sh 'cat target/sonar/report-task.txt'
                def props = readProperties  file: 'target/sonar/report-task.txt'
                
                def (GROUPID, ARTIFACTID) = props['projectKey'].tokenize( ':' )
                env.DASHBOARD_URL =  props['serverUrl']+"/dashboard?branch="+props['branch']+"&id="+GROUPID+"%3A"+ARTIFACTID
                
                /*
                alternative to waitForQualityGate method to returns quality gate status.
                Error message: 
                java.lang.IllegalStateException: Unable to get SonarQube task id and/or server name. Please use the 'withSonarQubeEnv' wrapper to run your analysis.
                */                
                def TaskUrl = props['ceTaskUrl']
                def TaskUrljson = sh(script: "curl -u b847e26dd8d10f274e19b6d2ad299a706e3ecff4: ${TaskUrl}", returnStdout: true)
                def analysisId = sh(script: "echo ${TaskUrljson} | grep -o -P '(?<=analysisId:).*?(?=})'", returnStdout: true)
               
                def analysisIdUrl = props['serverUrl']+"/api/qualitygates/project_status?analysisId="+analysisId
                def analysisIdjson = sh(script: "curl -u b847e26dd8d10f274e19b6d2ad299a706e3ecff4: ${analysisIdUrl}", returnStdout: true)
                env.qualityGate_status = sh(script: "echo ${analysisIdjson} | grep -o -P '(?<=status:).*?(?=})' | head -1", returnStdout: true)
            }
            
            if (env.GERRIT_CHANGE_NUMBER) {
                if (env.SKIP_SONAR) {
                    qualityGateCheck()
                }
            }
        }
    }
}

def qualityGateCheck() {
    if (env.SKIP_SONAR == 'false') {
        sh 'echo "SKIP_SONAR -> FALSE, proceeding to Quality Gate section"'
        if (qualityGate_status.replaceAll("\\s","") != 'OK') {
            env.SQ_MESSAGE="'"+"SonarQube Quality Gate Failed: ${DASHBOARD_URL}"+"'"
            sh '''
            ssh -p 29418 gerrit.ericsson.se gerrit review --label 'SQ-Quality-Gate=-1' --message ${SQ_MESSAGE} --project ${GERRIT_PROJECT} ${GERRIT_PATCHSET_REVISION}
            '''
            error "Pipeline aborted due to quality gate failure!\n Report: ${env.DASHBOARD_URL}"
        } else {
            env.SQ_MESSAGE="'"+"SonarQube Quality Gate Passed: ${DASHBOARD_URL}"+"'"
            sh '''
            ssh -p 29418 gerrit.ericsson.se gerrit review --label 'SQ-Quality-Gate=+1' --message ${SQ_MESSAGE} --project ${GERRIT_PROJECT} ${GERRIT_PATCHSET_REVISION}
            '''
        }
    } else if (env.SKIP_SONAR == 'true') {
        sh 'echo "SKIP_SONAR -> TRUE, disable Quality gate section"'

        env.SQ_MESSAGE="'"+"SonarQube Quality Gate: ${DASHBOARD_URL}"+"'"
        sh '''
        ssh -p 29418 gerrit.ericsson.se gerrit review --label 'SQ-Quality-Gate=0' --message ${SQ_MESSAGE} --project ${GERRIT_PROJECT} ${GERRIT_PATCHSET_REVISION}
        '''
    }    
}