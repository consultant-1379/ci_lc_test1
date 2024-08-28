#!/usr/bin/env groovy

def call(args) {
    println "Checkout master branch: ${GERRIT_MIRROR}/${args?.gerritProject}"
    git changelog: true, poll: false, url: "${GERRIT_MIRROR}/${args?.gerritProject}"
}