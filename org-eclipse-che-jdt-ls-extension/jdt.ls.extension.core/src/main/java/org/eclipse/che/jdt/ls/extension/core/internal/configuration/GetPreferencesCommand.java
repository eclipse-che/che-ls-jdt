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
import static org.eclipse.jdt.ls.core.internal.preferences.Preferences.CONFIGURATION_UPDATE_BUILD_CONFIGURATION_KEY;
import static org.eclipse.jdt.ls.core.internal.preferences.Preferences.ERRORS_INCOMPLETE_CLASSPATH_SEVERITY_KEY;
import static org.eclipse.jdt.ls.core.internal.preferences.Preferences.EXECUTE_COMMAND_ENABLED_KEY;
import static org.eclipse.jdt.ls.core.internal.preferences.Preferences.IMPLEMENTATIONS_CODE_LENS_ENABLED_KEY;
import static org.eclipse.jdt.ls.core.internal.preferences.Preferences.IMPORT_GRADLE_ENABLED;
import static org.eclipse.jdt.ls.core.internal.preferences.Preferences.IMPORT_MAVEN_ENABLED;
import static org.eclipse.jdt.ls.core.internal.preferences.Preferences.JAVA_COMPLETION_FAVORITE_MEMBERS_KEY;
import static org.eclipse.jdt.ls.core.internal.preferences.Preferences.JAVA_FORMAT_ENABLED_KEY;
import static org.eclipse.jdt.ls.core.internal.preferences.Preferences.JAVA_IMPORT_EXCLUSIONS_KEY;
import static org.eclipse.jdt.ls.core.internal.preferences.Preferences.MAVEN_USER_SETTINGS_KEY;
import static org.eclipse.jdt.ls.core.internal.preferences.Preferences.MEMBER_SORT_ORDER;
import static org.eclipse.jdt.ls.core.internal.preferences.Preferences.PREFERRED_CONTENT_PROVIDER_KEY;
import static org.eclipse.jdt.ls.core.internal.preferences.Preferences.REFERENCES_CODE_LENS_ENABLED_KEY;
import static org.eclipse.jdt.ls.core.internal.preferences.Preferences.RENAME_ENABLED_KEY;
import static org.eclipse.jdt.ls.core.internal.preferences.Preferences.SIGNATURE_HELP_ENABLED_KEY;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.eclipse.che.jdt.ls.extension.api.dto.JdtLsPreferences;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.preferences.MemberSortOrder;
import org.eclipse.jdt.ls.core.internal.preferences.PreferenceManager;
import org.eclipse.jdt.ls.core.internal.preferences.Preferences;

/** @author Anatolii Bazko */
public class GetPreferencesCommand {

  /**
   * Returns JDT LS preferences.
   *
   * @see PreferenceManager#getPreferences()
   */
  public static JdtLsPreferences execute(List<Object> params, IProgressMonitor pm) {
    ensureNotCancelled(pm);

    PreferenceManager preferencesManager = JavaLanguageServerPlugin.getPreferencesManager();
    Preferences prefs = preferencesManager.getPreferences();

    return new JdtLsPreferences(toMap(prefs));
  }

  private static Map<String, String> toMap(Preferences prefs) {
    Map<String, Object> prefsAsMap = prefs.asMap();
    if (prefsAsMap == null) {
      prefsAsMap = new HashMap<>();

      putNotNullValue(
          prefsAsMap,
          ERRORS_INCOMPLETE_CLASSPATH_SEVERITY_KEY,
          prefs::getIncompleteClasspathSeverity);

      putNotNullValue(
          prefsAsMap,
          CONFIGURATION_UPDATE_BUILD_CONFIGURATION_KEY,
          prefs::getUpdateBuildConfigurationStatus);

      prefsAsMap.put(IMPORT_GRADLE_ENABLED, prefs.isImportGradleEnabled());
      prefsAsMap.put(IMPORT_MAVEN_ENABLED, prefs.isImportMavenEnabled());
      prefsAsMap.put(REFERENCES_CODE_LENS_ENABLED_KEY, prefs.isCodeLensEnabled());
      prefsAsMap.put(
          IMPLEMENTATIONS_CODE_LENS_ENABLED_KEY, prefs.isImplementationsCodeLensEnabled());
      prefsAsMap.put(JAVA_FORMAT_ENABLED_KEY, prefs.isJavaFormatEnabled());
      prefsAsMap.put(SIGNATURE_HELP_ENABLED_KEY, prefs.isSignatureHelpEnabled());
      prefsAsMap.put(RENAME_ENABLED_KEY, prefs.isRenameEnabled());
      prefsAsMap.put(EXECUTE_COMMAND_ENABLED_KEY, prefs.isExecuteCommandEnabled());

      putNotNullValue(prefsAsMap, JAVA_IMPORT_EXCLUSIONS_KEY, prefs::getJavaImportExclusions);
      putNotNullValue(prefsAsMap, MAVEN_USER_SETTINGS_KEY, prefs::getMavenUserSettings);

      MemberSortOrder memberSortOrder = prefs.getMemberSortOrder();
      if (memberSortOrder != null) {
        StringBuilder sortOrder = new StringBuilder(MemberSortOrder.N_CATEGORIES);

        for (int kind = 0; kind < MemberSortOrder.N_CATEGORIES; kind++) {
          sortOrder.append(memberSortOrder.getCategoryIndex(kind));
        }
        prefsAsMap.put(MEMBER_SORT_ORDER, sortOrder.toString());
      }

      putNotNullValue(
          prefsAsMap,
          JAVA_COMPLETION_FAVORITE_MEMBERS_KEY,
          prefs::getJavaCompletionFavoriteMembers);
      putNotNullValue(
          prefsAsMap, PREFERRED_CONTENT_PROVIDER_KEY, prefs::getPreferredContentProviderIds);
    }

    return prefsAsMap
        .entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
  }

  private static void putNotNullValue(
      Map<String, Object> prefsAsMap, String key, Supplier<Object> value) {

    Object obj = value.get();
    if (obj != null) {
      prefsAsMap.put(key, obj);
    }
  }
}
