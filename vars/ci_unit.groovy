#!/usr/bin/env groovy

def call(){
 	withMaven(maven: 'Maven 3.0.5',options: [invokerPublisher(), concordionPublisher(), dependenciesFingerprintPublisher(), findbugsPublisher(), artifactsPublisher(), jgivenPublisher(), junitPublisher(healthScaleFactor: 1.0), mavenLinkerPublisher(), openTasksPublisher(), pipelineGraphPublisher()]) {
  	sh 'mvn clean install -P unit'
   	junit allowEmptyResults: true, healthScaleFactor: 0.0, testResults: '**/surefire-reports/*.xml'

 	}
}
