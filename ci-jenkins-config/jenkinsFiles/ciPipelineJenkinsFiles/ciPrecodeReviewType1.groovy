@Library('ci-pipeline-lib') _
metrics.init_metrics()
pipeline {
    agent { label 'dev_slave' }
    options {
        timeout(time: 30, unit: 'MINUTES') 
    }
    environment {
        TEAM_NAME = 'Undefined'
    }
    stages {
        stage('Clone') {
            steps {
                ci_pcr_clone()
            }
        }
        stage('Init') {
            steps {
                ci_pcr_init()
            }
        }
        stage('Unit') {
            steps {
                ci_pcr_unit()
            }
        }
        stage('Sonar') {
            steps {
                ci_pcr_sonar_analysis()
            }
        }
        stage('Quality Gate') {
            steps {
                ci_pcr_get_qualitygate()
            }
        }
    }
    post {
        always {
            ci_pcr_post()
        }
    }
}
