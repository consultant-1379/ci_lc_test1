import java.io.File

/*
loop createPipelinePreCodeUnitJob method for each entry of ci-jenkins-config/projects_unit file
e.g. about the file content:

OSS/com.ericsson.oss.services.shm/shm-common
OSS/com.ericsson.oss.services.shm/shm-licensemanagement
...

*/
String projectsFile = readFileFromWorkspace('ci-jenkins-config/projects_unit')
projectsFile.eachLine {
    project_name -> createPipelinePreCodeUnitJob(project_name)
}

def createPipelinePreCodeUnitJob(project_name) {
    println("Project name : " + project_name)
    def String ci_pipeline_jenkins_config_project = 'OSS/com.ericsson.oss.de/ci-pipeline-jenkins-config' // Pipeline official repository.
  
    // extract environment variables from pipeline.cfg located in development repository
    def cmd1='git archive --remote=ssh://gerrit.ericsson.se:29418/'+ci_pipeline_jenkins_config_project+' HEAD pipeline_global.cfg'
    def cmd2='tar --extract --to-stdout'
    def proc = cmd1.execute() | cmd2.execute()
    
    def std_out = new StringBuffer()
    proc.consumeProcessErrorStream(std_out)
    def config_file = proc.text

    // Create or updates a pipeline job.
    pipelineJob(project_name.split('/').last() + "_PCR") {
        // jenkins job description.
        description('<b><font color="red">CLICK "RETRIGGER" ON THE PATCH YOU WANT TO BUILD, OTHERWISE CLICK "BUILD NOW" TO BUILD THE LAST.</font></b><br><a href="https://confluence-nam.lmera.ericsson.se/display/CIE/Retriggering+a+parameterised%28preCodeReview%29+Jenkins+job">How to "RETRIGGER" a job</a><br>This job is triggered by a "git push origin HEAD:refs/for/master".<br>It will build the artifact. If the build is successful the code is sent for code review.<br>')
        
        // Block build if certain jobs are running.
        blockOn(project_name.split('/').last() + "_Release")

        // only up to this number of build records are kept.
        logRotator {
            numToKeep(20)
        }
        
        // Adds environment variables to the build.
        environmentVariables {
            env('REPO', project_name)
            config_file.eachLine {
            String line ->
                // environment variable name, environment variable value.
                def (env_name, env_value) = line.tokenize( '=' )                
                env(env_name, env_value)
            }
        }
        
        // Sets the trigger strategy that Jenkins will use to choose what branches to build in what order.
        triggers {
            gerritTrigger {
                // silentMode(true) // Sets silent mode to on or off. When silent mode is on there will be no communication back to Gerrit.
                triggerOnEvents {
                    patchsetCreated { // Trigger when a new change or patch set is uploaded.
                        excludeDrafts(false)
                        excludeTrivialRebase(false) // this will ignore any patchset which Gerrit considers a "trivial rebase" from triggering this build.
                        excludeNoCodeChange(false) // this will ignore any patchset which Gerrit considers without any code changes from triggering this build.
                    }
                }
                
                // Specify what Gerrit project(s) to trigger a build on.
                gerritProjects {
                    gerritProject {
                        compareType("PLAIN") // The exact repository name in Gerrit, case sensitive equality.
                        pattern(project_name)
                        branches {
                            branch {
                                compareType("PLAIN") // The exact branch name in Gerrit, case sensitive equality.
                                    pattern("master")
                            }
                        }
                        disableStrictForbiddenFileVerification(false)
                    }
                }
            }

            definition {
                // Specify where to obtain a source code repository containing your JenkinsFile.
                cpsScm {
                    scm {
                        git {
                            remote {url("${GERRIT_MIRROR}/${ci_pipeline_jenkins_config_project}")} // Specify the URL of this remote repository. This uses the same syntax as your git clone command.
                            branch("*/master") // Specify the branches if you'd like to track a specific branch in a repository.
                        }
                    }
                    scriptPath('ci-jenkins-config/jenkinsFiles/ciPipelineJenkinsFiles/pcr_Gate_Jenkinsfile.groovy') // Relative location for JenkinsFile
                }
            }
        }
    }
}

// Creates or updates a view that shows items in a simple list format.
listView('1_PCR') {
    filterBuildQueue()
    filterExecutors()
    jobs {
        regex(/.*PCR/)
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