import java.io.File

/*
loop createPipelinePreCodeUnitJob method for each entry of ci-jenkins-config/projects_unit file
e.g. about the file content:

OSS/com.ericsson.oss.services.shm/shm-common
OSS/com.ericsson.oss.services.shm/shm-licensemanagement
...

*/

projectFileLocation = 'ci-jenkins-config/projects_unit_test'
projectFileLocationNoGate = projectFileLocation+'_gate_off'
String projectsFile_normal = readFileFromWorkspace(projectFileLocation)


def noGatefile = new File(WORKSPACE+"/"+projectFileLocationNoGate)
String projectsFile_gate_off = ""

if (noGatefile.exists()){
    projectsFile_gate_off = readFileFromWorkspace(projectFileLocationNoGate)
    projectsFile = projectsFile_normal+ projectsFile_gate_off
} else {
    projectsFile = projectsFile_normal

}

projectsFile.eachLine {
    project_name -> createPipelinePreCodeUnitJob(project_name,projectsFile_gate_off)
}

String pipeline_name

def createPipelinePreCodeUnitJob(project_name,projectsFile_gate_off) {
    println("Project name : " + project_name)
    def String ci_pipeline_jenkins_config_project = 'OSS/com.ericsson.oss.de/ci-pipeline-jenkins-config' // Pipeline official repository.
  
    // extract environment variables from pipeline.cfg located in development repository
    def cmd1='git archive --remote=ssh://gerrit.ericsson.se:29418/'+ci_pipeline_jenkins_config_project+' HEAD pipeline_global.cfg'
    def cmd2='tar --extract --to-stdout'
    def proc = cmd1.execute() | cmd2.execute()
    
    def std_out = new StringBuffer()
    proc.consumeProcessErrorStream(std_out)
    def config_file = proc.text
    
    /* check if groupID contains 'servicegroup' string,
    in order to avoid Pipeline job with same name
    */
    if (project_name.contains("servicegroup")) {
      pipeline_name = project_name.split('/').last() + "_sg_PCR"
    } else {
      pipeline_name = project_name.split('/').last() + "_PCR"
    }

    // Create or updates a pipeline job.
    pipelineJob(pipeline_name) {
        // jenkins job description.
        if (isGateOff(projectsFile_gate_off,project_name)) {
            description('<p style="color:red; underline"><strong>&#9888; <u>QualityGate is OFF</u></strong></p><b><font color="red">CLICK "RETRIGGER" ON THE PATCH YOU WANT TO BUILD, OTHERWISE CLICK "BUILD NOW" TO BUILD THE LAST.</font></b><br><a href="https://confluence-nam.lmera.ericsson.se/display/CIE/Retriggering+a+parameterised%28preCodeReview%29+Jenkins+job">How to "RETRIGGER" a job</a><br>This job is triggered by a "git push origin HEAD:refs/for/master".<br>It will build the artifact. If the build is successful the code is sent for code review.<br>')
        }else {
            description('<b><font color="red">CLICK "RETRIGGER" ON THE PATCH YOU WANT TO BUILD, OTHERWISE CLICK "BUILD NOW" TO BUILD THE LAST.</font></b><br><a href="https://confluence-nam.lmera.ericsson.se/display/CIE/Retriggering+a+parameterised%28preCodeReview%29+Jenkins+job">How to "RETRIGGER" a job</a><br>This job is triggered by a "git push origin HEAD:refs/for/master".<br>It will build the artifact. If the build is successful the code is sent for code review.<br>')
        }

        // Block build if certain jobs are running.
        blockOn(project_name.split('/').last() + "_Release")

        // only up to this number of build records are kept.
        logRotator {
            numToKeep(20)
        }

        // Adds environment variables to the build.
        environmentVariables {
            env('REPO', project_name)
            if (isGateOff(projectsFile_gate_off, project_name)) {
                env('SKIP_SONAR', 'true')
            } else {
                env('SKIP_SONAR', 'false')
            }
            config_file.eachLine {
                String line ->
                    if (line.contains("SKIP_SONAR")) {//we don't want to add the skip_sonar from global config
                        return //return acts like continue in normal loop
                    }
                    // environment variable name, environment variable value.
                    def (env_name, env_value) = line.split('=', 2)
                    env(env_name, env_value)
            }
        }
        
        // Sets the trigger strategy that Jenkins will use to choose what branches to build in what order.
        triggers {
            gerritTrigger {
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
                cps {
					script(readFileFromWorkspace('ci-jenkins-config/jenkinsFiles/ciPipelineJenkinsFiles/pcr_Gate.groovy'))
					sandbox(true)
                }
            }
        }
    }
    queue(pipeline_name)
}

// Creates or updates a view that shows items in a simple list format.
listView('PCR') {
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

def isGateOff(projectsFile_gate_off,project_name){
    def foundMatch = false
    projectsFile_gate_off.eachLine {
        if (it.equals(project_name)){
            foundMatch = true
        }
    }

    if (foundMatch){
        return true
    } else {
        return false
    }
}