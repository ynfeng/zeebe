/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.0. You may not use this file
 * except in compliance with the Zeebe Community License 1.0.
 */
package io.zeebe.cloud.google.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.zeebe.cloud.google.logging.stackdriver.StackdriverLogEntry;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.util.StringBuilderWriter;

/**
 * Stackdriver JSON layout as described here:
 * https://cloud.google.com/logging/docs/reference/v2/rest/v2/LogEntry
 *
 * <p>The layout produces log output which fully integrates with Google's ErrorReporting, as well as
 * properly unwrapping the context map to allow adding ad-hoc fields such as the trace and spanId to
 * integrated with Cloud Trace.
 *
 * <p>Open points:
 *
 * <ul>
 *   <li>How to pass nested context values such as the HttpRequest field?
 * </ul>
 */
@Plugin(name = "StackdriverLayout", category = Node.CATEGORY, elementType = Layout.ELEMENT_TYPE)
public final class StackdriverLayout extends AbstractStringLayout {

  private static final ObjectWriter WRITER =
      new ObjectMapper().writerFor(StackdriverLogEntry.class);
  private static final String DEFAULT_SERVICE_VERSION = "development";
  private static final String DEFAULT_SERVICE_NAME = "zeebe";

  private final String serviceName;
  private final String serviceVersion;

  public StackdriverLayout() {
    this(DEFAULT_SERVICE_NAME, DEFAULT_SERVICE_VERSION);
  }

  public StackdriverLayout(final String serviceName, final String serviceVersion) {
    super(StandardCharsets.UTF_8);
    if (serviceName == null || serviceName.isBlank()) {
      this.serviceName = DEFAULT_SERVICE_NAME;
    } else {
      this.serviceName = serviceName;
    }

    if (serviceVersion == null || serviceVersion.isBlank()) {
      this.serviceVersion = DEFAULT_SERVICE_VERSION;
    } else {
      this.serviceVersion = serviceVersion;
    }
  }

  @PluginFactory
  public static StackdriverLayout createLayout(
      @PluginAttribute("serviceName") final String serviceName,
      @PluginAttribute("serviceVersion") final String serviceVersion) {
    return new StackdriverLayout(serviceName, serviceVersion);
  }

  @Override
  public String toSerializable(final LogEvent event) {
    final var entry = buildLogEntry(event);

    try (StringBuilderWriter writer = new StringBuilderWriter()) {
      WRITER.writeValue(writer, entry);
      writer.append('\n');
      return writer.toString();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private StackdriverLogEntry buildLogEntry(final LogEvent event) {
    final var builder = StackdriverLogEntry.builder();

    return builder
        .withLevel(event.getLevel())
        .withMessage(event.getMessage().getFormattedMessage())
        .withError(event.getThrownProxy())
        .withTime(getInstant(event.getInstant()))
        .withSource(event.getSource())
        .withContext(event.getContextData().toMap())
        .withContextIfAbsent("threadName", event.getThreadName())
        .withContextIfAbsent("loggerName", event.getLoggerName())
        .withContextIfAbsent("threadId", event.getThreadId())
        .withContextIfAbsent("threadPriority", event.getThreadPriority())
        .withServiceName(serviceName)
        .withServiceVersion(serviceVersion)
        .build();
  }

  private Instant getInstant(final org.apache.logging.log4j.core.time.Instant instant) {
    return Instant.ofEpochSecond(instant.getEpochSecond(), instant.getNanoOfSecond());
  }
}
