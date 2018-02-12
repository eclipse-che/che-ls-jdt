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
package org.eclipse.che.jdt.ls.extension.core.internal.configuration;

import static org.eclipse.che.jdt.ls.extension.core.internal.Utils.ensureNotCancelled;

import java.util.Hashtable;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.api.dto.JavaCoreOptions;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaCore;

/** @author Anatolii Bazko */
public class GetJavaCoreOptionsCommand {

  /**
   * Returns Java Core options.
   *
   * @see JavaCore#getOptions()
   * @see JavaCore#getOption(String)
   * @param params list of specific java options to return otherwise all available options will be
   *     returned
   */
  public static JavaCoreOptions execute(List<Object> params, IProgressMonitor pm) {
    ensureNotCancelled(pm);
    Hashtable<String, String> options = getJavaCoreOptions(params);
    return new JavaCoreOptions(options);
  }

  private static Hashtable<String, String> getJavaCoreOptions(List<Object> filters) {
    if (filters.isEmpty()) {
      return JavaCore.getOptions();
    } else {
      Hashtable<String, String> javaCoreOptions = new Hashtable<>(filters.size());
      filters.forEach(
          key -> {
            String keyAsStr = key.toString();

            String option = JavaCore.getOption(keyAsStr);
            if (option != null) {
              javaCoreOptions.put(keyAsStr, option);
            }
          });
      return javaCoreOptions;
    }
  }
}
