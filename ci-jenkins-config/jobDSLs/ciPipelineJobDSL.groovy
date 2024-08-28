import java.io.File


String projectsFile = readFileFromWorkspace('ci-jenkins-config/projects')
projectsFile.eachLine {
    project_name -> createPipelineJob(project_name)
}

def createPipelineJob(project_name) {
    println("Project name : " + project_name)
    def String ci_pipeline_jenkins_config_project = 'OSS/com.ericsson.oss.de/ci-pipeline-jenkins-config'

    def cmd1='git archive --remote=ssh://gerrit.ericsson.se:29418/'+project_name+' HEAD pipeline.cfg'
    def cmd2='tar --extract --to-stdout'

    def proc = cmd1.execute() |cmd2.execute()
    def std_out = new StringBuffer()
    proc.consumeProcessErrorStream(std_out)
    def config_file = proc.text

    branch = (config_file =~ /BRANCH=(.+)/)
    if(branch){
        branch = branch[0][1]
    }

    gerrit_refspec = (config_file =~ /GERRIT_REFSPEC=(.+)/)
    if(gerrit_refspec){
        gerrit_refspec = gerrit_refspec[0][1]
    }

    testware_repo = (config_file =~ /TESTWARE_REPO=(.+)/)
    if(testware_repo){
        testware_repo = testware_repo[0][1]
    }

    it_folder = (config_file =~ /IT_FOLDER=(.+)/)
    if(it_folder){
        it_folder = it_folder[0][1]
    }

    mvn_build_base_options = (config_file =~ /MVN_BUILD_BASE_OPTIONS=(.+)/)
    if(mvn_build_base_options){
        mvn_build_base_options = mvn_build_base_options[0][1]
    }

    mvn_verify_base_options = (config_file =~ /MVN_VERIFY_BASE_OPTIONS=(.+)/)
    if(mvn_verify_base_options){
        mvn_verify_base_options = mvn_verify_base_options[0][1]
    }

    source_dir = (config_file =~ /SOURCES_DIR=(.+)/)
    if(source_dir){
        source_dir = source_dir[0][1]
    }

    java_monitored_services = (config_file =~ /JAVA_MONITORED_SERVICES=(.+)/)
    if(java_monitored_services){
        java_monitored_services = java_monitored_services[0][1]
    }

    it_report_type = (config_file =~ /IT_REPORT_TYPE=(.+)/)
    if(it_report_type){
        it_report_type = it_report_type[0][1]
    }

    compose_file = (config_file =~ /COMPOSE_FILE=(.+)/)
    if(compose_file){
        compose_file = compose_file[0][1]
    }

    testrunner_java_version = (config_file =~ /TESTRUNNER_JAVA_VERSION=(.+)/)
    if(testrunner_java_version){
        testrunner_java_version = testrunner_java_version[0][1]
    }

    snapshot_code_rpms_container = (config_file =~ /SNAPSHOT_CODE_RPMS_CONTAINER=(.+)/)
    if(snapshot_code_rpms_container){
        snapshot_code_rpms_container = snapshot_code_rpms_container[0][1]
    }

    compose_http_timeout = (config_file =~ /COMPOSE_HTTP_TIMEOUT=(.+)/)
    if(compose_http_timeout){
        compose_http_timeout = compose_http_timeout[0][1]
    }

    ignore_rpms = (config_file =~ /IGNORE_RPMS=(.+)/)
    if(ignore_rpms){
        ignore_rpms = ignore_rpms[0][1]
    }

    default_team = (config_file =~ /DEFAULT_TEAM=(.+)/)
    if(default_team){
        default_team = default_team[0][1]
    }

    pipelineJob('pipeline_' + project_name.split('/').last()) {
        description("Autocreated pipeline job for ${project_name}")
        triggers {
            gerritTrigger {
                triggerOnEvents {
                    commentAdded {
                        verdictCategory('Code-Review')
                        commentAddedTriggerApprovalValue('+2')
                    }
                }
                gerritProjects {
                    gerritProject {
                        compareType("PLAIN")
                        pattern(project_name)
                        branches {
                            branch {
                                compareType("PLAIN")
                                    pattern("master")
                            }
                        }
                        disableStrictForbiddenFileVerification(false)
                    }
                }
            }
            parameters {
                stringParam('REPO', project_name)
                if(branch){stringParam('BRANCH', branch)}
                if(gerrit_refspec){stringParam('GERRIT_REFSPEC', gerrit_refspec)}
                if(testware_repo){stringParam('TESTWARE_REPO', testware_repo)}
                if(it_folder){stringParam('IT_FOLDER', it_folder)}
                if(mvn_build_base_options){stringParam('MVN_BUILD_BASE_OPTIONS', mvn_build_base_options)}
                if(mvn_verify_base_options){stringParam('MVN_VERIFY_BASE_OPTIONS', mvn_verify_base_options)}
                if(source_dir){stringParam('SOURCES_DIR', source_dir)}
                if(java_monitored_services){stringParam('JAVA_MONITORED_SERVICES', java_monitored_services)}
                if(it_report_type){stringParam('IT_REPORT_TYPE', it_report_type)}
                if(compose_file){stringParam('COMPOSE_FILE', compose_file)}
                if(testrunner_java_version){stringParam('TESTRUNNER_JAVA_VERSION', testrunner_java_version)}
                if(snapshot_code_rpms_container){stringParam('SNAPSHOT_CODE_RPMS_CONTAINER', snapshot_code_rpms_container)}
                if(compose_http_timeout){stringParam('COMPOSE_HTTP_TIMEOUT', compose_http_timeout)}
                if(ignore_rpms){stringParam('IGNORE_RPMS', ignore_rpms)}
                if(default_team){stringParam('DEFAULT_TEAM', default_team)}
            }

            definition {
                cpsScm {
                    scm {
                        git {
                            remote {url("${GERRIT_MIRROR}/${ci_pipeline_jenkins_config_project}")}
                            branch("*/master")
                        }
                    }
                    scriptPath('ci-jenkins-config/jenkinsFiles/ciPipelineJenkinsFiles/ciPipelineJenkinsFile.groovy')
                }
            }
        }
    }
}
listView('pipeline') {
    filterBuildQueue()
    filterExecutors()
    jobs {
        regex(/pipeline.*/)
    }
    columns {
        status()
        weather()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
        buildButton()
    }
}
