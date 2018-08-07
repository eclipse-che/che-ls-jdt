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
package org.eclipse.che.jdt.ls.extension.core.internal.externallibrary;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ls.core.internal.IContentProvider;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;

/**
 * An IContentProvider for elements in jar files for non-classfiles. The corresponding uris are
 * generated in {@link LibraryNavigation#convertToJarEntry()}
 *
 * @author Thomas Mäder
 */
public class LibraryContentProvider implements IContentProvider {

  @Override
  public String getContent(URI uri, IProgressMonitor monitor) throws CoreException {
    try {
      String parentId = URLDecoder.decode(uri.getAuthority(), "UTF-8");
      IJavaElement parent = JavaCore.create(parentId);
      IPath path = new Path(uri.getPath());
      IStorage res = findIn(parent, path);

      if (res != null) {
        try (InputStream stream = res.getContents()) {
          return IOUtils.toString(stream, "UTF-8");
        } catch (IOException e) {
          JavaLanguageServerPlugin.logException("Can't read file content: " + res.getFullPath(), e);
        }
      }
    } catch (UnsupportedEncodingException e) {
      // utf-8 is mandatory
      throw new RuntimeException("Should not happen", e);
    }

    return null;
  }

  private IStorage findIn(IJavaElement parent, IPath path) throws JavaModelException {
    if (parent instanceof IPackageFragmentRoot) {
      return findInResources(((IPackageFragmentRoot) parent).getNonJavaResources(), path);
    } else if (parent instanceof IPackageFragment) {
      return findInResources(((IPackageFragment) parent).getNonJavaResources(), path);
    }
    return null;
  }

  private IStorage findInResources(Object[] resources, IPath path) {
    for (Object resource : resources) {
      if (resource instanceof IJarEntryResource) {
        IJarEntryResource res = (IJarEntryResource) resource;
        if (res.getName().equals(path.segment(0))) {
          if (path.segmentCount() == 1) {
            return res;
          } else {
            return findInResources(res.getChildren(), path.removeFirstSegments(1));
          }
        }
      }
    }

    return null;
  }
}
