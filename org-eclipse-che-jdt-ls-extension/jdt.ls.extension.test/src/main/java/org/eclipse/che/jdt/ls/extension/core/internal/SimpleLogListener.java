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

import com.google.common.base.Throwables;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;

public class SimpleLogListener implements ILogListener {

  private List<IStatus> messages;

  public SimpleLogListener() {
    messages = new ArrayList<>();
  }

  @Override
  public void logging(IStatus status, String plugin) {
    messages.add(status);
  }

  public List<IStatus> getStatuses() {
    return messages;
  }

  public List<String> getErrors() {
    return getMessages(IStatus.ERROR);
  }

  public List<String> getInfos() {
    return getMessages(IStatus.INFO);
  }

  public List<String> getWarnings() {
    return getMessages(IStatus.WARNING);
  }

  public List<String> getMessages(int severity) {
    return getStatuses()
        .stream()
        .filter(s -> s.getSeverity() == severity)
        .map(this::convert)
        .collect(Collectors.toList());
  }

  private String convert(IStatus status) {
    StringBuilder s = new StringBuilder(status.getMessage());
    if (status.getException() != null) {
      String stackTrace = Throwables.getStackTraceAsString(status.getException());
      s.append("\n");
      s.append(stackTrace);
    }
    return s.toString();
  }
}
