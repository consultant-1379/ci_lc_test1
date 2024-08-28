#!/usr/bin/env groovy

def call(){
	sh '''
        #clean workspace
        echo Workspace ${WORKSPACE}
        sudo rm -rf ${WORKSPACE}/*

	running=$(docker ps -a -q| wc -l)
	if [ "$running" -gt "0" ];then
    		echo "Killing containers"
    		docker rm -f $(docker ps -a -q)
	fi
        '''
	if (env.GERRIT_CHANGE_NUMBER) {
		checkout changelog: true, \
		scm: [$class: 'GitSCM', \
            	branches: [[name: "$GERRIT_REFSPEC"]], \
            	doGenerateSubmoduleConfigurations: false, \
            	extensions: [[$class: 'BuildChooserSetting', buildChooser: [$class: 'GerritTriggerBuildChooser']]], \
            	submoduleCfg: [], \
            	userRemoteConfigs: [[refspec: "${GERRIT_REFSPEC}", \
            	url: "${GERRIT_MIRROR}/${GERRIT_PROJECT}"]]]
	} else {
	    println "No GERRIT_CHANGE_NUMBER"
	    git changelog: true, poll: false, url: '${GERRIT_MIRROR}/${REPO}'
   	}
}
