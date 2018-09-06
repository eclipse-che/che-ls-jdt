/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.jdt.ls.extension.api;

/**
 * Defines constants related to custom notifications
 *
 * @author Thomas Mäder
 */
public class Notifications {

  // CLASSPATH updater
  public static final String UPDATE_PROJECTS_CLASSPATH =
      "che.jdt.ls.extension.workspace.clientUpdateProjectsClasspath";
  // Project updater
  public static final String UPDATE_PROJECT = "che.jdt.ls.extension.workspace.clientUpdateProject";
  public static final String UPDATE_ON_PROJECT_CLASSPATH_CHANGED =
      "che.jdt.ls.extension.workspace.clientUpdateOnProjectClasspathChanged";
  public static final String UPDATE_MAVEN_MODULE =
      "che.jdt.ls.extension.workspace.clientUpdateMavenModule";
  public static final String UPDATE_PROJECT_CONFIG =
      "che.jdt.ls.extension.workspace.clientUpdateProjectConfig";
}
