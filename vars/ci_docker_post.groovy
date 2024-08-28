#!/usr/bin/env groovy

def call() {
    sh '''
       if hash docker 2>/dev/null; then
            echo "docker post actions and cleanup"
            sleep 5
            docker-compose  -f ${DOCKER_COMPOSE_FILE} logs > docker-compose.log || true
            docker-compose  -f ${DOCKER_COMPOSE_FILE} down || true
            running=$(docker ps -a -q| wc -l)
                if [ "$running" -gt "0" ];then
                    docker stop $(docker ps -q) || true
                    docker rm -f $(docker ps -q) || true
                fi
            ls -l
            env
       fi
   '''
}