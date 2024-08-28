import java.io.File

/*
loop createPipelineReleaseJob method for each entry of ci-jenkins-config/projects_unit file
e.g. about the file content:

OSS/com.ericsson.oss.services.shm/shm-common
OSS/com.ericsson.oss.services.shm/shm-licensemanagement
...

*/
String projectsFile = readFileFromWorkspace('ci-jenkins-config/projects_unit')
projectsFile.eachLine {
    project_name -> createPipelineReleaseJob(project_name)
}

def createPipelineReleaseJob(project_name) {
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
    pipelineJob(project_name.split('/').last() + "_Release") {
        // jenkins job description.
        description('<font color="red"><b>Do not make changes to Maven goals</b></font><br> This job build the artifact again with a full version number. Code coverage and Unit tests are ran again. If the job is successful the code is published to the CI Portal.<br>')
       
        // Block build if certain jobs are running.
        blockOn(project_name.split('/').last() + "_PCR")

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
            definition {
                // Specify where to obtain a source code repository containing your JenkinsFile.
                cpsScm {
                    scm {
                        git {
                            remote {url("${GERRIT_MIRROR}/${ci_pipeline_jenkins_config_project}")} // Specify the URL of this remote repository. This uses the same syntax as your git clone command.
                            branch("*/master") // Specify the branches if you'd like to track a specific branch in a repository.
                        }
                    }
                    scriptPath('ci-jenkins-config/jenkinsFiles/ciPipelineJenkinsFiles/release_Jenkinsfile.groovy') // Relative location for JenkinsFile
                }
            }
        }
    }
}

// Creates or updates a view that shows items in a simple list format.
listView('2_Release') {
    filterBuildQueue()
    filterExecutors()
    jobs {
        regex(/.*Release/)
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