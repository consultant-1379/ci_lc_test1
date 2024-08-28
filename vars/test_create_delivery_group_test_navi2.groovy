#!/usr/bin/env groovy

import groovy.json.JsonSlurper

def call(){
    jiraIssue = (GERRIT_CHANGE_SUBJECT =~ /\[(.+)]/)//handles jira in []
    if (!jiraIssue) {
        jiraIssue = (GERRIT_CHANGE_SUBJECT =~ /(.+-\d+)/)  // Handles no [] around jira
    }
    jiraIssue = jiraIssue[ 0 ][ 1 ]
    str = jiraIssue.split(":")
    jiraIssue = str[0].trim()
    commitMsg = str[1].trim()

    println "------------------JIRA-ISSUE-COMMITMSG------------------------"
    println jiraIssue
    println commitMsg
    println "-------------------------END----------------------------------"

    pom = readMavenPom file: 'pom.xml'
    version = pom.version.replace("-SNAPSHOT","")
    modules = pom.getModules()
    for(String module: modules){
        matchRPMConvention = (module =~ /ERIC.*_CXP.*/)
        if (matchRPMConvention) {
            deliverPackage = module
        }
    }
    matchRPMConvention = null
    def getTeamUrl="https://cifwk-oss.lmera.ericsson.se/api/getteamfromjira/number/"+jiraIssue+"/?format=json"
    def teamName = ""
    
    try{
        def url = new URL(getTeamUrl);
        def HttpURLConnection connection = url.openConnection()
        def getRC = connection.getResponseCode();
        if(getRC.equals(200)) {
            teamResponseJson=connection.getInputStream().getText();
            println(teamResponseJson);
            myObject = readJSON text: teamResponseJson;
            teamName=myObject.team;
            myObject=null
        }
        connection=null
        getRC=null
    }catch (Exception e){
        println "FAILURE: Getting team name: Exception: " + e
        println "Failure: Make sure the web server is alive!"
    }  

    comment="Auto created by "+BUILD_URL+" "
    def message = '{"creator": "lciadm100","product": "ENM","artifacts": "'+deliverPackage+'::'+version+'","jiraIssues": "'+jiraIssue+'","missingDependencies": "false","comment":  "'+comment+'","team": "'+teamName+'", "validateOnly": "false", "checkKgb": "false"}'
    println message
    try{
        def post = new URL("http://atclvm1372.athtem.eei.ericsson.se:8080/api/createDeliveryGroup/?format=json").openConnection();
        post.setRequestMethod("POST")
        post.setDoOutput(true)
        post.setRequestProperty("Content-Type", "application/json")
        post.getOutputStream().write(message.getBytes("UTF-8"));
        def postRC = post.getResponseCode();
        def jsonSlurper = new JsonSlurper()
        if(postRC.equals(201)) {
            env.DG_CREATED = "true"
						env.PACKAGE_NAME = deliverPackage
						env.VERSION = version
            def groupIDJson=post.inputStream.text
						env.DG_OUTPUT =  groupIDJson
            println(groupIDJson);
            def object = jsonSlurper.parseText(groupIDJson)
            group_id=object[0].deliveryGroup
            println(group_id)
            post=null
            postRC=null
            if(!(commitMsg.endsWith("_noAutoDG"))){
                post = new URL("http://atclvm1372.athtem.eei.ericsson.se:8080/api/autoCreatedGroupDeliver/?format=json").openConnection();
                message = '{"group_id": "'+group_id+'"}'
                println(message)
                post.setRequestMethod("POST")
                post.setDoOutput(true)
                post.setRequestProperty("Content-Type", "application/json")
                post.setRequestProperty( 'Accept', 'application/json' )
                post.getOutputStream().write(message.getBytes("UTF-8"));
                postRC=post.getResponseCode();

                if(postRC.equals(200)) {
                    println(post.getInputStream().getText());
                }else{
                    println(post.getErrorStream().getText());
                    errorMsg = post.getErrorStream().getText();
                    throw new Exception(errorMsg);
                }
            }else{
                println("The_noAutoDG is defined in the Topic. So delivery group not Delivered automatically!!!")
            }
            post=null
            postRC=null
        }else{
            errorMsg = post.getErrorStream().getText();
            throw new Exception(errorMsg);
        }
        post=null
        postRC=null
    }
    catch (Exception e) {
        println "FAILURE: To post data into delivery Queue.Exception: " + e
        currentBuild.result = 'FAILURE'
    }
}
