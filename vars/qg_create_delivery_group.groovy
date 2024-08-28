#!/usr/bin/env groovy
import com.ericsson.oss.de.pipeline.exceptions.PipelineLibraryException

import groovy.json.*
import static com.ericsson.oss.de.pipeline.GlobalVars.*

def call(args) {

    if (!args?.jira) {
        throw new PipelineLibraryException("The JIRA ID needs to be provided to create a delivery queue!")
    }

    if (!args?.teamName) {
        throw new PipelineLibraryException("The team name needs to be provided to create a delivery queue!")
    }

    if (!args?.packageVersion) {
        throw new PipelineLibraryException("The package version needs to be provided to create a delivery queue!")
    }

    def ciPortalServer = "${env.CI_FRAMEWORK_PORTAL_URL}".toString()
    if (ciPortalServer == "") {
        ciPortalServer = CI_PORTAL_HOST
    }


    def missingDependencies = (args?.missingDependencies) ? args.missingDependencies : false

    def deliverable = "${getRpmModule()}::${args.packageVersion}"
    def comment = "Auto created by " + BUILD_URL

    def user = (args?.user) ? args.user : DEFAULT_USER_DELIVERY_QUEUE
    return createDelivery([
            creator: user,
            product: "ENM",
            jiraIssues: args.jira,
            missingDependencies: "${missingDependencies}",
            artifacts: deliverable,
            comment: comment,
            team: args.teamName,
            validateOnly: "false",
            checkKgb: "False"
    ], ciPortalServer)
}

def getRpmModule() {

    def pom = readMavenPom file: 'pom.xml'
    def modules = pom.getModules()
    //FIXME for now we assume each repo has one RPM. We can change this to findAll when we want to manage multiple RPMs bper repository
    return modules.find {it =~ /ERIC.*_CXP.*/}
}

def createDelivery(payload, server) {

    def response = [:]
    def post = new URL("https://${server}/api/createDeliveryGroup/?format=json").openConnection()

    try {

        post.setRequestMethod("POST")
        post.setDoOutput(true)
        post.setRequestProperty("Content-Type", "application/json")
        post.getOutputStream().write(new JsonBuilder(payload).toPrettyString().getBytes("UTF-8"))

        def responseCode = post.getResponseCode()
        if (responseCode == 201) {
            response.successful = true
            def json = readJSON text: post.inputStream.text
            response.delivery = json[0]

        } else {
            response.successful = false
            def errorResponse = readJSON text: post.errorStream.text
            response.exception = errorResponse.error

        }
    } finally {
        post = null
    }

    return response
}