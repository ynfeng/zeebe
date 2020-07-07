/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.0. You may not use this file
 * except in compliance with the Zeebe Community License 1.0.
 */
package io.zeebe.util;

import java.util.Random;
import org.junit.Assert;
import org.junit.Test;

public class FlakyTest {

  Random random = new Random();

  @Test
  public void shouldFailNineInTenInvocations() {
    System.out.println(System.currentTimeMillis());
    if (random.nextDouble() < 0.9) {
      Assert.fail("failed");
    }
  }

  @Test
  public void shouldFailOnceInTwoInvocations() {
    System.out.println(System.currentTimeMillis());
    if (random.nextDouble() < 0.5) {
      Assert.fail("failed");
    }
  }

  @Test
  public void shouldFailOnceInTenInvocations() {
    System.out.println(System.currentTimeMillis());
    if (random.nextDouble() < 0.1) {
      Assert.fail("failed");
    }
  }

  @Test
  public void shouldFailOnceInHundredInvocations() {
    System.out.println(System.currentTimeMillis());
    if (random.nextDouble() < 0.01) {
      Assert.fail("failed");
    }
  }
}
