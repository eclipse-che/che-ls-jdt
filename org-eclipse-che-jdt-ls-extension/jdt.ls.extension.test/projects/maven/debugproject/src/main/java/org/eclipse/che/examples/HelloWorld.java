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
package org.eclipse.che.examples;

public class HelloWorld {
  public static void main(String[] argvs) {
    String a = "Che";

    new InnerClass().sayHello();

    new Thread() {
      @Override
      public void run() {
        System.out.println("Hello");
      }
    }.run();

    Stream.of("a", "b")
        .forEach(
            v -> {
              System.out.println(v);
            });
  }

  private static class InnerClass {
    public void sayHello() {
      System.out.println("Hello");
    }
  }

  public static void test() {
    class LocalClass1 {
      public void sayHello() {
        System.out.println("Hello");
      }
    }
    
    class LocalClass2 {
      public void sayHello() {
        System.out.println("Hello");
      }
    }
  }
}
