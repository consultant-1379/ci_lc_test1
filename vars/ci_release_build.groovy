#!/usr/bin/env groovy

def call(){
	
  	checkout poll: false, \
        	scm: [$class: 'GitSCM', branches: [[name: 'master']], \
        	doGenerateSubmoduleConfigurations: false, \
        	extensions: [[$class: 'DisableRemotePoll'], \
        	[$class: 'CleanCheckout'], \
        	[$class: 'UserExclusion', excludedUsers: '''ENM_Jenkins
        	ENM_CI_Admin
        	CDS_CI_Admin'''], \
        	[$class: 'LocalBranch', localBranch: 'master']], 
        	submoduleCfg: [], \
        	userRemoteConfigs: [[name: 'gcm', url: '${GERRIT_MIRROR}/${REPO}'], \
        	[name: 'gcn', url: '${GERRIT_CENTRAL}/${REPO}']]]
        	sh '''
        		git fetch gcm
        		git status
        	'''
                pom = readMavenPom file: 'pom.xml'
                env.version_from_pom = pom.version.replace("-SNAPSHOT","")
        withMaven(maven: 'Maven 3.0.5') {
            def buildResult = sh returnStatus: true, script: 'mvn -V -Dresume=false release:prepare release:perform  -DpreparationGoals="install -U" -Dgoals="clean deploy jacoco:report -U" -DlocalCheckout=true'
            println "Build Return Status: " + buildResult
            if (buildResult != 0) {
                currentBuild.rawBuild.result = Result.FAILURE
                throw new hudson.AbortException('Release Failed, Failing Build')
            }
	}	
}
