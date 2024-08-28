import java.io.File
import groovy.json.JsonSlurper

/*
loop createPipelinePreCodeIntegrationJob method for each entry of ci-jenkins-config/projects file
e.g. about the file content:

OSS/com.ericsson.oss.services.shm/shm-common
OSS/com.ericsson.oss.services.shm/shm-licensemanagement
...

*/

projectFileLocation = 'ci-jenkins-config/projects'
String projectsFile = readFileFromWorkspace(projectFileLocation)

jenkins_instance = DOMAIN_ID.tokenize('.').last()
String pipeline_name

projectsFile.eachLine {
    project_details ->
        def env_map = [:] // hashmap to group environment variables contained in projects file
        project_name = project_details.tokenize(',')[0]
        project_name = project_name.replaceAll("\\s","") // remove empty spaces

        if (project_details.contains(jenkins_instance) && !project_details.contains("EXC-PCR") && project_details.contains("SILENT_MODE=false") && project_details.contains("PCR=test_PM")){
            project_details.tokenize(',').each {
                project_element ->
                    if (project_element.contains("=")) {
                        def project_variable_name = project_element.tokenize('=')[0]
                        def project_variable_value = project_element.tokenize('=')[1]
                        env_map[project_variable_name] = project_variable_value
                    }
            }

            if (project_details.contains("SKIP_SONAR=false")){
                createPipelinePreCodeIntegrationJob(project_name,"true",env_map)
            } else {
                createPipelinePreCodeIntegrationJob(project_name,"false",env_map)
            }
        }
}

