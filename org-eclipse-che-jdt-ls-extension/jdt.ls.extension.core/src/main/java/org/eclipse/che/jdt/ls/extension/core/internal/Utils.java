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
