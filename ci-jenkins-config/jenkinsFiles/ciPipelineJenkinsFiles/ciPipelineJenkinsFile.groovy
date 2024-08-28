@Library('ci-pipeline-lib') _
metrics.init_metrics()
pipeline {
    agent { label 'pipeline_dev' }
    stages {
        stage('Clone') {
            steps {
                script {
                     ci_clone()
                     metrics.generate_metrics()
                }
            }
        }
        stage('Unit') {
            steps {
                script {
                     ci_unit()
                     metrics.generate_metrics()
                }
            }
        }
        stage('Coverage') {
            steps {
                script {
                     ci_coverage()
                     metrics.generate_metrics()
                }
            }
        }
        stage('Sonarqube') {
            steps {
                script {
                     //ci_sonarqube()
										 echo "Temp removed, see CIS-104412"
                     metrics.generate_metrics()
                }
            }
        }
        stage('USAT') {
            steps {
                script {
                    ci_usat()
                    metrics.generate_metrics()
                }
            }
        }
        stage('Submit Commit') {
            steps {
                script {
                    ci_submit_commit()
                    metrics.generate_metrics()
                }
            }
        }
        stage('Release Build'){
            steps {
                script {
                    ci_release_build()
                    metrics.generate_metrics()
                }
            }
        }
        stage('Git Publish'){
            steps {
                script {
                     ci_merge_to_master()
                     metrics.generate_metrics()
                }
            }
        }
        stage('Create Delivery Group'){
            steps {
                script {
                    //ci_create_delivery_group()
                    metrics.generate_metrics()
                }
            }
        }
    }
    post {
        always {
            echo 'always'
            script {
                ci_archive()
                ci_delivery_group_mail()
                metrics.generate_metrics()
                try {
                    metrics.send_metrics()
                }
                catch (Exception err) {
                    println("Error: " +err)
                }
            }
        }
    }
}
