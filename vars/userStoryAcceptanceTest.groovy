
def call() {
    def String labelForSlaves = ''
    node(labelForSlaves) {
        echo('Executing USAT')
        echo("GERRIT_PROJECT is $GERRIT_PROJECT")
        echo('hi guys')
        /* sh('''docker pull armdocker.rnd.ericsson.se/proj_oss_releases/enm/testrunner
            FILE=testrunner/testrunner.sh
            git archive --remote=ssh://gerrit.ericsson.se:29418/OSS/com.ericsson.oss.de/ci-pipeline-tooling HEAD "$FILE" | tar -xO "$FILE" > $(basename "$FILE")
            chmod 755 testrunner.sh
            GERRIT_USER='lciadm100'
            ./testrunner.sh --skip-clean-up --skip-report run
            ''') */
    }
}