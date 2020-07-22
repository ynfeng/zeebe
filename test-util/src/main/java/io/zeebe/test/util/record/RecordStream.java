/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.0. You may not use this file
 * except in compliance with the Zeebe Community License 1.0.
 */
package io.zeebe.test.util.record;

import io.zeebe.protocol.record.Record;
import io.zeebe.protocol.record.RecordValue;
import io.zeebe.protocol.record.ValueType;
import io.zeebe.protocol.record.intent.WorkflowInstanceIntent;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class RecordStream extends ExporterRecordStream<RecordValue, RecordStream> {
  public RecordStream(final Stream<Record<RecordValue>> wrappedStream) {
    super(wrappedStream);
  }

  @Override
  protected RecordStream supply(final Stream<Record<RecordValue>> wrappedStream) {
    return new RecordStream(wrappedStream);
  }

  public RecordStream between(final long lowerBoundPosition, final long upperBoundPosition) {
    return between(
        r -> r.getPosition() > lowerBoundPosition, r -> r.getPosition() >= upperBoundPosition);
  }

  public RecordStream between(final Record<?> lowerBound, final Record<?> upperBound) {
    return between(lowerBound::equals, upperBound::equals);
  }

  public RecordStream between(
      final Predicate<Record<?>> lowerBound, final Predicate<Record<?>> upperBound) {
    return limit(upperBound::test).skipUntil(lowerBound::test);
  }

  public RecordStream limitToWorkflowInstance(final long workflowInstanceKey) {
    return between(
        r ->
            r.getKey() == workflowInstanceKey
                && r.getIntent() == WorkflowInstanceIntent.ELEMENT_ACTIVATING,
        r ->
            r.getKey() == workflowInstanceKey
                && Set.of(
                        WorkflowInstanceIntent.ELEMENT_COMPLETED,
                        WorkflowInstanceIntent.ELEMENT_TERMINATED)
                    .contains(r.getIntent()));
  }

  public WorkflowInstanceRecordStream workflowInstanceRecords() {
    return new WorkflowInstanceRecordStream(
        filter(r -> r.getValueType() == ValueType.WORKFLOW_INSTANCE).map(Record.class::cast));
  }

  public TimerRecordStream timerRecords() {
    return new TimerRecordStream(
        filter(r -> r.getValueType() == ValueType.TIMER).map(Record.class::cast));
  }

  public VariableDocumentRecordStream variableDocumentRecords() {
    return new VariableDocumentRecordStream(
        filter(r -> r.getValueType() == ValueType.VARIABLE_DOCUMENT).map(Record.class::cast));
  }

  public VariableRecordStream variableRecords() {
    return new VariableRecordStream(
        filter(r -> r.getValueType() == ValueType.VARIABLE).map(Record.class::cast));
  }

  public JobRecordStream jobRecords() {
    return new JobRecordStream(
        filter(r -> r.getValueType() == ValueType.JOB).map(Record.class::cast));
  }

  public IncidentRecordStream incidentRecords() {
    return new IncidentRecordStream(
        filter(r -> r.getValueType() == ValueType.INCIDENT).map(Record.class::cast));
  }

  public MessageSubscriptionRecordStream messageSubscriptionRecords() {
    return new MessageSubscriptionRecordStream(
        filter(r -> r.getValueType() == ValueType.MESSAGE_SUBSCRIPTION).map(Record.class::cast));
  }
}
