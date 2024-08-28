#!/usr/bin/env groovy

def call() {

    if (env.GERRIT_CHANGE_NUMBER) {
        return "https://gerrit.ericsson.se/#/c/${env.GERRIT_CHANGE_NUMBER}/${env.GERRIT_PATCHSET_NUMBER}"
    } else {
        println "Job was not triggered from a code review. It's not possible to retrive the Gerrit link."
        return null
    }

}