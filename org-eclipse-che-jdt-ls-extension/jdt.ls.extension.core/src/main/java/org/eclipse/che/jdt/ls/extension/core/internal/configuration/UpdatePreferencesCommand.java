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
import static org.eclipse.che.jdt.ls.extension.core.internal.Utils.ensureNotCancelled;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.api.dto.JdtLsPreferences;
import org.eclipse.che.jdt.ls.extension.core.internal.GsonUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.preferences.PreferenceManager;
import org.eclipse.jdt.ls.core.internal.preferences.Preferences;

/** @author Anatolii Bazko */
public class UpdatePreferencesCommand {

  private static final Gson GSON = GsonUtils.getInstance();

  /** Updates JST LS configuration. */
  public static Boolean execute(List<Object> params, IProgressMonitor pm) {
    validateParams(params);
    ensureNotCancelled(pm);

    JdtLsPreferences currentPrefs = GetPreferencesCommand.execute(emptyList(), pm);
    JdtLsPreferences newPrefs = GSON.fromJson(GSON.toJson(params.get(0)), JdtLsPreferences.class);

    merge(currentPrefs, newPrefs);
    updatePreferences(currentPrefs);

    return true;
  }

  private static void merge(JdtLsPreferences currentPrefs, JdtLsPreferences newPrefs) {
    newPrefs
        .getPreferences()
        .forEach(
            (key, value) -> {
              if ("NULL".equalsIgnoreCase(value)) {
                currentPrefs.getPreferences().remove(key);
              } else {
                currentPrefs.getPreferences().put(key, value);
              }
            });
  }

  private static void updatePreferences(JdtLsPreferences jdtLsPreferences) {
    PreferenceManager preferencesManager = JavaLanguageServerPlugin.getPreferencesManager();
    Preferences preferences =
        Preferences.createFrom(new HashMap<>(jdtLsPreferences.getPreferences()));
    preferencesManager.update(preferences);
  }

  private static void validateParams(List<Object> params) {
    Preconditions.checkArgument(!params.isEmpty(), JdtLsPreferences.class.getName() + " expected.");
  }
}
