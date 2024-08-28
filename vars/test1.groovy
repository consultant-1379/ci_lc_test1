#!/usr/bin/env groovy

def call(){
echo currentBuild.result
echo "------"
echo VAR1
echo "------"
echo env.VAR1
echo "------"
           when(currentBuild.result == null) {
                echo 'Performing steps of stage Zero'
                sh 'echo three'
	        }
}


