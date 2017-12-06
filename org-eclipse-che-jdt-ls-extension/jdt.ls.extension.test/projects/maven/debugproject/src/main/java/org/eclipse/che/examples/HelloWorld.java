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
