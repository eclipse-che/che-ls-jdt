/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.jdt.ls.extension.core.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/** The activator class controls the plug-in life cycle */
public class ExtensionActivator implements BundleActivator {

  // The plug-in ID
  public static final String PLUGIN_ID = "jdt.ls.extension.core";

  // The shared instance
  private static ExtensionActivator plugin;

  public void start(BundleContext context) throws Exception {
    plugin = this;
  }

  public void stop(BundleContext context) throws Exception {
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
