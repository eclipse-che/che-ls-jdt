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

/**
 * Progress report class copied from
 * https://github.com/eclipse/eclipse.jdt.ls/blob/master/org.eclipse.jdt.ls.core/src/org/eclipse/jdt/ls/core/internal/ProgressReport.java
 *
 * @author Valeriy Svydenko
 */
public class ProgressReport {

  private String id;

  private String task;

  private String subTask;

  private String status;

  private int totalWork;

  private int workDone;

  private boolean complete;

  public ProgressReport() {}

  /** @return the task */
  public String getTask() {
    return task;
  }

  /** @param task the task to set */
  public void setTask(String task) {
    this.task = task;
  }

  /** @return the status */
  public String getStatus() {
    return status;
  }

  /** @param status the status to set */
  public void setStatus(String status) {
    this.status = status;
  }

  /** @return the complete */
  public boolean isComplete() {
    return complete;
  }

  /** @param complete the complete to set */
  public void setComplete(boolean complete) {
    this.complete = complete;
  }

  /** @return the totalWork */
  public int getTotalWork() {
    return totalWork;
  }

  /** @param totalWork the totalWork to set */
  public void setTotalWork(int totalWork) {
    this.totalWork = totalWork;
  }

  /** @return the workDone */
  public int getWorkDone() {
    return workDone;
  }

  /** @param workDone the workDone to set */
  public void setWorkDone(int workDone) {
    this.workDone = workDone;
  }

  /** @return the progress id */
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  /** @return the subTask name */
  public String getSubTask() {
    return subTask;
  }

  /** @param subTask the subTask to set */
  public void setSubTask(String subTask) {
    this.subTask = subTask;
  }
}
