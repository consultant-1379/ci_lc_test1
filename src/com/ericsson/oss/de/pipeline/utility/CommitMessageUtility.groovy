/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.de.pipeline.utility

class CommitMessageUtility {

    private static final String TAGS_REGEX = /\{\{([\w\d-]*)\}\}/
    private static final String AUTHOR_REGEX = /Author:\s([\w\s]*)/
    private static final String CHANGE_ID_REGEX = /Change-Id:\s([\w\d]*)/
    private static final String JIRA_REGEX = /(TORF-[\d]*)/

    static def parseCommitMessage(message) {

        def commitProperties = [:]

        def group = (message =~ TAGS_REGEX)
        if (group.hasGroup()) {
            commitProperties.tags = group.collect { it[1]}
        }

        group = (message =~ AUTHOR_REGEX)
        if (group.size() > 0) {
            commitProperties.author = group[0][1]
        } else {
            commitProperties.author = null
        }

        group = (message =~ CHANGE_ID_REGEX)
        if (group.size() > 0) {
            commitProperties.changeId = group[0][1]
        } else {
            commitProperties.changeId = null
        }

        group = (message =~ JIRA_REGEX)
        if (group.size() > 0) {
            commitProperties.jira = group[0][1]
        } else {
            commitProperties.jira = null
        }

        return commitProperties
    }

}
