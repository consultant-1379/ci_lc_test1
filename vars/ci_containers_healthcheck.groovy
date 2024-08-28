#!/usr/bin/env groovy

def call() {
  String statusUnH = "unhealthy"
  String statusStart = "starting"
  noUnHealthy = 0
  noStarting = 0

  for (i = 0; i <=60; i++) {
    def status = sh(script: 'docker ps --format {{.Status}}', returnStdout: true).trim()
    def matcherUnH = status =~/\b${statusUnH}\b/
    def matcherStart = status =~/\b${statusStart}\b/
    noUnHealthy = matcherUnH.size()
    noStarting = matcherStart.size()
    matcherUnH = null
    matcherStart = null
    println ' ****** attempt ' +i+ ' ******* '
    if(noUnHealthy==0 && noStarting==0){
        println 'All containers are healthy'
        break
    }
    else if(noStarting>=1){
        println 'Number of "Starting" ' + noStarting
        Thread.sleep(5000)
        continue
    }
    else if(noUnHealthy>=1){
        println 'Number of "Unhealthy" ' + noUnHealthy
        sh 'exit 1'
        break
    }

  }
  if (noStarting>=1){
      println 'Number of containers still in "Starting" state ' + noStarting
      sh 'exit 1'
  }
}