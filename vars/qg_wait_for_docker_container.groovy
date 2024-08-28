#!/usr/bin/env groovy

def call(args) {

    timeout(time: args.timeout, unit: 'MINUTES') {
        // Checks Docker health status until is "healthy".
        sh "jboss_status=\$(docker inspect ${args.containerName} -f \'{{ json .State.Health.Status }}\') \n" +
            '''while [[ $jboss_status != '"healthy"' ]] ''' + "\n" +
            "do \n"+
            "  echo \"Waiting for JBoss to be Ready\" \n" +
            "  sleep 2; \n" +
            "  jboss_status=\$(docker inspect ${args.containerName} -f \'{{ json .State.Health.Status }}\')\n" +
            "done"
    }

}