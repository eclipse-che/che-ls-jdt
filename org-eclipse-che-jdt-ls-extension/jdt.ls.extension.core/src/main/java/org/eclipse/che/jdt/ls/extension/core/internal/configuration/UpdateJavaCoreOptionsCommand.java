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

import static java.util.Collections.emptyList;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import java.util.Hashtable;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.api.dto.JavaCoreOptions;
import org.eclipse.che.jdt.ls.extension.core.internal.GsonUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.JavaCore;

/** @author Anatolii Bazko */
public class UpdateJavaCoreOptionsCommand {

  private static final Gson GSON = GsonUtils.getInstance();

  /**
   * Updates Java Core options.
   *
   * @see JavaCore#setOptions(Hashtable)
   */
  public static Boolean execute(List<Object> params, IProgressMonitor pm) {
    validateParams(params);
    ensureNotCancelled(pm);

    JavaCoreOptions currentOptions = GetJavaCoreOptionsCommand.execute(emptyList(), pm);
    JavaCoreOptions newOptions = GSON.fromJson(GSON.toJson(params.get(0)), JavaCoreOptions.class);

    merge(currentOptions, newOptions);
    updateJavaCoreOptions(currentOptions);

    return true;
  }

  private static void merge(JavaCoreOptions currentOptions, JavaCoreOptions newOptions) {
    newOptions
        .getOptions()
        .forEach(
            (key, value) -> {
              if ("NULL".equalsIgnoreCase(value)) {
                currentOptions.getOptions().remove(key);
              } else {
                currentOptions.getOptions().put(key, value);
              }
            });
  }

  private static void updateJavaCoreOptions(JavaCoreOptions javaCoreOptions) {
    JavaCore.setOptions(new Hashtable<>(javaCoreOptions.getOptions()));
  }

  private static void validateParams(List<Object> params) {
    Preconditions.checkArgument(!params.isEmpty(), JavaCoreOptions.class.getName() + " expected.");
  }

  private static void ensureNotCancelled(IProgressMonitor pm) {
    if (pm.isCanceled()) {
      throw new OperationCanceledException();
    }
  }
}
