/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.0. You may not use this file
 * except in compliance with the Zeebe Community License 1.0.
 */
package io.zeebe.cloud.google.tracing;

import io.opencensus.trace.Sampler;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.samplers.Samplers;
import java.util.Objects;
import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("zeebe.tracing")
public final class TracingModuleProperties {

  private boolean enabled = false;
  private StackdriverTraceProperties stackdriverTraceProperties = new StackdriverTraceProperties();
  private Double probability;
  private Integer maxNumberOfAnnotations;
  private Integer maxNumberOfMessageEvents;
  private Integer maxNumberOfAttributes;
  private Integer maxNumberOfLinks;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  public Double getProbability() {
    return probability;
  }

  public void setProbability(final double probability) {
    this.probability = probability;
  }

  public Integer getMaxNumberOfAnnotations() {
    return maxNumberOfAnnotations;
  }

  public void setMaxNumberOfAnnotations(final int maxNumberOfAnnotations) {
    this.maxNumberOfAnnotations = maxNumberOfAnnotations;
  }

  public Integer getMaxNumberOfMessageEvents() {
    return maxNumberOfMessageEvents;
  }

  public void setMaxNumberOfMessageEvents(final int maxNumberOfMessageEvents) {
    this.maxNumberOfMessageEvents = maxNumberOfMessageEvents;
  }

  public Integer getMaxNumberOfAttributes() {
    return maxNumberOfAttributes;
  }

  public void setMaxNumberOfAttributes(final int maxNumberOfAttributes) {
    this.maxNumberOfAttributes = maxNumberOfAttributes;
  }

  public Integer getMaxNumberOfLinks() {
    return maxNumberOfLinks;
  }

  public void setMaxNumberOfLinks(final int maxNumberOfLinks) {
    this.maxNumberOfLinks = maxNumberOfLinks;
  }

  public StackdriverTraceProperties getStackdriverTraceProperties() {
    return stackdriverTraceProperties;
  }

  public void setStackdriverTraceProperties(
      final StackdriverTraceProperties stackdriverTraceProperties) {
    this.stackdriverTraceProperties = stackdriverTraceProperties;
  }

  public TraceParams createTraceParams() {
    final var builder = Tracing.getTraceConfig().getActiveTraceParams().toBuilder();
    Optional.ofNullable(maxNumberOfAnnotations).ifPresent(builder::setMaxNumberOfAnnotations);
    Optional.ofNullable(maxNumberOfAttributes).ifPresent(builder::setMaxNumberOfAttributes);
    Optional.ofNullable(maxNumberOfLinks).ifPresent(builder::setMaxNumberOfLinks);
    Optional.ofNullable(maxNumberOfMessageEvents).ifPresent(builder::setMaxNumberOfMessageEvents);
    Optional.ofNullable(probability).map(this::createSampler).ifPresent(builder::setSampler);

    return builder.build();
  }

  public Sampler createSampler(final double samplingProbability) {
    if (samplingProbability >= 1) {
      return Samplers.alwaysSample();
    }

    if (samplingProbability <= 0) {
      return Samplers.neverSample();
    }

    return Samplers.probabilitySampler(samplingProbability);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        isEnabled(),
        getStackdriverTraceProperties(),
        getProbability(),
        getMaxNumberOfAnnotations(),
        getMaxNumberOfMessageEvents(),
        getMaxNumberOfAttributes(),
        getMaxNumberOfLinks());
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final TracingModuleProperties that = (TracingModuleProperties) o;
    return isEnabled() == that.isEnabled()
        && Objects.equals(getStackdriverTraceProperties(), that.getStackdriverTraceProperties())
        && Objects.equals(getProbability(), that.getProbability())
        && Objects.equals(getMaxNumberOfAnnotations(), that.getMaxNumberOfAnnotations())
        && Objects.equals(getMaxNumberOfMessageEvents(), that.getMaxNumberOfMessageEvents())
        && Objects.equals(getMaxNumberOfAttributes(), that.getMaxNumberOfAttributes())
        && Objects.equals(getMaxNumberOfLinks(), that.getMaxNumberOfLinks());
  }

  @Override
  public String toString() {
    return "TracingModuleProperties{"
        + "enabled="
        + enabled
        + ", stackdriverTraceProperties="
        + stackdriverTraceProperties
        + ", probability="
        + probability
        + ", maxNumberOfAnnotations="
        + maxNumberOfAnnotations
        + ", maxNumberOfMessageEvents="
        + maxNumberOfMessageEvents
        + ", maxNumberOfAttributes="
        + maxNumberOfAttributes
        + ", maxNumberOfLinks="
        + maxNumberOfLinks
        + '}';
  }
}
