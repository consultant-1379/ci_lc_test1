#!/usr/bin/env groovy
import groovy.json.JsonSlurper

def call() {
    setEnvironmentVariables()
    setJira()
    setTeamName()
    setVersionFromPom()
    setSonarBranch()
    setBuildName()
}

def setTeamName() {
    if (env.JIRA != '' && !env.JIRA.toUpperCase().contains('NO JIRA')) {
        def getTeamNameURL = "https://cifwk-oss.lmera.ericsson.se/api/getteamfromjira/number/" + env.JIRA + "/?format=json"
        try {
            jsonText = new URL(getTeamNameURL).text
            TEAM_NAME = new JsonSlurper().parseText(jsonText).team
        }
        catch (Exception e) {
            println "no team"
        }
    }
}

def setBuildName() {
    currentBuild.displayName = "${BUILD_NUMBER} | ${env.SONAR_BRANCH} | ${TEAM_NAME}"
}

def setJira() {
    def JiraMatcher = null
    if (env.GERRIT_CHANGE_SUBJECT) {
        if (GERRIT_CHANGE_SUBJECT =~ /\[(.+)]/) {
            JiraMatcher = (GERRIT_CHANGE_SUBJECT =~ /\[(.+)]/)//handles jira in []
        } else if (GERRIT_CHANGE_SUBJECT =~ /^(.+-\d+) /) {
            JiraMatcher = (GERRIT_CHANGE_SUBJECT =~ /^(.+-\d+) /)  // Handles no [] around jira
        } else if (GERRIT_CHANGE_SUBJECT =~ /([Nn][Oo] [Jj][Ii][Rr][Aa])/) {
            JiraMatcher = (GERRIT_CHANGE_SUBJECT =~ /([Nn][Oo] [Jj][Ii][Rr][Aa])/)  // Handles no jira
        }
        if (JiraMatcher) {
            env.JIRA = JiraMatcher[0][1]
        } else {
            env.JIRA = 'NO JIRA'
        }
    } else {
        env.JIRA = ''
    }
    JiraMatcher = null
}

def setSonarBranch() {
    if (env.GERRIT_CHANGE_SUBJECT) {
        if (env.JIRA != '' && !env.JIRA.toUpperCase().contains('NO JIRA')) {
            env.SONAR_BRANCH = env.JIRA
        } else {
            env.SONAR_BRANCH = GERRIT_CHANGE_NUMBER
        }
    } else {
        env.SONAR_BRANCH = env.VERSION 
    }
}

def setVersionFromPom() {
    pom = readMavenPom file: 'pom.xml'
    env.VERSION = pom.version
}

def setEnvironmentVariables() {
    if ((env.ADMIN_CONTROL != 'true')&&(fileExists(file: 'pipeline_local.cfg'))) {
        def envVariableMap = readProperties  file: 'pipeline_local.cfg'
        envVariableMap.each { entry -> env."$entry.key" = "$entry.value" }
    }
}
