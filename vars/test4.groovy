#!/usr/bin/env groovy

def call(String portalURL){
   echo 'Performing test step'
	 env.PACKAGE_NAME="ERICaaa"
   env.PACKAGE_VERSION="1.2.3"
	 env.DG_CREATED=true
	 env.TEAM_EMAIL='test@ttt.ttt'
	 env.DG_OUTPUT='[{"drop": "16.9","deliveryGroup": "7332","result": "Success - Delivery Group was Created","warnings": ""}]'

	if (!portalURL){
			portalURL="aabbcc"
		}

	println("https://"+portalURL+"/ddddd")
}


