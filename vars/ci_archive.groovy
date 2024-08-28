#!/usr/bin/env groovy

def call(){
	 archiveArtifacts artifacts: '**/*.log,**/*.ear,**/*.rpm' ,allowEmptyArchive: true
}
