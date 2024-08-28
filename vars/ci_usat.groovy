#!/usr/bin/env groovy

def call(){
	sh '''
		env |grep -v "JAVA_HOME" | grep -v "M2_HOME" | grep "=" > env.txt
		export ENV_FILE="env.txt" 
		FILE=testrunner/testrunner.sh && git archive --remote=ssh://gerrit.ericsson.se:29418/OSS/com.ericsson.oss.de/ci-pipeline-tooling HEAD "$FILE" | tar -xO "$FILE" > $(basename "$FILE")
		chmod 755 testrunner.sh
		./testrunner.sh --type usat  --log debug run
	'''
}
