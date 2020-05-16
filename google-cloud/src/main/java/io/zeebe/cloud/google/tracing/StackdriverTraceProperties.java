/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.0. You may not use this file
 * except in compliance with the Zeebe Community License 1.0.
 */
package io.zeebe.cloud.google.tracing;

import io.opencensus.exporter.trace.stackdriver.StackdriverTraceConfiguration;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

final class StackdriverTraceProperties {
  private String projectId;
  private Duration deadline;

  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(final String projectId) {
    this.projectId = projectId;
  }

  public Duration getDeadline() {
    return deadline;
  }

  public void setDeadline(final Duration deadline) {
    this.deadline = deadline;
  }

  public StackdriverTraceConfiguration stackdriverTraceConfiguration() {
    final var builder = StackdriverTraceConfiguration.builder();
    Optional.ofNullable(projectId).ifPresent(builder::setProjectId);
    Optional.ofNullable(deadline)
        .map(Duration::toMillis)
        .map(io.opencensus.common.Duration::fromMillis)
        .ifPresent(builder::setDeadline);

    return builder.build();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getProjectId(), getDeadline());
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final StackdriverTraceProperties that = (StackdriverTraceProperties) o;
    return Objects.equals(getProjectId(), that.getProjectId())
        && Objects.equals(getDeadline(), that.getDeadline());
  }

  @Override
  public String toString() {
    return "StackdriverTraceProperties{"
        + "projectId='"
        + projectId
        + '\''
        + ", deadline="
        + deadline
        + '}';
  }
}
