import java.io.File


String projectsFile = readFileFromWorkspace('ci-jenkins-config/projects_unit')
projectsFile.eachLine {
    project_name -> createPipelinePreCodeUnitJob(project_name)
}

def createPipelinePreCodeUnitJob(project_name) {
    println("Project name : " + project_name)
    def String ci_pipeline_jenkins_config_project = 'OSS/com.ericsson.oss.de/ci-pipeline-jenkins-config'

    def cmd1='git archive --remote=ssh://gerrit.ericsson.se:29418/'+project_name+' HEAD pipeline.cfg'
    def cmd2='tar --extract --to-stdout'

    def proc = cmd1.execute() |cmd2.execute()
    def std_out = new StringBuffer()
    proc.consumeProcessErrorStream(std_out)
    def config_file = proc.text


    pipelineJob('Pipeline_Unit_' + project_name.split('/').last()) {
        description("Autocreated pipeline job for ${project_name}")
        triggers {
            gerritTrigger {
                triggerOnEvents {
                    patchsetCreated {
                        excludeDrafts(false)
                        excludeTrivialRebase(false)
                        excludeNoCodeChange(false)
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
                stringParam('TEAM_NAME', 'Undefined')
            }
            definition {
                cpsScm {
                    scm {
                        git {
                            remote {url("${GERRIT_MIRROR}/${ci_pipeline_jenkins_config_project}")}
                            branch("*/master")
                        }
                    }
                    scriptPath('ci-jenkins-config/jenkinsFiles/ciPipelineJenkinsFiles/ciPipelinePreCodeUnitJenkinsFile.groovy')
                }
            }
        }
    }
}
listView('Pipeline_Unit') {
    filterBuildQueue()
    filterExecutors()
    jobs {
        regex(/Pipeline_Unit.*/)
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

