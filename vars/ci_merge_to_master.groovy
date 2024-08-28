#!/usr/bin/env groovy

def call(){
	if (currentBuild.currentResult != 'FAILURE') {
            //Ensure latest version of master branch in use as we are merging the releases changes onto it
            sh '''
            git fetch gcm
            git merge master
            git push ${GERRIT_CENTRAL}/${REPO} HEAD:master
            '''
        }
}
