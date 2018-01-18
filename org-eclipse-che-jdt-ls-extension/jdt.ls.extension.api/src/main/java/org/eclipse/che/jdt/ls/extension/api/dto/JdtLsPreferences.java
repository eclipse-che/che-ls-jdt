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
