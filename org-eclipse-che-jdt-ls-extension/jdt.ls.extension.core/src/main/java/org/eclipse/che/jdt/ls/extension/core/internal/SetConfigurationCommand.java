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

import com.google.common.base.Preconditions;
import com.google.gson.Gson;

import org.eclipse.che.jdt.ls.extension.api.dto.JdtLsConfiguration;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.preferences.PreferenceManager;
import org.eclipse.jdt.ls.core.internal.preferences.Preferences;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** @author Anatolii Bazko */
public class SetConfigurationCommand {

  private static final Gson GSON = GsonUtils.getInstance();

  /**
   * Sets JST LS configuration.
   *
   * <p>{@link JavaModelManager#setOptions(Hashtable)}
   *
   * <p>{@link PreferenceManager#update(Preferences)}
   */
  public static Void execute(List<Object> params, IProgressMonitor pm) {
    validateParams(params);
    ensureNotCancelled(pm);

    JdtLsConfiguration configuration =
        GSON.fromJson(GSON.toJson(params.get(0)), JdtLsConfiguration.class);

    setPreferences(configuration.getPreferences());
    setJavaCoreOptions(configuration.getJavaCoreOptions());

    return null;
  }

  private static void setPreferences(Map<String, String> prefsAsMap) {
    PreferenceManager preferencesManager = JavaLanguageServerPlugin.getPreferencesManager();

    Preferences preferences =
        Preferences.createFrom(
            prefsAsMap
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

    preferencesManager.update(preferences);
  }

  private static void setJavaCoreOptions(Map<String, String> options) {
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
