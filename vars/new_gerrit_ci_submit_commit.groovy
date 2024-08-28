#!/usr/bin/env groovy

def call(){
	if (env.GERRIT_CHANGE_NUMBER) {
		sh '''
			ssh -i ~/.ssh/id_rsa_axisadm -p 29418 axisadm@gerrit.sero.gic.ericsson.se gerrit review --verified 0 --project $GERRIT_PROJECT $GERRIT_PATCHSET_REVISION
			ssh -p 29418 lciadm100@gerrit.sero.gic.ericsson.se gerrit review --verified +1 --label 'Vertical-Slice=+1' --project $GERRIT_PROJECT $GERRIT_PATCHSET_REVISION
          		ssh -p 29418 lciadm100@gerrit.sero.gic.ericsson.se gerrit review --submit --project $GERRIT_PROJECT $GERRIT_PATCHSET_REVISION
            sleep 10
		'''
        }
}
