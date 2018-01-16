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

import static java.util.Collections.emptyList;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.che.jdt.ls.extension.api.dto.JdtLsConfiguration;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.preferences.PreferenceManager;
import org.eclipse.jdt.ls.core.internal.preferences.Preferences;

/** @author Anatolii Bazko */
public class UpdateConfigurationCommand {

  private static final Gson GSON = GsonUtils.getInstance();

  /** Updates JST LS configuration. */
  public static Void execute(List<Object> params, IProgressMonitor pm) {
    validateParams(params);
    ensureNotCancelled(pm);

    JdtLsConfiguration currentConf = GetConfigurationCommand.execute(emptyList(), pm);
    JdtLsConfiguration updatedConf =
        GSON.fromJson(GSON.toJson(params.get(0)), JdtLsConfiguration.class);

    merge(currentConf.getJavaCoreOptions(), updatedConf.getJavaCoreOptions());
    merge(currentConf.getJdtLsPreferences(), updatedConf.getJdtLsPreferences());

    updateJavaCoreOptions(currentConf.getJavaCoreOptions());
    updatePreferences(currentConf.getJdtLsPreferences());

    return null;
  }

  private static void merge(Map<String, String> existedProps, Map<String, String> newProps) {
    newProps.forEach(
        (key, value) -> {
          if ("NULL".equalsIgnoreCase(value)) {
            existedProps.remove(key);
          } else {
            existedProps.put(key, value);
          }
        });
  }

  private static void updatePreferences(Map<String, String> prefs) {
    PreferenceManager preferencesManager = JavaLanguageServerPlugin.getPreferencesManager();

    Preferences preferences =
        Preferences.createFrom(
            prefs
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

    preferencesManager.update(preferences);
  }

  private static void updateJavaCoreOptions(Map<String, String> options) {
    JavaCore.setOptions(new Hashtable<>(options));
  }

  private static void validateParams(List<Object> params) {
    Preconditions.checkArgument(
        !params.isEmpty(), JdtLsConfiguration.class.getName() + " expected.");
  }

  private static void ensureNotCancelled(IProgressMonitor pm) {
    if (pm.isCanceled()) {
      throw new OperationCanceledException();
    }
  }
}
