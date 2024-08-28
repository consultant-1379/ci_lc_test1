#!/usr/bin/env groovy

def call() {
    if (env.GERRIT_TOPIC) {
        //Gerrit Topic
        def topicJsonSplit = getTopicInfo()
        parseTopic(topicJsonSplit)
    } else if (env.GERRIT_CHANGE_NUMBER) {
        //If we get here it's either a product or testware patch
        // This section will clone testware repo and testware patch if exists
        if (env.GERRIT_PROJECT && env.TESTWARE_REPO) {
            env.TESTWARE_ROOT_DIR = env.TESTWARE_REPO.split('/').last()

            if (env.GERRIT_PROJECT == env.REPO) {
                //clone product patchset
                checkout changelog: true,  \
		         scm: [$class: 'GitSCM',  \
            	 branches: [[name: "$GERRIT_REFSPEC"]],  \
            	 doGenerateSubmoduleConfigurations: false,  \
            	 extensions: [[$class: 'BuildChooserSetting', buildChooser: [$class: 'GerritTriggerBuildChooser']]],  \
            	 submoduleCfg: [],  \
            	 userRemoteConfigs: [[refspec: "${GERRIT_REFSPEC}",  \
            	 url: "${GERRIT_MIRROR}/${GERRIT_PROJECT}"]]]

                //Clone testware from master
                sh 'git clone ${GERRIT_MIRROR}/${TESTWARE_REPO}'


            } else {
                //Checkout main repo from master
                mainRepo = env.GERRIT_MIRROR + "/" + env.REPO
                git changelog: true, poll: false, url: mainRepo
                //Clone Testware patch
                env.TESTWARE_PATCH = true
                sh '''
               git clone ${GERRIT_MIRROR}/${TESTWARE_REPO}
               cd ${TESTWARE_ROOT_DIR}
               git fetch ${GERRIT_MIRROR}/${TESTWARE_REPO} ${GERRIT_REFSPEC} && git checkout FETCH_HEAD
               cd -
               '''

            }
        } else {
            //Single repo config
            //clone product patchset
            checkout changelog: true,  \
		         scm: [$class: 'GitSCM',  \
            	 branches: [[name: "$GERRIT_REFSPEC"]],  \
            	 doGenerateSubmoduleConfigurations: false,  \
            	 extensions: [[$class: 'BuildChooserSetting', buildChooser: [$class: 'GerritTriggerBuildChooser']]],  \
            	 submoduleCfg: [],  \
            	 userRemoteConfigs: [[refspec: "${GERRIT_REFSPEC}",  \
            	 url: "${GERRIT_MIRROR}/${GERRIT_PROJECT}"]]]
        }
    } else {
        //Not a patchset
        //clone main repo
        mainRepo = env.GERRIT_MIRROR + "/" + env.REPO
        git changelog: true, poll: false, url: mainRepo
        if (env.TESTWARE_REPO) {
            echo "build from master, clone testware from master"
            env.TESTWARE_ROOT_DIR = env.TESTWARE_REPO.split('/').last()
            sh 'git clone ${GERRIT_MIRROR}/${TESTWARE_REPO}'
        }

    }
}

def cloneMain(ref) {

    mainRepo = env.GERRIT_MIRROR + "/" + env.REPO
    checkout changelog: true,  \
	 scm: [$class: 'GitSCM',  \
     branches: [[name: ref]],  \
     doGenerateSubmoduleConfigurations: false,  \
     extensions: [[$class: 'BuildChooserSetting', buildChooser: [$class: 'GerritTriggerBuildChooser']]],  \
     submoduleCfg: [],  \
	 userRemoteConfigs: [[refspec: ref,  \
	 url: mainRepo]]]

}

def cloneRepo(ref, repoToClone) {

    env.REPO_TO_CLONE = repoToClone
    env.REF = ref
    env.CLONE_ROOT_DIR = repoToClone.split('/').last()
    if (repoToClone == env.TESTWARE_REPO) {
        echo "clone TW"
        env.TESTWARE_ROOT_DIR = env.TESTWARE_REPO.split('/').last()
    }
    sh '''
               git clone ${GERRIT_MIRROR}/${REPO_TO_CLONE}
               cd ${CLONE_ROOT_DIR}
               git fetch ${GERRIT_MIRROR}/${REPO_TO_CLONE} ${REF} && git checkout FETCH_HEAD
               cd -
               '''
}

def parseTopic(topicJsonSplit) {

    for (int i = 0; i < topicJsonSplit.size(); i++) {

        def parsedJson = readJSON text: topicJsonSplit[i]

        if (parsedJson['project']) {
            println parsedJson['project']
            topicRepo = parsedJson['project']
            env.TOPIC_REPO = topicRepo
            ref = parsedJson['currentPatchSet']['ref']

            if (topicRepo == env.REPO) {
                println "MAIN REPO, DO SCM CHECKOUT"
                println "MAIN REPO, DO SCM CHECKOUT"
                cloneMain(ref)
            } else if (topicRepo == env.TESTWARE_REPO) {
                echo "testware patch"
                echo "testware patch"
                cloneRepo(ref, topicRepo)

            }

        }
    }

}


@NonCPS
def getTopicInfo() {
    println "GERRIT TOPIC"
    def getTopicString = "ssh -p 29418 " + env.GERRIT_HOST + " gerrit query topic: " + env.GERRIT_TOPIC + " --current-patch-set --format JSON"
    def proc = getTopicString.execute()
    def buffer = new StringBuffer()
    proc.consumeProcessErrorStream(buffer)

    def topicJson = proc.text
    println topicJson
    println "----"
    proc = null
    buffer = null
    return topicJson.split('\n')
}








