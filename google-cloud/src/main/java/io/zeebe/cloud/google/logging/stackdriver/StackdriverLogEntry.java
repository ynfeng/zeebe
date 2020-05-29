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
import java.util.List;
import java.util.Map;

/**
 * POJO allowing the easy construction and serialization of a Stackdriver compatible LogEntry
 *
 * <p>See here for documentation:
 * https://cloud.google.com/logging/docs/agent/configuration#special-fields
 * https://cloud.google.com/logging/docs/reference/v2/rest/v2/LogEntry
 */
@JsonInclude(Include.NON_EMPTY)
public final class StackdriverLogEntry {
  @JsonProperty("severity")
  private String level;

  @JsonProperty("logging.googleapis.com/sourceLocation")
  private SourceLocation sourceLocation;

  @JsonProperty(value = "message", required = true)
  private String message;

  @JsonProperty("@type")
  private String type;

  @JsonProperty("serviceContext")
  private ServiceContext service;

  @JsonProperty("context")
  private Map<String, Object> context;

  @JsonProperty("time")
  private String time;

  @JsonProperty("logging.googleapis.com/trace")
  private String trace;

  @JsonProperty("logging.googleapis.com/spanId")
  private String spanId;

  @JsonProperty("logging.googleapis.com/trace_sampled")
  private String traceSampled;

  @JsonProperty("logging.googleapis.com/labels")
  private List<Label> labels;

  StackdriverLogEntry() {}

  public static StackdriverLogEntryBuilder builder() {
    return new StackdriverLogEntryBuilder();
  }

  public String getLevel() {
    return level;
  }

  public void setLevel(final String level) {
    this.level = level;
  }

  public SourceLocation getSourceLocation() {
    return sourceLocation;
  }

  public void setSourceLocation(final SourceLocation sourceLocation) {
    this.sourceLocation = sourceLocation;
  }

  public String getTime() {
    return time;
  }

  public void setTime(final String time) {
    this.time = time;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  public String getType() {
    return type;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public ServiceContext getService() {
    return service;
  }

  public void setService(final ServiceContext service) {
    this.service = service;
  }

  public Map<String, Object> getContext() {
    return context;
  }

  public void setContext(final Map<String, Object> context) {
    this.context = context;
  }

  public String getTrace() {
    return trace;
  }

  public void setTrace(final String trace) {
    this.trace = trace;
  }

  public String getSpanId() {
    return spanId;
  }

  public void setSpanId(final String spanId) {
    this.spanId = spanId;
  }

  public String getTraceSampled() {
    return traceSampled;
  }

  public void setTraceSampled(final String traceSampled) {
    this.traceSampled = traceSampled;
  }

  public List<Label> getLabels() {
    return labels;
  }

  public void setLabels(final List<Label> labels) {
    this.labels = labels;
  }

  public void reset() {

  }
}
