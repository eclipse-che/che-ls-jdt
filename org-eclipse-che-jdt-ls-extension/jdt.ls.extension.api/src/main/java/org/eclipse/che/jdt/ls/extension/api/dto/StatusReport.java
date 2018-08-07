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

/**
 * Status report class copied from
 * https://github.com/eclipse/eclipse.jdt.ls/blob/master/org.eclipse.jdt.ls.core/src/org/eclipse/jdt/ls/core/internal/StatusReport.java
 *
 * @author Thomas Mäder
 */
public class StatusReport {

  /** The message type. See { */
  private String type;
  /** The actual message */
  private String message;

  /**
   * The message type. See {
   *
   * @return The type
   */
  public String getType() {
    return type;
  }

  /**
   * The message type. See {
   *
   * @param type The type
   */
  public void setType(String type) {
    this.type = type;
  }

  public StatusReport withType(String type) {
    this.type = type;
    return this;
  }

  /**
   * The actual message
   *
   * @return The message
   */
  public String getMessage() {
    return message;
  }

  /**
   * The actual message
   *
   * @param message The message
   */
  public void setMessage(String message) {
    this.message = message;
  }
}
