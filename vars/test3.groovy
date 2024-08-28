#!/usr/bin/env groovy

def call(){
 jiraIssue = (GERRIT_CHANGE_SUBJECT =~ /\[(.+)]/)//handles jira in []
  if (!jiraIssue) {
    jiraIssue = (GERRIT_CHANGE_SUBJECT =~ /(.+) /) // Handles no [] around jira
  }
    jiraIssue =jiraIssue[ 0 ][ 1 ]
    pom = readMavenPom file: 'pom.xml'
    version = env.version_from_pom
    modules=pom.getModules()
    for(String module: modules){
        matchRPMConvention = (module =~ /ERIC.*_CXP.*/)
        if (matchRPMConvention) {
            deliverPackage=module
        }
    }
  matchRPMConvention=null

  getTeamUrl="https://cifwk-oss.lmera.ericsson.se/api/getteamfromjira/number/"+jiraIssue+"/?format=json"
  url = new URL(getTeamUrl);//.openConnection();
  def HttpURLConnection connection = url.openConnection()
  getRC = connection.getResponseCode();
  if(getRC.equals(200)) {
                teamResponseJson=connection.getInputStream().getText()
    println(teamResponseJson);
    myObject = readJSON text: teamResponseJson;
    teamName=myObject.team;
  }

  connection=null
  getRC=null

  comment="Auto created by "+BUILD_URL+" "
	//post = new URL("https://cifwk-oss.lmera.ericsson.se/api/createDeliveryGroup/").openConnection();
	message = '{"creator": "lciadm100","product": "ENM","artifacts": "'+deliverPackage+'::'+version+'","jiraIssues": "'+jiraIssue+'","missingDependencies": "false","comment":  "'+comment+'","team": "'+teamName+'", "validateOnly": "false"}'
	println "Delivery Group creation not enabled, DG parameters below"
	println message
	//post.setRequestMethod("POST")
  //post.setDoOutput(true)
  //post.setRequestProperty("Content-Type", "application/json")
  //post.getOutputStream().write(message.getBytes("UTF-8"));
  //postRC = post.getResponseCode();

  //if(postRC.equals(201)) {
  //      println("created")
  //}
  post=null
}