def createPipelinePreCodeIntegrationJob(project_name,gating,env_map) {
    println("Project name : " + project_name)
    def String ci_pipeline_jenkins_config_project = 'OSS/com.ericsson.oss.ci/ci_lc_test1' // Pipeline official repository.

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
    
    arquillian_cucumber_options_value = set_arquillian_cucumber_options(project_name)

    // Create or updates a pipeline job.
    pipelineJob(pipeline_name) {
        // jenkins job description.
        if (gating.equals("false")) {
            description('<p style="color:red; underline"><strong>&#9888; <u>QualityGate is OFF</u></strong></p><b><font color="red">CLICK "RETRIGGER" ON THE PATCH YOU WANT TO BUILD, OTHERWISE CLICK "BUILD NOW" TO BUILD THE LAST.</font></b><br><a href="https://confluence-nam.lmera.ericsson.se/display/CIE/Retriggering+a+parameterised%28preCodeReview%29+Jenkins+job">How to "RETRIGGER" a job</a><br>This job is triggered by a "git push origin HEAD:refs/for/master".<br>It will build the artifact. If the build is successful the code is sent for code review.<br><br>This job is able to trigger Arquillian and Cucumber integration tests ("legacy" pm-service vertical slice like)<br>')
        } else {
            description('<b><font color="red">CLICK "RETRIGGER" ON THE PATCH YOU WANT TO BUILD, OTHERWISE CLICK "BUILD NOW" TO BUILD THE LAST.</font></b><br><a href="https://confluence-nam.lmera.ericsson.se/display/CIE/Retriggering+a+parameterised%28preCodeReview%29+Jenkins+job">How to "RETRIGGER" a job</a><br>This job is triggered by a "git push origin HEAD:refs/for/master".<br>It will build the artifact. If the build is successful the code is sent for code review.<br><br>This job is able to trigger Arquillian and Cucumber integration tests ("legacy" pm-service vertical slice like)<br>')
        }

        // Block build if certain jobs are running.
        blockOn(project_name.split('/').last() + "_Release")

        // only up to this number of build records are kept.
        logRotator {
            numToKeep(20)
        }
        
        parameters {
            extensibleChoiceParameterDefinition { 
              name('ARQUILLIAN_CUCUMBER_OPTIONS')
              choiceListProvider {
                textareaChoiceListProvider {
                  choiceListText(arquillian_cucumber_options_value)
                  defaultChoice('')
                  addEditedValue(true)
                  whenToAdd('Triggered')
                }
              }
              editable(true)
              description('<br>Select from available or list or edit  Cucumber Options to be used.<br>E.g.<br>--tags ~@ignore<br>--tags ~@ignore --tags ~@Netsim                                (Features/Scenarios which do not require netsim)<br>--tags @Netsim                                                           (Features/Scenarios against netsim)<br>--tags ~@MINI-LINK-Indoor                                          (Exclude MiniLink features)<br>--tags @RadioNode,@SGSN-MME,@CPP                    (CPP, RadioNode and SGSN-MME test cases)<br>--tags @Router6672                                                     (Router6672 test cases)<br>--tags @RadioNode                                                      (RadioNode test cases)<br>--tags @SGSN-MME                                                    (SGSN-MME test cases)<br>--tags @CPP                                                               (CPP test cases)<br>--tags @CPP --tags @Stats                                          (Stats test cases for CPP)<br>--tags @RadioNode,@SGSN-MME --tags @Collection    (Collection test cases for RadioNode and SGSN-MME)')
            }
            booleanParam('GERRIT_VERTICAL_SLICE_SUBMIT', false, 'optional parameter: Check this parameter if you want this job to submit the reviews under test')
            stringParam('TOPIC', '', 'optional parameter: select the topic you want to verify')
            stringParam('GERRIT_CHANGE_NUMBERS', '', "<p>Use this field if you want to build a subset of reviews for a given gerrit change number.<br>The subset is identified by the owner provided. Use a list comma separated for more than one change number.<br>(e.g. given <a href='https://gerrit.ericsson.se/#/c/1898411/'>https://gerrit.ericsson.se/#/c/1898411/</a> and <a href='https://gerrit.ericsson.se/#/c/1906315/'>https://gerrit.ericsson.se/#/c/1906315/</a><br>the parameter value must be: <b>1898411,1906315</b>)<br><b>NOTE: </b>the last patch will be taken in consideration.</p>")
            stringParam('DeployPackage', '', '<p>If you <b>want</b> to execute software update, <b>add your packages here</b> in the following format:</p><blockquote><p><font color="blue"><b>&lt;deliverable name&gt;::&lt;version&gt;</b></font>, where version is in maven version format or "Latest". There is also an option to add a package that that has not been officially delivered as a complete URL.</p></blockquote><blockquote><p><b>When adding multiple rpms, use @@ as the separator:</b><br>&lt;deliverable name&gt;::&lt;version&gt;<font color="blue"><b>@@</b></font>&lt;deliverable name&gt;::&lt;version&gt;<br>ERICneconnmepstub_CXP0000000::Latest@@ERICmediationrouter_CXP9031423::2.10.7@@ERICrpm::https://cifwk-oss.lmera.ericsson.se/static/tmpUploadSnapshot/2015-08-17_14-44-56/ERICapdatamacro_CXP9030537-1.22.9-SNAPSHOT20150817134050.noarch.rpm</p></blockquote>')
			choiceParam('DOCKER_COMPOSE_OPTIONS', ['-f docker-compose.yml', '-f docker-compose.netsim.yml', '-f docker-compose.yml'], 'Docker compose configuration')            
            stringParam('INTEGRATION_TESTS', 'FileCollectionIT,SubscriptionAndScannerIT', '<p>If you <b>want</b> to execute software updaIntegration Test to be executed (name of java classes comma separated: e.g. SubscriptionAndScannerIT,FileCollectionIT)')
            booleanParam('FAST_FAIL', true, 'Skip all remaining features when a failure happens')
        }

        // Adds environment variables to the build.
        environmentVariables {
            env('REPO', project_name)

            config_file.eachLine {
                String line ->
                    // environment variable name, environment variable value.
                    def (env_name, env_value) = line.split('=', 2)

                    if (env_name.equals("SLAVE_LABEL")) {
                       env_value = env_value.replaceAll("FEM",jenkins_instance)
                    }

                    if (env_map.containsKey(env_name)) {
                        env(env_name, env_map[env_name])
                        return //return acts like continue in normal loop
                    }

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
                    script(readFileFromWorkspace('ci-jenkins-config/jenkinsFiles/ciPipelineJenkinsFiles/pcr_Gate_Integration_InspectReview.groovy'))
                    sandbox(true)
                }
            }
        }
    }
}

