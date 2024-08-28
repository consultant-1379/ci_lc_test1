#!/usr/bin/env groovy

def call(){
if (env.DG_CREATED!=null){
	if (env.GERRIT_PATCHSET_UPLOADER_EMAIL!=null || env.TEAM_EMAIL !=null){
		to_email=''
		if (env.GERRIT_PATCHSET_UPLOADER_EMAIL!=null){
			to_email=env.GERRIT_PATCHSET_UPLOADER_EMAIL
    }
		if (env.TEAM_EMAIL !=null){
			if (to_email==''){
				to_email=env.TEAM_EMAIL 
      } else{
				to_email=to_email+','+env.TEAM_EMAIL 
      } 
		}
    mail body: '''Delivery Group Created and Delivered

Package: '''+env.PACKAGE_NAME+'''
Version: '''+env.PACKAGE_VERSION+'''
Delivery Group Information: '''+env.DG_OUTPUT,
		from: 'axisadm@lmera.ericsson.se',
    subject: 'Delivery Group Created',
    to: to_email
  }
}

}



