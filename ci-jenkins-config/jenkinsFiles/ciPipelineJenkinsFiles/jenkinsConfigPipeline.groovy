
GERRIT_PROJECT = 'OSS/com.ericsson.oss.de/ci-pipeline-jenkins-config'

node('') {
    stage('Checkout') {
        String gitUrl = "${GERRIT_MIRROR}/${GERRIT_PROJECT}"
        /* If this were run on a fully configured jenkins then the credentialsId
        * below would probably not be necessary. */
        git(url: gitUrl, credentialsId: 'a13b45e7-e261-47fd-98cc-ec112890cdbf')
    }
    stage('Create Jobs') {
        job_dsl_target = "ci-jenkins-config/jobDSLs/*.groovy"
        jobDsl(targets: job_dsl_target)
    }
}