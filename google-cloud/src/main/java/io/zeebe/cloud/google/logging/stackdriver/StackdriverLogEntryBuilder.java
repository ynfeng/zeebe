/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.0. You may not use this file
 * except in compliance with the Zeebe Community License 1.0.
 */
package io.zeebe.cloud.google.logging.stackdriver;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.impl.ThrowableProxy;

public final class StackdriverLogEntryBuilder {
  private static final String TYPE_REPORTING_EVENT =
      "type.googleapis.com/google.devtools.clouderrorreporting.v1beta1.ReportedErrorEvent";

  private final ServiceContext service;
  private final Map<String, Object> context;

  private HttpRequest request;
  private SourceLocation sourceLocation;
  private Level level;
  private String time;
  private String message;
  private String type;
  private ThrowableProxy errorProxy;
  private StackTraceElement traceElement;

  StackdriverLogEntryBuilder() {
    this.service = new ServiceContext();
    this.context = new HashMap<>();
  }

  public StackdriverLogEntryBuilder withLevel(final Level level) {
    this.level = level;
    return this;
  }

  public StackdriverLogEntryBuilder withSource(final StackTraceElement traceElement) {
    this.traceElement = traceElement;
    return this;
  }

  public StackdriverLogEntryBuilder withTime(final Instant time) {
    // returns a ISO-8061; RFC3339 is a variant of it, and thus compatible
    this.time = DateTimeFormatter.ISO_INSTANT.format(time);
    return this;
  }

  public StackdriverLogEntryBuilder withMessage(final String message) {
    this.message = message;
    return this;
  }

  public StackdriverLogEntryBuilder withType(final String type) {
    this.type = type;
    return this;
  }

  public StackdriverLogEntryBuilder withServiceName(final String serviceName) {
    this.service.setService(serviceName);
    return this;
  }

  public StackdriverLogEntryBuilder withServiceVersion(final String serviceVersion) {
    this.service.setVersion(serviceVersion);
    return this;
  }

  public StackdriverLogEntryBuilder withContextIfAbsent(final String key, final Object value) {
    this.context.putIfAbsent(key, value);
    return this;
  }

  public <T> StackdriverLogEntryBuilder withContext(final Map<String, T> context) {
    this.context.putAll(context);
    return this;
  }

  public StackdriverLogEntryBuilder withError(final ThrowableProxy error) {
    this.errorProxy = error;
    return this;
  }

  public StackdriverLogEntryBuilder withHttpRequest(final HttpRequest request) {
    this.request = request;
    return this;
  }

  public StackdriverLogEntry build() {
    final StackdriverLogEntry stackdriverLogEntry = new StackdriverLogEntry();

    if (errorProxy != null) {
      final var errorTraceElements = errorProxy.getStackTrace();
      message = errorProxy.getExtendedStackTraceAsString();
      type = TYPE_REPORTING_EVENT;
      level = Level.ERROR;

      if (errorTraceElements != null && errorTraceElements.length > 0) {
        context.putIfAbsent("reportLocation", new SourceLocation(errorTraceElements[0]));
      }
    }

    if (traceElement != null) {
      sourceLocation = new SourceLocation(traceElement);
    }

    stackdriverLogEntry.setRequest(request);
    stackdriverLogEntry.setLevel(level.name());
    stackdriverLogEntry.setSourceLocation(sourceLocation);
    stackdriverLogEntry.setTime(time);
    stackdriverLogEntry.setMessage(Objects.requireNonNull(message));
    stackdriverLogEntry.setRequest(request);
    stackdriverLogEntry.setType(type);
    stackdriverLogEntry.setService(service);
    stackdriverLogEntry.setContext(context);

    return stackdriverLogEntry;
  }
}
