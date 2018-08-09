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
package org.eclipse.che.jdt.ls.extension.core.internal;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.MavenPlugin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/** The activator class controls the plug-in life cycle */
public class ExtensionActivator implements BundleActivator {

  // The plug-in ID
  public static final String PLUGIN_ID = "jdt.ls.extension.core";

  // The shared instance
  private static ExtensionActivator plugin;
  private static final JavaModelEventProvider javaModelEventProvider = new JavaModelEventProvider();
  private static final MavenProjectConfigurator mavenProjectConfigurator =
      new MavenProjectConfigurator();

  public void start(BundleContext context) throws Exception {
    plugin = this;
    JavaCore.addElementChangedListener(javaModelEventProvider);
    MavenPlugin.getMavenProjectRegistry().addMavenProjectChangedListener(mavenProjectConfigurator);
  }

  public void stop(BundleContext context) throws Exception {
    MavenPlugin.getMavenProjectRegistry()
        .removeMavenProjectChangedListener(mavenProjectConfigurator);
    JavaCore.removeElementChangedListener(javaModelEventProvider);
    plugin = null;
  }

  /**
   * Returns the shared instance
   *
   * @return the shared instance
   */
  public static ExtensionActivator getDefault() {
    return plugin;
  }
}
