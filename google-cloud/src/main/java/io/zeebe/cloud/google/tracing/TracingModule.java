/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.0. You may not use this file
 * except in compliance with the Zeebe Community License 1.0.
 */
package io.zeebe.cloud.google.tracing;

import io.opencensus.exporter.trace.stackdriver.StackdriverTraceExporter;
import io.opencensus.trace.Tracing;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(TracingModuleProperties.class)
public final class TracingModule implements InitializingBean {
  private static final Logger LOGGER = LoggerFactory.getLogger(TracingModule.class);

  @Autowired private TracingModuleProperties tracing;

  @Override
  public void afterPropertiesSet() throws Exception {
    if (tracing.isEnabled()) {
      configTracing();
      registerStackdriverTraceExporter();
    }
  }

  private void registerStackdriverTraceExporter() {
    final var config = tracing.getStackdriverTraceProperties();

    try {
      StackdriverTraceExporter.createAndRegister(config.stackdriverTraceConfiguration());
    } catch (final IOException e) {
      LOGGER.error("Failed to register stack driver exporter", e);
    }
  }

  private void configTracing() {
    final var config = Tracing.getTraceConfig();
    config.updateActiveTraceParams(tracing.createTraceParams());
  }
}
