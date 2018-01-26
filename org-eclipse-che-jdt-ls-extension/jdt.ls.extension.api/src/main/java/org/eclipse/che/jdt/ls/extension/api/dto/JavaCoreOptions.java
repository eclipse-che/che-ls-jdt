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
public class JavaCoreOptions {
  private Map<String, String> options;

  public JavaCoreOptions() {
    this.options = new HashMap<>();
  }

  public JavaCoreOptions(Map<String, String> options) {
    this.options = options;
  }

  public Map<String, String> getOptions() {
    return options;
  }

  public void setOptions(Map<String, String> options) {
    this.options = options;
  }
}
