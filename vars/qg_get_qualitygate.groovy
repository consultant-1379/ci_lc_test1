#!/usr/bin/env groovy
import static com.ericsson.oss.de.pipeline.GlobalVars.*

def call(args) {
    timeout(time: args.timeout, unit: 'MINUTES') {
        withSonarQubeEnv(SONARQUBE_CONFIG) {
            return waitForQualityGate()
        }
    }
}