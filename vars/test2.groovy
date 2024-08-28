#!/usr/bin/env groovy

def call(){
	try {
		when(currentBuild.result == null) {
			echo 'Performing steps of stage Zero'
			sh 'echo three'
			sh 'exit 1'
		}
	} catch (Exception err) {
		sh 'echo error'
	  currentBuild.rawBuild.result = Result.FAILURE
  }
}


