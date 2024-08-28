#!/usr/bin/env groovy

def call() {
    if (env.GERRIT_CHANGE_NUMBER) {
        checkout changelog: true,  \
		 scm: [$class: 'GitSCM',  \
            	 branches: [[name: "$GERRIT_REFSPEC"]],  \
            	 doGenerateSubmoduleConfigurations: false,  \
            	 extensions: [[$class: 'BuildChooserSetting', buildChooser: [$class: 'GerritTriggerBuildChooser']]],  \
            	 submoduleCfg: [],  \
            	 userRemoteConfigs: [[refspec: "${GERRIT_REFSPEC}",  \
            	 url: "${GERRIT_MIRROR}/${GERRIT_PROJECT}"]]]
    } else {
        println "No GERRIT_CHANGE_NUMBER"
        git changelog: true, poll: false, url: GERRIT_MIRROR + "/" + REPO
    }
}
