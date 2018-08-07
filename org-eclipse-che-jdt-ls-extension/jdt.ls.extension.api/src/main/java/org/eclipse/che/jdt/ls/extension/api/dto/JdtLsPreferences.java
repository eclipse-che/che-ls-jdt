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
package org.eclipse.che.jdt.ls.extension.api.dto;

import java.util.HashMap;
import java.util.Map;

/** @author Anatolii Bazko */
public class JdtLsPreferences {
  private Map<String, String> preferences;

  public JdtLsPreferences(Map<String, String> preferences) {
    this.preferences = preferences;
  }

  public JdtLsPreferences() {
    this.preferences = new HashMap<>();
  }

  public Map<String, String> getPreferences() {
    return preferences;
  }

  public void setPreferences(Map<String, String> preferences) {
    this.preferences = preferences;
  }
}
