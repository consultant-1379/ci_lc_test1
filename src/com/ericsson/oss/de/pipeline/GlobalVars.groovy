#!/usr/bin/env groovy
package com.ericsson.oss.de.pipeline

class GlobalVars {

  /**
   * Default SonarQube configuration
   */
  static final String SONARQUBE_CONFIG = "sonarqube enterprise 77"

  /**
   * Default Maven configuration
   */
  static final String MAVEN_CONFIG = "Maven 3.0.5"

  /**
   * Defualt user used by the auto delivery queue
   */
  static final String DEFAULT_USER_DELIVERY_QUEUE = "lciadm100"

  static final String CI_PORTAL_HOST = "atclvm1224.athtem.eei.ericsson.se"

}
