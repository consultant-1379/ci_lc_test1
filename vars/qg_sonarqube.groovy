#!/usr/bin/env groovy

import static com.ericsson.oss.de.pipeline.GlobalVars.SONARQUBE_CONFIG
import static com.ericsson.oss.de.pipeline.GlobalVars.MAVEN_CONFIG

def call(args) {

  def mavenConfig = args?.mvnConfig ? args.mvnConfig : MAVEN_CONFIG
  def sonarConfig = args?.sonarConfig ? args.sonarConfig : SONARQUBE_CONFIG
  withSonarQubeEnv(sonarConfig) {
    def mvnArgs = "sonar:sonar -Dsonar.login=${SONAR_AUTH_TOKEN} -Dsonar.host.url=${SONAR_HOST_URL}"

    if (args.branchName) mvnArgs += " -Dsonar.branch.name='$args.branchName'"
    if (args.targetBranch) mvnArgs += " -Dsonar.branch.target='$args.targetBranch'"
    if (args.projectVersion) mvnArgs += " -Dsonar.projectVersion='$args.projectVersion'"
    if (args.previewMode) mvnArgs += " -Dsonar.analysis.mode=preview -Dsonar.report.export.path=sonar-report.json"

    withMaven(maven: mavenConfig) {
      // Run the maven build
      sh "mvn $mvnArgs"
    }

  }
}
