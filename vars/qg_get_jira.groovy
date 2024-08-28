#!/usr/bin/env groovy
import com.ericsson.oss.de.pipeline.utility.CommitMessageUtility

def call() {

    def commitProperties = CommitMessageUtility.parseCommitMessage(
            sh(returnStdout: true, script: "git --no-pager log -1 || true").trim()
    )
    def jira = commitProperties?.jira

    if (!jira || jira == 'null') {
        def gerritChange = env.GERRIT_CHANGE_NUMBER
        jira = "No Jira ${gerritChange ? "[${gerritChange}]" : ""}"
    }

    return jira

}