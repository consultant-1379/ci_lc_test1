def init_metrics() {
    """
    Function initialises global variables.
    """
    environment = [
            previousStageEnd = 0,
            stageMethodFail = [:],
            stageTimes = [:],
    ]
}

def generate_metrics() {
    """
    Function generates metrics based on stage execution duration minus previous stage execution duration.
    """
    try {
        stageTimes[env.STAGE_NAME] = currentBuild.duration - (previousStageEnd as int)
        previousStageEnd = currentBuild.duration
    } catch(Exception exception) {
        if (stageMethodFail["generate_metrics"] == null) {
            stageMethodFail["generate_metrics"] = [env.STAGE_NAME]
        } else {
            stageMethodFail.generate_metrics.add(env.STAGE_NAME)
        }
    }
}

def generate_testrunner_metrics() {
    """
    Function pre-process the 'STAGE_DURATION' values from environment file and store those values in stageTimes before sending to influx db.
    """
    def stage_duration = sh script: 'cat env.txt | grep "STAGE_DURATION"', returnStdout: true
    stage_duration = stage_duration.trim()
    def stage_values = stage_duration.tokenize('=')
    stage_values = stage_values[1].replaceAll("\\{", "[").replaceAll("\\}","]");
    def stage_values_map = new GroovyShell().evaluate(stage_values)
    stage_values_map.each{ key, value -> println "${key}:${value}"
        stageTimes[key] = value
    }
}

def send_metrics() {
    """
    Function sends generated metrics to grafana influx db.
    """
    try {
        step([$class: 'InfluxDbPublisher', target: 'grafana', customPrefix: null, customData: stageTimes])
    } catch(Exception exception) {
        if (stageMethodFail["send_metrics"] == null) {
            stageMethodFail["send_metrics"] = [env.STAGE_NAME]
        } else {
            stageMethodFail.send_metrics.add(env.STAGE_NAME)
        }
    }
    verify_metrics_and_send_email()
}

def verify_metrics_and_send_email() {
    try {
        if (stageMethodFail != null && stageMethodFail.size() != 0) {
            mail bcc: '', body: "<p><strong> The failure of generation or send of metrics has occured in the following stages ${stageMethodFail}! </strong><br /><br /><strong>Job</strong>: ${env.JOB_NAME} <br /><strong>Build Number</strong>: ${env.BUILD_NUMBER} <br /><strong>Build URL</strong>: ${env.BUILD_URL}", cc: '', charset: 'UTF-8', from: 'axisadm@fem142-eiffel004.lmera.ericsson.se', mimeType: 'text/html', replyTo: '', subject: "Failed to generate or send metrics:  ${env.JOB_NAME}", to: "PDLCIAXISC@pdl.internal.ericsson.com"
        }
    } catch(Exception exception) {
        echo "ERROR sending notification email for failure of generate_metrics."
    }
}
