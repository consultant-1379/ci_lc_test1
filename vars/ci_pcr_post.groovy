#!/usr/bin/env groovy

def call() {
    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
    archiveArtifacts artifacts: '**/*.log,**/*.ear,**/*.rpm', allowEmptyArchive: true
}
