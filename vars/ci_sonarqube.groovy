#!/usr/bin/env groovy

def call(){
        if (env.GERRIT_CHANGE_NUMBER) {
        	withSonarQubeEnv('sonarqube') {
            		withMaven(jdk: 'jdk_8_localGE', maven: 'Maven 3.0.5') {
                		sh 'mvn clean install sonar:sonar -Dmaven.test.skip=true -Dsonar.analysis.mode=issues -Dsonar.report.export.path=sonar-report.json'
            		}
        	}
            	sonarToGerrit(
                	sonarURL: "${SONARQUBE_SERVER_URL}",
                	severity: 'INFO',
                	postScore: true,
                	category: 'Code-Review',
                	issuesScore: '-1',
                	noIssuesScore: '0'
                	)
        	
        	println("Checking SonarQube Analysis Results")
        	def output = readFile('target/sonar/sonar-report.json')
        	def parsedJson = new groovy.json.JsonSlurper().parseText(output)
        	parsedJson.issues.any { issues ->
            		if (issues.severity != "INFO" &&  issues.isNew == true) {
                		currentBuild.rawBuild.result = Result.FAILURE
										println("SonarQube Analysis Result has issues, Failing Build")
                		throw new hudson.AbortException('SonarQube Analysis Result has issues, Failing Build')
            		}
		}
        }
}
