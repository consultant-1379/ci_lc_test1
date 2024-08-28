#!/usr/bin/env groovy
import com.ericsson.oss.de.pipeline.exceptions.PipelineLibraryException

def call(args) {

    if (!args?.gav) {
        throw new PipelineLibraryException("gav argument is required to use qg_get_latest_release! e.g: gav -> com.ericsson.oss.services:topologyCollectionsService")
    }

    def tokens = args.gav.split(":")
    if (tokens.size() < 2) {
        throw new PipelineLibraryException("GAV argument seems invalid. make sure you have passed at least \"groupId:artifactId\"")
    }
    def groupIdPath = tokens[0].replaceAll("\\.","/")
    def artifactId = tokens[1]
    def metadata = "https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/service/local/repositories/releases/content/${groupIdPath}/${artifactId}/maven-metadata.xml"

    def xmlText = new URL(metadata).text
    try {
        def xml = new XmlSlurper().parseText(xmlText)
        return xml.versioning.release.text()
    } catch (Exception e) {
        throw new PipelineLibraryException("Failed to parse the XML from nexus: ${metadata}", e)
    }

}

