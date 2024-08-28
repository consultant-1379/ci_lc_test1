#!/usr/bin/env groovy

def call(args) {

    println "Checkout code review https://gerrit.ericsson.se/#/c/${env.GERRIT_CHANGE_NUMBER}/${env.GERRIT_PATCHSET_NUMBER}: " +
            "${GERRIT_MIRROR}/${args?.gerritProject} -> ${GERRIT_REFSPEC}"
    checkout changelog: true,
    scm: [
            $class: 'GitSCM',
            branches: [[name: "$GERRIT_REFSPEC"]],
            doGenerateSubmoduleConfigurations: false,
            extensions: [[$class: 'BuildChooserSetting', buildChooser: [$class: 'GerritTriggerBuildChooser']]],
            submoduleCfg: [],
            userRemoteConfigs: [[refspec: "${GERRIT_REFSPEC}", url: "${GERRIT_MIRROR}/${args?.gerritProject}"]]
    ]

}