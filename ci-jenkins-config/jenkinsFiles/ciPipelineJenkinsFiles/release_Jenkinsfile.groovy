@Library('ci-pipeline-lib') _
metrics.init_metrics()
pipeline {
    agent { label 'GridEngine' }
    options {
        timeout(time: 60, unit: 'MINUTES')
        timestamps()
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
        stage('Release') {
            steps {
                ci_full_release_build()
                ci_merge_to_master()
            }
        }
        stage('Sonar') {
            steps {
                ci_release_sonar()
            }
        }
    }
}
