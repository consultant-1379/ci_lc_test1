#!/usr/bin/env groovy
import com.ericsson.oss.de.pipeline.utility.StringUtility

import static com.ericsson.oss.de.pipeline.GlobalVars.*

def call(args) {
    withSonarQubeEnv(SONARQUBE_CONFIG) {
        def pom = readMavenPom file: "${WORKSPACE}/pom.xml"
        def gav = [groupId: pom.groupId, artifactId: pom.artifactId, version: pom.version]
        return "${SONAR_HOST_URL}/dashboard?branch=${StringUtility.encode(args ?. branchName)}&id=${StringUtility.encode(gav.groupId + ":" + gav.artifactId)}"
    }
}