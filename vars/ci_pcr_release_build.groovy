#!/usr/bin/env groovy

def call() {

    checkout poll: false,  \
        	 scm: [$class      : 'GitSCM', branches: [[name: 'master']],  \
        	 doGenerateSubmoduleConfigurations: false,  \
        	 extensions: [[$class: 'DisableRemotePoll'],  \
        	 [$class: 'CleanCheckout'],  \
        	 [$class: 'UserExclusion', excludedUsers: '''ENM_Jenkins
        	ENM_CI_Admin
        	CDS_CI_Admin'''],  \
        	 [$class: 'LocalBranch', localBranch: 'master']],
                   submoduleCfg: [],  \
        	 userRemoteConfigs: [[name: 'gcm', url: '${GERRIT_MIRROR}/${REPO}'],  \
        	 [name: 'gcn', url: '${GERRIT_CENTRAL}/${REPO}']]]
    sh '''
        git fetch gcm
  		git status      	
  	   '''

    checkGerritSync()
    pom = readMavenPom file: 'pom.xml'
    env.BUILD_VERSION = pom.version.replace("-SNAPSHOT", "")
    env.CURRENT_VERSION = pom.version
    withMaven(maven: 'Maven 3.0.5') {
        if (!env.CURRENT_VERSION.contains("SNAPSHOT")) {
            println "NO SNAPSHOT"
            addSnapshotVersion()

        }
        def buildResult = sh returnStatus: true, script: 'mvn -V -Dresume=false release:prepare release:perform  -DpreparationGoals="install -U" -Dgoals="clean deploy jacoco:report -U" -DlocalCheckout=true'
        println "Build Return Status: " + buildResult
        if (buildResult != 0) {
            currentBuild.rawBuild.result = Result.FAILURE
            throw new hudson.AbortException('Release Failed, Failing Build')
        }
    }
}

def checkGerritSync() {
    sh '''
        RETRY=6
        SLEEP=10
        
        # check if branch was passed as arg, else use Jenkins working branch
        [ -n "$1" ] && branch=$1 || branch=${GIT_BRANCH##*/}
        
        # get the commit ID's on GC master and mirror
        echo "INFO: Checking commit ID's for '$branch' branch on Gerrit Central."
        gcr=$(git ls-remote -h ${GERRIT_CENTRAL}/${REPO} ${branch} | awk '{print $1}')
        gmr=$(git ls-remote -h ${GERRIT_MIRROR}/${REPO} ${branch} | awk '{print $1}')
        echo "INFO: central: ${gcr}"
        echo "INFO: mirror:  ${gmr}"
        
        # compare master and mirror
        if [[ "${gcr}" != "${gmr}" ]]; then
          echo "INFO: Gerrit central and mirror are out of sync."
          echo "INFO: Waiting a maximum of $((RETRY*SLEEP)) seconds for sync."
        
          retry=0
          # retry a number of times
          while (( retry < RETRY )); do
            echo "INFO: Attempting retry #$((retry+1)) of $RETRY in $SLEEP seconds."
            sleep $SLEEP
        
            gcr=$(git ls-remote -h ${gcu} ${branch} | awk '{print $1}')
            gmr=$(git ls-remote -h ${gmu} ${branch} | awk '{print $1}')
            echo "INFO: central: $gcr, mirror: $gmr"
        
            # compare master and mirror, again
            if [ "${gcr}" = "${gmr}" ]; then
                echo "INFO: fetching latest changes on branch $branch."
                git fetch
                break
            fi
        
            ((retry++))
          done
        fi
        
        # if still out of sync, fail the job
        [ "${gcr}" != "${gmr}" ] && echo "ERROR: Gerrit central and mirror out of sync." && exit 1
        # Check we're on the correct (synced) revision
        [ "${GIT_COMMIT}" != "${gmr}" ] && echo -e "*** WARNING: Not using latest revision.\nFetching upstream changes again from $gmu. ***" && git fetch
        echo "INFO: Branch in sync between Gerrit central and mirror."

    '''
}

def addSnapshotVersion() {
    sh '''    		
       #Replace versions with SNAPSHOT version
        mvn versions:set -DnewVersion=${CURRENT_VERSION}-SNAPSHOT

        #clean all untracted files
        git clean -fdx

        #Add files
        git add .
        git commit -m "Adding Snapshot to Project"

       '''
}
