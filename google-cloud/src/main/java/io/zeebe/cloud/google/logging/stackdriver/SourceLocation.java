/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.0. You may not use this file
 * except in compliance with the Zeebe Community License 1.0.
 */
package io.zeebe.cloud.google.logging.stackdriver;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_EMPTY)
final class SourceLocation {
  @JsonProperty("function")
  private String methodName;

  private String file;
  private int line;

  SourceLocation(final StackTraceElement traceElement) {
    file = traceElement.getFileName();
    line = traceElement.getLineNumber();
    methodName = traceElement.getMethodName();
  }

  public String getFile() {
    return file;
  }

  public void setFile(final String file) {
    this.file = file;
  }

  public String getMethodName() {
    return methodName;
  }

  public void setMethodName(final String methodName) {
    this.methodName = methodName;
  }

  public int getLine() {
    return line;
  }

  public void setLine(final int line) {
    this.line = line;
  }
}
