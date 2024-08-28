#!/usr/bin/env groovy

import groovy.json.JsonSlurper

def call(){
def teamName
def jiraIssue
  if (env.GERRIT_CHANGE_SUBJECT) {
      jiraIssue = (GERRIT_CHANGE_SUBJECT =~ /\[(.+)]/)//handles jira in []
      if (!jiraIssue) {
          jiraIssue = (GERRIT_CHANGE_SUBJECT =~ /^(.+-\d+) /)  // Handles no [] around jira
      }
      jiraIssue = jiraIssue[ 0 ][ 1 ]
      teamName = getTeamName(jiraIssue)

  } else {
      if (env.JIRA_ISSUE) {
        jiraIssue = env.JIRA_ISSUE
        teamName = getTeamName(jiraIssue)
      }
  }
    if (!teamName){
        teamName = env.DEFAULT_TEAM
    }
    pom = readMavenPom file: 'pom.xml'
    version = env.version_from_pom
    modules = pom.getModules()
    for(String module: modules){
        matchRPMConvention = (module =~ /ERIC.*_CXP.*/)
        if (matchRPMConvention) {
            deliverPackage = module
        }
    }
    println(jiraIssue)
    matchRPMConvention = null
    pom = null

		env.PACKAGE_NAME = deliverPackage
		env.PACKAGE_VERSION = version

    comment = "Auto created by "+BUILD_URL+" "
    message = '{"creator": "lciadm100","product": "ENM","artifacts": "'+deliverPackage+'::'+version+'","jiraIssues": "'+jiraIssue+'","missingDependencies": "false","comment":  "'+comment+'","team": "'+teamName+'", "validateOnly": "false", "checkKgb":"False"}'
    println(message)


    try{
        post = new URL("https://xxcifwk-oss.lmera.ericsson.se/api/createDeliveryGroup/?format=json").openConnection();
        post.setRequestMethod("POST")
        post.setDoOutput(true)
        post.setRequestProperty("Content-Type", "application/json")
        post.getOutputStream().write(message.getBytes("UTF-8"));
        def postRC = post.getResponseCode();
        def jsonSlurper = new JsonSlurper();
        if(postRC.equals(201)) {
						env.DG_CREATED = "true"
            def groupIDJson = post.inputStream.text
            println(groupIDJson);
						env.DG_OUTPUT = groupIDJson
            def object = jsonSlurper.parseText(groupIDJson)
            group_id = object[0].deliveryGroup
            println(group_id)
            post = null
            postRC = null
            if(!(env.GERRIT_CHANGE_SUBJECT.endsWith("_noAutoDG"))){
                post = new URL("https://xxcifwk-oss.lmera.ericsson.se/api/autoCreatedGroupDeliver/?format=json").openConnection();
                message = '{"group_id": "'+group_id+'"}'
                println(message)
                post.setRequestMethod("POST")
                post.setDoOutput(true)
                post.setRequestProperty("Content-Type", "application/json")
                post.setRequestProperty( 'Accept', 'application/json' )
                post.getOutputStream().write(message.getBytes("UTF-8"));
                postRC = post.getResponseCode();

                if(postRC.equals(200)) {
                    println(post.getInputStream().getText());
                }else{
                   errorMsg = post.getErrorStream().getText();
                   throw new Exception(errorMsg);
                }
            }else{
                println("The_noAutoDG is defined in the Topic. So delivery group is only created but not Delivered automatically!!!")
            }
            post = null
            postRC = null
        }else{
            errorMsg = post.getErrorStream().getText();
            throw new Exception(errorMsg);
        }
        post = null
        postRC = null
    }
    catch (Exception e) {
        println "FAILURE: To post data into delivery Queue.Exception: " + e
        currentBuild.result = 'FAILURE'
    }
}
@NonCPS
def getTeamName(String jiraIssue) {
    try{
     getTeamUrl = "https://cifwk-oss.lmera.ericsson.se/api/getteamfromjira/number/"+jiraIssue+"/?format=json"
    url = new URL(getTeamUrl);
     def HttpURLConnection connection = url.openConnection()
        def getRC = connection.getResponseCode();
        if(getRC.equals(200)) {
            teamResponseJson = connection.getInputStream().getText()
            println(teamResponseJson);
            //myObject = readJSON text: teamResponseJson;
            teamName=teamResponseJson.replace('{"team":"','')
            teamName=teamName.replace('"}','')
        } else{
            teamName = env.DEFAULT_TEAM
        }
        connection = null
        getRC = null
        teamResponseJson = null
        return teamName
    }catch (Exception e){
        println "FAILURE: Getting team name: Exception: " + e
        println "Failure: Make sure the web server is alive!"
    }
}

