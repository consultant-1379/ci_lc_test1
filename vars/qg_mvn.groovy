#!/usr/bin/env groovy
import static com.ericsson.oss.de.pipeline.GlobalVars.MAVEN_CONFIG

def call(args) {

    def mavenConfig = args?.config ? args.config : MAVEN_CONFIG
    withMaven(maven: mavenConfig) {
        // Run the maven build
        sh "mvn ${args.cmd}"
    }

}