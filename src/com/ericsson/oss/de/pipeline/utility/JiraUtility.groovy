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

import com.ericsson.oss.de.pipeline.exceptions.PipelineLibraryException
import groovy.json.JsonSlurper

class JiraUtility {

    static String getTeamName(jiraId) {
        def endpoint = "https://cifwk-oss.lmera.ericsson.se/api/getteamfromjira/number/${jiraId}/?format=json"
        def jsonText = new URL(endpoint).text
        try {
            return new JsonSlurper().parseText(jsonText).team

        } catch(Exception e) {
            throw new PipelineLibraryException(e)
        }

    }
}
