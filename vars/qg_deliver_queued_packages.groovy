#!/usr/bin/env groovy
import com.ericsson.oss.de.pipeline.exceptions.PipelineLibraryException
import groovy.json.JsonBuilder

import static com.ericsson.oss.de.pipeline.GlobalVars.*

def call(args) {

    if (!args?.deliveryGroupId) {
        throw new PipelineLibraryException("The delivery group id is required to make a delivery!")
    }

    def user = (args?.user) ? args.user : DEFAULT_USER_DELIVERY_QUEUE
    return deliver([group_id: args.deliveryGroupId, user: user])

}

def deliver(payload) {

    def response = [:]

    def post = new URL("https://${CI_PORTAL_HOST}/api/autoCreatedGroupDeliver/?format=json").openConnection()
    post.setRequestMethod("POST")
    post.setDoOutput(true)
    post.setRequestProperty("Content-Type", "application/json")
    post.setRequestProperty( 'Accept', 'application/json' )
    post.getOutputStream().write(new JsonBuilder(payload).toPrettyString().getBytes("UTF-8"))
    def responseCode = post.getResponseCode()

    if (responseCode == 200) {
        response.successful = true
        response.message = post.inputStream.text

    } else {
        response.successful = false
        response.message = post.errorStream.text
    }

    return response
}