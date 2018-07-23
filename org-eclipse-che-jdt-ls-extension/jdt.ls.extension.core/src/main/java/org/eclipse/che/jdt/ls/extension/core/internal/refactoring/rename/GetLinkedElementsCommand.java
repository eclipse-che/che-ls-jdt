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
package org.eclipse.che.jdt.ls.extension.core.internal.refactoring.rename;

import static java.util.Collections.emptyList;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.core.internal.GsonUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.internal.corext.dom.IASTSharedValues;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.corext.dom.LinkedNodeFinder;
import org.eclipse.jdt.ls.core.internal.handlers.JsonRpcHelpers;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;

/**
 * Command to find linked elements for rename refactoring.
 *
 * @author Valeriy Svydenko
 */
public class GetLinkedElementsCommand {
  private static final Gson GSON = GsonUtils.getInstance();

  /**
   * Analyzes cursor position and finds ranges of linked elements in opened CU.
   *
   * @param arguments contains instance of {@link TextDocumentPositionParams}
   * @param pm progress monitor
   * @return list of ranges which have the same link
   */
  public static List<Range> execute(List<Object> arguments, IProgressMonitor pm) {
    validateArguments(arguments);

    TextDocumentPositionParams params =
        GSON.fromJson(GSON.toJson(arguments.get(0)), TextDocumentPositionParams.class);

    TextDocumentIdentifier textDocument = params.getTextDocument();
    Position position = params.getPosition();
    if (textDocument == null || position == null) {
      return emptyList();
    }

    ICompilationUnit cu = JDTUtils.resolveCompilationUnit(textDocument.getUri());
    if (cu == null) {
      return emptyList();
    }

    CompilationUnit root =
        new RefactoringASTParser(IASTSharedValues.SHARED_AST_LEVEL).parse(cu, true);

    List<Range> result = new LinkedList<>();
    try {
      int offset =
          JsonRpcHelpers.toOffset(cu.getBuffer(), position.getLine(), position.getCharacter());
      ASTNode selectedNode = NodeFinder.perform(root, offset, 0);
      if (!(selectedNode instanceof SimpleName)) {
        return emptyList();
      }

      ASTNode[] sameNodes = getAstNodes(root, (SimpleName) selectedNode);

      IDocument document = JsonRpcHelpers.toDocument(cu.getBuffer());
      for (ASTNode elem : sameNodes) {
        int length = elem.getLength();
        int startPosition = elem.getStartPosition();
        Range range =
            new Range(
                createPosition(document, startPosition),
                createPosition(document, startPosition + length));
        result.add(range);
      }
    } catch (JavaModelException e) {
      JavaLanguageServerPlugin.logException(e.getMessage(), e);
    }

    return result;
  }

  private static ASTNode[] getAstNodes(CompilationUnit root, SimpleName nameNode) {
    final int pos = nameNode.getStartPosition();
    ASTNode[] sameNodes = LinkedNodeFinder.findByNode(root, nameNode);

    Arrays.sort(
        sameNodes,
        new Comparator<ASTNode>() {
          public int compare(ASTNode o1, ASTNode o2) {
            return rank(o1) - rank(o2);
          }

          private int rank(ASTNode node) {
            int relativeRank = node.getStartPosition() + node.getLength() - pos;
            if (relativeRank < 0) {
              return Integer.MAX_VALUE + relativeRank;
            } else {
              return relativeRank;
            }
          }
        });
    return sameNodes;
  }

  private static Position createPosition(IDocument document, int offset) {
    Position start = new Position();
    try {
      int lineOfOffset = document.getLineOfOffset(offset);
      start.setLine(lineOfOffset);
      start.setCharacter(offset - document.getLineOffset(lineOfOffset));
    } catch (BadLocationException e) {
      JavaLanguageServerPlugin.logException(e.getMessage(), e);
    }
    return start;
  }

  private static void validateArguments(List<Object> arguments) {
    Preconditions.checkArgument(
        !arguments.isEmpty(), TextDocumentPositionParams.class.getName() + " is expected.");
  }
}
