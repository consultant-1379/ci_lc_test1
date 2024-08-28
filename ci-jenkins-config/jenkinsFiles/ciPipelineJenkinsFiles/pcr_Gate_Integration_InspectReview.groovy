@Library('ci-pipeline-lib') _

pipeline {
    agent { label env.SLAVE_LABEL }
    options {
        timeout(time: 120, unit: 'MINUTES')
        timestamps()
    }
    stages {
        stage('Pre-step') {
            steps {
                ci_pcr_pre()
            }
        }
        stage('Clone') {
            steps {
              script {
                if (env.INSPECT_REVIEW == 'true') {
                   ci_pcr_clone()
                }  else {
                   ci_manage_commits()
                }
              }
            }
        }
        stage('Init') {
            steps {
                ci_pcr_init()
            }
        }
        stage('Build Containers') {
            steps {
                ci_build_containers()
            }
        }
        stage('Start Containers') {
            steps {
                ci_start_containers_with_rpm()
                ci_containers_healthcheck()
            }
        }
        stage('Integration-Arquillian') {
            when { environment name: 'IT_PROJECT_NAME', value: 'pm-service' }
            steps {
                ci_pcr_integration_arquillian()
            }
        }
        stage('Deploy Inspect_Review') {
            when { environment name: 'INSPECT_REVIEW', value: 'true' }
            steps {
                ci_inspect_review()
            }
        }
        stage('Env Setup') {
            when { environment name: 'IT_PROJECT_NAME', value: 'pm-service' }
            steps {
                ci_start_containers_with_rpm()   //post Arquillian
                ci_containers_healthcheck()
            }
        }
        stage('Integration') {
            steps {
                ci_pcr_integration_no_docker()      //including Cucumber Tests
            }
        }
        stage('Unit') {
            when {  not { environment name: 'PROJECT_NAME', value: 'pm-mediation-vertical-slice' } }
            steps {
                ci_pcr_unit()
            }
        }
        stage('Sonar') {
            when {
                environment name: 'SKIP_SONAR_GLOBAL', value: 'false'
            }
            steps {
                ci_pcr_sonar_analysis()
            }
        }
        stage('Quality Gate') {
            when {
                environment name: 'SKIP_SONAR_GLOBAL', value: 'false'
            }
            steps {
                ci_pcr_get_qualitygate()
            }
        }
    }
    post {
        always {
            ci_pcr_post()
            ci_reports()
        }
    }
}