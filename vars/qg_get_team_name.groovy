#!/usr/bin/env groovy
import com.ericsson.oss.de.pipeline.exceptions.PipelineLibraryException
import com.ericsson.oss.de.pipeline.utility.JiraUtility

def call(args) {

    if (args?.jira) {
        try {
            return JiraUtility.getTeamName(args.jira)
        } catch (PipelineLibraryException e) {
            println "failed to retrieve the team name: ${e.getCause().getMessage()}"
            return null
        }

    } else {
        println "No JIRA ID provided. Not possible to retrieve the team name."
        return null
    }

}
