#!/usr/bin/env groovy
import com.ericsson.oss.de.pipeline.utility.CommitMessageUtility

def call() {
    return CommitMessageUtility.parseCommitMessage(
            sh(returnStdout: true, script: "git --no-pager log -1 || true").trim()
    )
}