def set_arquillian_cucumber_options(project_name){
    arquillian_cucumber_options_value = '--tags ~@CBP-OI --tags ~@MINI-LINK-Indoor\n--tags @RadioNode --tags @ScannerPolling\n--tags @RNC\n--tags @EPG-OI --tags @ScannerPolling\n--tags @SGSN-MME\n--tags @RadioNode --tags @SGSN-MME\n--tags @RadioNode\n--tags @BSC\n--tags @RadioNode\n--tags @Router6274\n--tags @Router6274 --tags @ScannerPolling\n--tags @RadioNode,@SGSN-MME,@BGF\n--tags @ESC\n--tags @BGF\n--tags @Router6274 --tags @FileCollection\n--tags @SGSN-MME --tags @Collection\n--tags @Router6274,@SGSN-MME,@BGF\n--tags @SGSN-MME --tags @Collection\n--tags @RadioNode,@SGSN-MME,@BGF,@Router6274\n--tags @SGSN-MME,@Router6274\n--tags @EPG-OI\n--tags @Router6672 --tags @ScannerPolling\n--tags @RNC,@StatsFileCollection\n--tags @RNC --tags @Stats --tags @Collection --tags @StatsFileCollection\n--tags @RNC --tags @Collection --tags @StatsFileCollection\n--tags @RNC --tags @Collection\n--tags @RBS\n--tags @CPP\n--tags @CPP --tags @StatsFileCollection\n--tags @MGW\n--tags @vWMG\n--tags @Router6672,@Router6x71,@Router6274\n--tags@Router6672,@Router6274,@Router6675,@Router6x71\n--tags @RadioNode --tags @StatsSubscription\n--tags ~@vWMG --tags ~@CBP-OI--tags ~@CPP\n--tags @CCRC --tags @CCPC --tags @CCSM --tags @CCDM --tags @SC --tags @CCES\n--tags @CCRC --tags @CCPC --tags @CCSM --tags @CCDM --tags @SC\n--tags @CCRC\n--tags @CCRC --tags @CCPC\n--tags @CCPC --tags @CCSM --tags @CCDM --tags @SC\n--tags @MINI-LINK-6351\n--tags @CCRC --tags @CCPC --tags @CCSM --tags @CCDM --tags @SC --tags CCES\n--tags @CCRC --tags @CCPC --tags @CCSM --tags @CCDM --tags @SC --tags @CCES\n--tags @CCRC,@CCPC,@CCSM,@CCDM,@SC,@CCES\n--tags @CCRC --tags @CCPC --tags @CCSM --tags @CCDM --tags @SC\n--tags @CCRC,@CCPC,@CCSM,@CCDM,@SC\n--tags @SDKNodeSnmp\n--tags @CISCO-ASR9000\n--tags @vRC\n--tags @JUNIPER-PTX\n--tags ~@JUNIPER-PTX\n--tags @juniper 20.1\n--tags ~@CBP-OI --tags ~@Juniper-20.1\n--tags @SAPC\n--tags @juniperptx 20.1\n--tags @20.1 Juniper\n--tags @ptx24\n--tags ~@CBP-OI --tags ~@junosptx\n--tags ~@junos,@StasFileCollection\n--tags ~@CBP-OI --tags ~@junospm\n--tags @jun20.1oer\n--tags ~@CBP-OI --tags ~@version20.1juniper\n--tags ~@CBP-OI --tags ~@ptxjunos\n--tags ~@CBP-OI --tags ~@JUNIPER-PTX\n--tags @SIU02\n--tags @EbsStream\n--tags ~@CBP-OI --tags ~@JUNIPER\n--tags @HLRFE-BSP\n--tags @MINI-LINK-6351,CISCO-ASR9000,@JUNIPER-PTX\n--tags @MINI-LINK-6351,@CISCO-ASR9000,@JUNIPER-PTX\n--tags @MINI-LINK-6351,@JUNIPER-PTX,@ESC\n--tags @MINI-LINK-6352,@juniperptx 20.1,@ESC\n--tags @MINI-LINK-6351,@juniperptx 20.1,@ESC\n--tags @MINI-LINK-6351 --tags @ESC\n--tags @MINI-LINK-6351,@CISCO-ASR9000\n--tags @MINI-LINK-Indoor\n--tags @Router6274 --tags @MINI-LINK-Indoor\n--tags @JUNIPER-PTX --tags @StatsFileCollection\n~tags @vDU\n--tags ~@CBP-OI\n--tags ~@CBP-OI --tags ~@Router6274\n--tags ~@CBP-OI --tags ~@MINI-LINK-Indoor --tags@MINI-LINK-Indoor\n--tags ~@CBP-OI --tags ~@MINI-LINK-Indoor --tags ~@Router6274 --tags ~@MINI-LINIK-6351\n--tags @BSC --tags @Stats --tags @StatsFileCollection\n--tags @BSC,@vHLRFE,@HLRFE-BSP,@HLRFE-IS --tags @Stats --tags @Collection --tags @StatsFileCollection\n--tags @SBG-IS\ntags(@JUNIPER-PTX @StatsFileCollection\n--tags ~@CBP-OI --tags @JUNIPER-PTX @StatsFileCollection\n--tags ~@CBP-OI --tags ~@VTFRadioNode\n--tags ~@CBP-OI --tags ~@MINI-LINK-6351 --tags ~@Router6274\n--tags ~@CBP-OI --tags ~@MINI-LINK-6351 --tags ~@Router6274 --tags ~@MINI-LINK-Indoor\n        \n--tags @JUNIPER-PTX @StatsFileCollection\n--tags @JUNIPER-PTX,@StatsFileCollection\n--tags @JUNIPER-PTX,@StatsFileCollection,@SDKNodeSnmp\n--tags @HLRFE-BSP --tags @Stats --tags @Collection --tags @StatsFileCollection\n--tags @HLRFE-BSP --tags @Stats --tags @Collection --tags @StatsFileCollection --tags@Roby\n--tags @HLRFE-BSP --tags @Stats --tags @Collection --tags @StatsFileCollection --tags @Roby\n--tags @SGB-IS\n--tags @HLRFE-BSP,@HLRFE --tags @Stats --tags @Collection --tags @StatsFileCollection --tags @Roby\n--tags @HLRFE-BSP,@HLRFE-IS --tags @Stats --tags @Collection --tags @StatsFileCollection --tags @Roby\n--tags @HLRFE-BSP,@HLRFE-IS,@BSC --tags @Stats --tags @Collection --tags @StatsFileCollection --tags @Roby\n--tags @JUNIPER-PTX @Netsim @Stats @Collection @StatsFileCollection\n--tags @HLRFE-BSP,@HLRFE-IS,@BSC --tags @Stats --tags @Collection --tags @StatsFileCollection,@StatsFileRecovery\n--tags @HLRFE-BSP --tags @Stats --tags @Collection --tags @StatsFileCollection,@StatsFileRecovery\n--tags@MINI-LINK-Indoor,@JUNIPER-PTX,@CISCO-ASR9000\n--tags @MINI-LINK-Indoor,@JUNIPER-PTX\n--tags @HLRFE-BSP,@HLRFE-IS,@BSC --tags @Stats --tags @Collection --tags @StatsFileCollection\n--tags @SAPC,@vSAPC --tags @Stats --tags @Collection\n--tags @HLRFE-BSP,@HLRFE-IS,@vHLRFE,@BSC --tags @Stats --tags @Collection --tags @StatsFileCollection,@StatsFileRecovery\n--tags @HLRFE-BSP,@HLRFE-IS,@vHLRFE,@BSC,@MSC-BC-IS --tags @Stats --tags @Collection --tags @StatsFileCollection,@StatsFileRecovery\n--tags @CISCO-ASR9000,@JUNIPER-PTX\n--tags @HLRFE-BSP,@HLRFE-IS,@vHLRFE,@BSC,@MSC-BC-IS,@MSC-BC-BSP --tags @Stats --tags @Collection --tags @StatsFileCollection,@StatsFileRecovery\n--tags @UPG,@IPWorks,@HSSFE  --tags @Stats --tags @Collection --tags @StatsFileCollection,@StatsFileRecovery\n--tags @DSC --tags @StatsFileCollection\n--tags @UPG  --tags @Stats --tags @Collection --tags @StatsFileCollection,@StatsFileRecovery\n--tags @SGSN-MME  --tags @Stats --tags @Collection --tags @StatsFileRecovery\n--tags @SGSN-MME  --tags @Stats --tags @Collection --tags @StatsFileCollection\n--tags ~@CBP-OI --tags ~@MINI-LINK-Indoor --tags ~@RNC --tags ~@SDKNodeSnm --tags ~@JUNIPER-PTX --tags ~@vHLRFE --tags ~@ESC\n--tags ~@CBP-OI--tags ~@MINI-LINK-Indoor --tags ~@RNC --tags ~@SDKNodeSnm --tags ~@JUNIPER-PTX --tags ~@vHLRFE --tags ~@ESC --tags ~@MINI-LINK-6351\n--tags @CPP --tags @StatsFileRecovery\n--tags ~@CBP-OI --tags ~@MINI-LINK-Indoor --tags ~@RNC --tags ~@SDKNodeSnm --tags ~@JUNIPER-PTX --tags ~@vHLRFE --tags ~@ESC --tags ~@MINI-LINK-6351 --tags ~@CPP\n--tags ~@CBP-OI --tags ~@MINI-LINK-Indoor --tags ~@RNC --tags ~@SDKNodeSnm --tags ~@JUNIPER-PTX --tags ~@vHLRFE --tags ~@ESC --tags ~@MINI-LINK-6351 --tags ~@CPP --tags ~@MSC\n--tags ~@CBP-OI --tags ~@MINI-LINK-Indoor --tags ~@RNC --tags ~@SDKNodeSnm --tags ~@JUNIPER-PTX --tags ~@vHLRFE --tags ~@ESC --tags ~@MINI-LINK-6351 --tags ~@CPP --tags ~@MSC --tags ~@vWMG\n--tags ~@CCDM --tags ~@CBP-OI --tags ~@MINI-LINK-Indoor --tags ~@RNC --tags ~@SDKNodeSnm --tags ~@JUNIPER-PTX --tags ~@vHLRFE --tags ~@ESC --tags ~@MINI-LINK-6351 --tags ~@CPP --tags ~@MSC --tags ~@vWMG\n--tags ~@StatsFileCollectionFinalization --tags ~@CCDM --tags ~@CBP-OI --tags ~@MINI-LINK-Indoor --tags ~@RNC --tags ~@SDKNodeSnm --tags ~@JUNIPER-PTX --tags ~@vHLRFE --tags ~@ESC --tags ~@MINI-LINK-6351 --tags ~@CPP --tags ~@MSC --tags ~@vWMG\n--tags @StatsFileRecovery\n--tags @StatsFileRecovery --tags ~@CBP-OI --tags ~@MINI-LINK-Indoor\n--tags @StatsFileRecovery --tags ~@CBP-OI --tags ~@MINI-LINK-Indoor --tags ~@CISCO-ASR9000\n--tags ~@5GRadioNode --tags ~@CBP-OI --tags ~@MINI-LINK-Indoor --tags @StatsFileCollection --tags ~@CISCO-ASR9000\n--tags ~@5GRadioNode --tags ~@CBP-OI --tags ~@MINI-LINK-Indoor --tags @StatsFileCollection --tags ~@CISCO-ASR9000 --tags ~@vHLRFE\n--tags @SAPC --tags @Collection\n--tags @HLRFE-BSP,@HLRFE-IS,@vHLRFE,@BSC,@MSC-BC-IS,@MSC-BC-BSP,@HLRFE --tags @Stats --tags @Collection --tags @StatsFileCollection,@StatsFileRecovery\n--tags @SAPC --tags @Stats --tags @Collection --tags @StatsFileCollection,@StatsFileRecovery\n--tags @SAPC --tags @Stats --tags @Collection --tags @StatsFileCollection\n--tags @SAPC,@vSAPC --tags @Stats --tags @Collection --tags @StatsFileCollection --tags @StatsFileRecovery\n--tags @SAPC,@vSAPC\n--tags @SDKNodeSnmp --tags @StatsFileCollectionSnmp --tags @Collection\n--tags @5GRadioNode\n--tags @vMSC --tags @Stats --tags @Collection --tags @StatsFileCollection,@StatsFileRecovery\n--tags @Router6675,@Router6274,@Router6x71,@Router6672\n--tags @IPWorks  --tags @Stats --tags @Collection --tags @StatsFileCollection\n--tags @Router6672,@Router6274,@Router6x71\n--tags @IPWorks  --tags @Stats --tags @Collection --tags @StatsFileCollection --tags @StatsFileRecovery\n--tags @vAFG\n--tags @IPWorks  --tags @Stats --tags @Collection\n--tags @HLRFE-BS,@HLRFE-IS,@MSC-BC-BSP,@MSC-BC-IS,@BSC,@vHLRFE,@vMSC --tags @Stats --tags @Collection --tags @StatsFileCollection,@StatsFileRecovery\n--tags @IPWorks  --tags @Stats --tags @Collection --tags @StatsFileRecovery\n--tags @IPWorks,@UPG,@HSSFE,@vUPG  --tags @Stats --tags @Collection\n--tags @IPWorks --tags @Stats --tags @Collection\n--tags @HLRFE-BS,@HLRFE-IS,@MSC-BC-BSP,@MSC-BC-IS,@BSC,@vHLRFE,@vMSC,@vMSC-HC --tags @Stats --tags @Collection --tags @StatsFileCollection,@StatsFileRecovery\n--tags @Router6672,@Router6274\n--tags @BSC --tags @Mtr --tags @Collection --tags @MtrFileCollection\n--tags @HLRFE-BS,@HLRFE-IS,@MSC-BC-BSP,@MSC-BC-IS,@BSC,@vHLRFE,@vMSC,@vMSC-HC --tags @Stats,@Mtr --tags @Collection --tags @StatsFileCollection,@StatsFileRecovery,@MtrFileCollection\n--tags @UPG --tags @Stats --tags @Collection\n--tags @MSC-BC-BSP --tags @Stats --tags @Collection --tags @StatsFileCollection\n--tags @HSSFE --tags @Stats --tags @Collection\n--tags @UPG,@vUPG,@HSSFE,@IPWorks --tags @Stats --tags @Collection\n--tags @HSSFE,@IPWorks --tags @Stats --tags @Collection\n--tags @UPG,@HSSFE,@IPWorks --tags @Stats --tags @Collection\n--tags @FRONTHAUL-6020\n--tags @RNC,@CPP --tags @Stats --tags @StatsSubscription\n@BSC @Mtr @Collection @MtrFileCollection\n--tags @FRONTHAUL-6080\n--tags @BSC @Rpmo @RpmoSubscription @Rtt @RttSubscription @ScannerPolling\n--tags @BSC --tags @Rpmo --tags @RpmoSubscription --tags @Rtt --tags @RttSubscription\n--tags @BSC --tags @MSC --tags ~@Rpmo --tags~@RpmoSubscription --tags ~@Rtt â€“tags ~@RttSubscription\n--tags ~@CBP-OI --tags ~@MINI-LINK-Indoor --tags @RNC\n--tags @SCU\n--tags @BSC --tags @Mtr'
    // extract ARQUILLIAN_CUCUMBER_OPTIONS from pipeline_local.cfg located in development repository
    def cmd1='git archive --remote=ssh://gerrit.ericsson.se:29418/'+project_name+' HEAD pipeline_local.cfg'
    def cmd2='tar --extract --to-stdout'
    def proc = cmd1.execute() | cmd2.execute()

    def std_out = new StringBuffer()
    proc.consumeProcessErrorStream(std_out)
    def local_file = proc.text
    
    local_file.eachLine {
        String line ->
            if (line.contains("ARQUILLIAN_CUCUMBER_OPTIONS")) {
               String arquillian_cucumber_options_value = line.split("=", 2)[1]
            }
    }
    return arquillian_cucumber_options_value
}