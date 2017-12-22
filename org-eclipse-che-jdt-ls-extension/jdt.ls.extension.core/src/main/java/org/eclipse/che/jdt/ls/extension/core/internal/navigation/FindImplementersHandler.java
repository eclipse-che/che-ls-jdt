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
package org.eclipse.che.jdt.ls.extension.core.internal.navigation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.api.dto.navigation.ImplementersResponse;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICodeAssist;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.MethodOverrideTester;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.ResourceUtils;
import org.eclipse.jdt.ls.core.internal.handlers.DocumentSymbolHandler;
import org.eclipse.jdt.ls.core.internal.hover.JavaElementLabels;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.jsonrpc.json.adapters.CollectionTypeAdapterFactory;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EitherTypeAdapterFactory;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EnumTypeAdapterFactory;

public class FindImplementersHandler {
  private static final Gson gson =
      new GsonBuilder()
          .registerTypeAdapterFactory(new CollectionTypeAdapterFactory())
          .registerTypeAdapterFactory(new EitherTypeAdapterFactory())
          .registerTypeAdapterFactory(new EnumTypeAdapterFactory())
          .create();

  @SuppressWarnings("restriction")
  public static Object getImplementers(List<Object> parameters, IProgressMonitor pm) {
    TextDocumentPositionParams param =
        gson.fromJson(gson.toJson(parameters.get(0)), TextDocumentPositionParams.class);

    ITypeRoot typeRoot = JDTUtils.resolveTypeRoot(param.getTextDocument().getUri());

    ImplementersResponse implementersResponse = new ImplementersResponse();
    List<SymbolInformation> implementations = new ArrayList<>();
    implementersResponse.setImplementations(implementations);
    try {
      IJavaElement elementToSearch =
          JDTUtils.findElementAtSelection(
              typeRoot,
              param.getPosition().getLine(),
              param.getPosition().getCharacter(),
              JavaLanguageServerPlugin.getPreferencesManager(),
              pm);

      switch (elementToSearch.getElementType()) {
        case IJavaElement.TYPE: // type
          findSubTypes(elementToSearch, implementations, pm);
          implementersResponse.setSearchedElement(elementToSearch.getElementName());
          break;
        case IJavaElement.METHOD: // method
          findTypesWithSubMethods(elementToSearch, implementations, pm);
          implementersResponse.setSearchedElement(elementToSearch.getElementName());
          break;
        default:
          break;
      }
    } catch (JavaModelException e) {
      throw new RuntimeException(e);
    }

    return implementersResponse;
  }

  private static IJavaElement getJavaElement(IJavaProject project, String fqn, int offset)
      throws JavaModelException {
    IJavaElement originalElement = null;
    IType type = project.findType(fqn);
    ICodeAssist codeAssist;
    if (type.isBinary()) {
      codeAssist = type.getClassFile();
    } else {
      codeAssist = type.getCompilationUnit();
    }

    IJavaElement[] elements = null;
    if (codeAssist != null) {
      elements = codeAssist.codeSelect(offset, 0);
    }

    if (elements != null && elements.length > 0) {
      originalElement = elements[0];
    }
    return originalElement;
  }

  private static void findSubTypes(
      IJavaElement element, List<SymbolInformation> implementations, IProgressMonitor pm)
      throws JavaModelException {
    IType type = (IType) element;
    ITypeHierarchy typeHierarchy = type.newTypeHierarchy(pm);
    IType[] implTypes = typeHierarchy.getAllSubtypes(type);

    for (IType implType : implTypes) {
      SymbolInformation dto = convertToSymbolInformation(implType);
      implementations.add(dto);
    }
  }

  @SuppressWarnings("restriction")
  private static void findTypesWithSubMethods(
      IJavaElement element, List<SymbolInformation> implementations, IProgressMonitor pm)
      throws JavaModelException {
    IMethod selectedMethod = (IMethod) element;
    IType parentType = selectedMethod.getDeclaringType();
    if (parentType == null) {
      return;
    }
    ITypeHierarchy typeHierarchy = parentType.newTypeHierarchy(pm);
    IType[] subTypes = typeHierarchy.getAllSubtypes(parentType);

    MethodOverrideTester methodOverrideTester = new MethodOverrideTester(parentType, typeHierarchy);

    for (IType type : subTypes) {
      IMethod method = methodOverrideTester.findOverridingMethodInType(type, selectedMethod);
      if (method == null) {
        continue;
      }
      SymbolInformation openDeclaration = convertToSymbolInformation(method);
      implementations.add(openDeclaration);
    }
  }

  @SuppressWarnings("restriction")
  private static SymbolInformation convertToSymbolInformation(IJavaElement javaElement)
      throws JavaModelException {
    SymbolInformation symbolInformation = new SymbolInformation();
    symbolInformation.setKind(DocumentSymbolHandler.mapKind(javaElement));
    symbolInformation.setName(
        JavaElementLabels.getElementLabel(javaElement, JavaElementLabels.ALL_DEFAULT));
    Location location = JDTUtils.toLocation(javaElement);
    location.setUri(ResourceUtils.toClientUri(location.getUri()));
    symbolInformation.setLocation(location);
    return symbolInformation;
  }
}
