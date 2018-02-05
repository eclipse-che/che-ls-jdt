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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/** Utils class for common methods. */
public class Utils {

  /**
   * Checks whether given task cancelled and if so throws {@code OperationCanceledException}
   *
   * @param progressMonitor progress monitor to check cancellation
   */
  public static void ensureNotCancelled(IProgressMonitor progressMonitor) {
    if (progressMonitor.isCanceled()) {
      throw new OperationCanceledException();
    }
  }
}
