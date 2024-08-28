@Library('ci-pipeline-lib') _
pipeline {
    agent { label 'Jenkins_fem142_mesos_podc_20' }
    options {
        timeout(time: 60, unit: 'MINUTES')
        timestamps()
    }
    stages {
        stage('Test Docker env1') {
			agent {
				// Equivalent to "docker build -f Dockerfile.build --build-arg version=1.0.2 ./build/
				dockerfile {
					filename 'Dockerfile'
					dir 'ci-jenkins-config/Dockerfiles/slave1'
				}
			}
            steps {
                sh 'node --version'
            }
        }
        stage('Test Docker env2') {
			agent {
				// Equivalent to "docker build -f Dockerfile.build --build-arg version=1.0.2 ./build/
				dockerfile {
					filename 'Dockerfile'
					dir 'ci-jenkins-config/Dockerfiles/slave2'
				}
			}
            steps {
                sh 'mvn --version'
            }
        }
    }
}