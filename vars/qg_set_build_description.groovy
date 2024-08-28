#!/usr/bin/env groovy

def call(properties) {

    def output =
            "<b>Job Properties </b><br/>"+
            "--------------------------------------<br/>"

    properties.each {
        output += "<b>${it.key}</b>: ${it.value}<br/>"
    }

    currentBuild.description = output

}