/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.jdt.ls.extension.core.internal.pom;

import static java.util.Collections.singletonList;
import static org.eclipse.che.jdt.ls.extension.core.internal.pom.EffectivePomHandler.getEffectivePom;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.eclipse.che.jdt.ls.extension.core.internal.AbstractProjectsManagerBasedTest;
import org.eclipse.che.jdt.ls.extension.core.internal.WorkspaceHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Before;
import org.junit.Test;

/** @author Mykola Morhun */
public class EffectivePomHandlerTest extends AbstractProjectsManagerBasedTest {
  private IProject project;

  @Before
  public void setup() throws Exception {
    importProjects("maven/testproject");
    project = WorkspaceHelper.getProject("testproject");
  }

  @Test
  public void shouldGetEffectivePom() throws Exception {
    final String[] CHECKPOINTS = getEffectivePomCheckpoints();
    final List<Object> arguments = singletonList("file://" + project.getLocation().toString());

    final String effectivePom = getEffectivePom(arguments, new NullProgressMonitor());

    assertTrue(effectivePom.length() > 0);
    for (String checkpoint : CHECKPOINTS) {
      assertTrue(effectivePom.contains(checkpoint));
    }
  }

  private String[] getEffectivePomCheckpoints() throws IOException {
    InputStream expectedEffectivePomInputStream =
        getClass().getResourceAsStream("effective-pom-checkpoints.txt");
    String effectivePom = IOUtils.toString(expectedEffectivePomInputStream, "UTF-8");
    return effectivePom.split("\\r?\\n");
  }
}
