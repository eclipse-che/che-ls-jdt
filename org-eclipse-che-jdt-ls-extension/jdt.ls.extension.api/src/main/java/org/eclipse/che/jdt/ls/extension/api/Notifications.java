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
  public static final String MAVEN_PROJECT_CREATED =
      "che.jdt.ls.extension.workspace.projectCreated";
}
