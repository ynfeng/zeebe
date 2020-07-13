/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.0. You may not use this file
 * except in compliance with the Zeebe Community License 1.0.
 */
package io.zeebe.engine.state;

import static org.assertj.core.api.Assertions.assertThat;

import io.zeebe.engine.processing.message.MessageObserver;
import io.zeebe.engine.state.instance.ElementInstanceState;
import io.zeebe.engine.state.instance.EventScopeInstanceState;
import io.zeebe.engine.state.instance.IncidentState;
import io.zeebe.engine.state.instance.JobState;
import io.zeebe.engine.state.instance.TimerInstanceState;
import io.zeebe.engine.state.instance.VariablesState;
import io.zeebe.engine.state.message.MessageStartEventSubscriptionState;
import io.zeebe.engine.state.message.MessageState;
import io.zeebe.engine.state.message.MessageSubscriptionState;
import io.zeebe.engine.state.message.WorkflowInstanceSubscriptionState;
import io.zeebe.engine.util.EngineRule;
import io.zeebe.model.bpmn.Bpmn;
import io.zeebe.protocol.record.intent.IncidentIntent;
import io.zeebe.protocol.record.intent.JobIntent;
import io.zeebe.protocol.record.intent.MessageStartEventSubscriptionIntent;
import io.zeebe.protocol.record.intent.WorkflowInstanceIntent;
import io.zeebe.protocol.record.value.BpmnElementType;
import io.zeebe.test.util.record.RecordingExporter;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public final class WorkflowExecutionCleanZeebeStateTest {

  private static final String PROCESS_ID = "workflow";

  @Rule public EngineRule engineRule = EngineRule.singlePartition();

  private ElementInstanceState elementInstanceState;
  private TimerInstanceState timerState;
  private EventScopeInstanceState eventScopeInstanceState;
  private JobState jobState;
  private IncidentState incidentState;
  private MessageState messageState;
  private MessageSubscriptionState messageSubscriptionState;
  private WorkflowInstanceSubscriptionState workflowInstanceSubscriptionState;
  private MessageStartEventSubscriptionState messageStartEventSubscriptionState;
  private VariablesState variablesState;

  @Before
  public void init() {
    final var zeebeState = engineRule.getZeebeState();

    final var workflowState = zeebeState.getWorkflowState();
    elementInstanceState = workflowState.getElementInstanceState();
    variablesState = elementInstanceState.getVariablesState();
    timerState = workflowState.getTimerState();
    eventScopeInstanceState = workflowState.getEventScopeInstanceState();

    jobState = zeebeState.getJobState();
    incidentState = zeebeState.getIncidentState();
    messageState = zeebeState.getMessageState();
    messageSubscriptionState = zeebeState.getMessageSubscriptionState();
    workflowInstanceSubscriptionState = zeebeState.getWorkflowInstanceSubscriptionState();
    messageStartEventSubscriptionState = zeebeState.getMessageStartEventSubscriptionState();

    assertThatStateIsEmpty();
  }

  @Test
  public void testWorkflowWithServiceTask() {
    engineRule
        .deployment()
        .withXmlResource(
            Bpmn.createExecutableProcess(PROCESS_ID)
                .startEvent()
                .serviceTask("task", t -> t.zeebeJobType("test"))
                .endEvent()
                .done())
        .deploy();

    final var workflowInstanceKey =
        engineRule.workflowInstance().ofBpmnProcessId(PROCESS_ID).withVariable("x", 1).create();

    engineRule
        .job()
        .ofInstance(workflowInstanceKey)
        .withType("test")
        .withVariable("y", 2)
        .complete();

    RecordingExporter.workflowInstanceRecords(WorkflowInstanceIntent.ELEMENT_COMPLETED)
        .withWorkflowInstanceKey(workflowInstanceKey)
        .withElementType(BpmnElementType.PROCESS)
        .await();

    assertThatStateIsEmpty();
  }

  @Test
  public void testWorkflowWithSubprocess() {
    engineRule
        .deployment()
        .withXmlResource(
            Bpmn.createExecutableProcess(PROCESS_ID)
                .startEvent()
                .subProcess(
                    "subprocess",
                    subProcess ->
                        subProcess
                            .zeebeInputExpression("x", "y")
                            .zeebeOutputExpression("y", "z")
                            .embeddedSubProcess()
                            .startEvent()
                            .endEvent())
                .endEvent()
                .done())
        .deploy();

    final var workflowInstanceKey =
        engineRule.workflowInstance().ofBpmnProcessId(PROCESS_ID).withVariable("x", 1).create();

    RecordingExporter.workflowInstanceRecords(WorkflowInstanceIntent.ELEMENT_COMPLETED)
        .withWorkflowInstanceKey(workflowInstanceKey)
        .withElementType(BpmnElementType.PROCESS)
        .await();

    assertThatStateIsEmpty();
  }

  @Test
  public void testWorkflowWithMultiInstance() {
    engineRule
        .deployment()
        .withXmlResource(
            Bpmn.createExecutableProcess(PROCESS_ID)
                .startEvent()
                .serviceTask(
                    "task",
                    t ->
                        t.zeebeJobType("test")
                            .multiInstance(
                                m ->
                                    m.zeebeInputCollectionExpression("items")
                                        .zeebeInputElement("item")
                                        .zeebeOutputCollection("results")
                                        .zeebeOutputElementExpression("result")))
                .endEvent()
                .done())
        .deploy();

    final var workflowInstanceKey =
        engineRule
            .workflowInstance()
            .ofBpmnProcessId(PROCESS_ID)
            .withVariable("items", List.of(1))
            .create();

    engineRule
        .job()
        .ofInstance(workflowInstanceKey)
        .withType("test")
        .withVariable("result", 2)
        .complete();

    RecordingExporter.workflowInstanceRecords(WorkflowInstanceIntent.ELEMENT_COMPLETED)
        .withWorkflowInstanceKey(workflowInstanceKey)
        .withElementType(BpmnElementType.PROCESS)
        .await();

    assertThatStateIsEmpty();
  }

  @Test
  public void testWorkflowWithTimerEvent() {
    engineRule
        .deployment()
        .withXmlResource(
            Bpmn.createExecutableProcess(PROCESS_ID)
                .startEvent()
                .intermediateCatchEvent("timer", e -> e.timerWithDuration("PT0S"))
                .endEvent()
                .done())
        .deploy();

    final var workflowInstanceKey =
        engineRule.workflowInstance().ofBpmnProcessId(PROCESS_ID).create();

    RecordingExporter.workflowInstanceRecords(WorkflowInstanceIntent.ELEMENT_COMPLETED)
        .withWorkflowInstanceKey(workflowInstanceKey)
        .withElementType(BpmnElementType.PROCESS)
        .await();

    assertThatStateIsEmpty();
  }

  @Test
  public void testWorkflowWithMessageEvent() {
    engineRule
        .deployment()
        .withXmlResource(
            Bpmn.createExecutableProcess(PROCESS_ID)
                .startEvent()
                .intermediateCatchEvent(
                    "message",
                    e ->
                        e.message(m -> m.name("message").zeebeCorrelationKeyExpression("key"))
                            .zeebeOutputExpression("x", "y"))
                .endEvent()
                .done())
        .deploy();

    final var workflowInstanceKey =
        engineRule
            .workflowInstance()
            .ofBpmnProcessId(PROCESS_ID)
            .withVariable("key", "key-1")
            .create();

    final var timeToLive = Duration.ofSeconds(10);
    engineRule
        .message()
        .withName("message")
        .withCorrelationKey("key-1")
        .withTimeToLive(timeToLive)
        .withVariables(Map.of("x", 1))
        .publish();

    RecordingExporter.workflowInstanceRecords(WorkflowInstanceIntent.ELEMENT_COMPLETED)
        .withWorkflowInstanceKey(workflowInstanceKey)
        .withElementType(BpmnElementType.PROCESS)
        .await();

    engineRule.increaseTime(timeToLive.plus(MessageObserver.MESSAGE_TIME_TO_LIVE_CHECK_INTERVAL));

    assertThatStateIsEmpty();
  }

  @Test
  public void testWorkflowWithMessageStartEvent() {
    final var deployment =
        engineRule
            .deployment()
            .withXmlResource(
                Bpmn.createExecutableProcess(PROCESS_ID)
                    .startEvent()
                    .message(m -> m.name("message").zeebeCorrelationKeyExpression("key"))
                    .zeebeOutputExpression("x", "y")
                    .endEvent()
                    .done())
            .deploy();

    final var workflowKey = deployment.getValue().getDeployedWorkflows().get(0).getWorkflowKey();

    final var timeToLive = Duration.ofSeconds(10);
    final var messagePublished =
        engineRule
            .message()
            .withName("message")
            .withCorrelationKey("key-1")
            .withTimeToLive(timeToLive)
            .withVariables(Map.of("x", 1))
            .publish();

    final var workflowInstanceKey =
        RecordingExporter.workflowInstanceRecords(WorkflowInstanceIntent.ELEMENT_ACTIVATING)
            .withWorkflowKey(workflowKey)
            .withElementType(BpmnElementType.PROCESS)
            .getFirst()
            .getKey();

    RecordingExporter.workflowInstanceRecords(WorkflowInstanceIntent.ELEMENT_COMPLETED)
        .withWorkflowInstanceKey(workflowInstanceKey)
        .withElementType(BpmnElementType.PROCESS)
        .await();

    engineRule.increaseTime(timeToLive.plus(MessageObserver.MESSAGE_TIME_TO_LIVE_CHECK_INTERVAL));

    // deploy new workflow without message start event to close the open subscription
    engineRule
        .deployment()
        .withXmlResource(Bpmn.createExecutableProcess(PROCESS_ID).startEvent().endEvent().done())
        .deploy();

    RecordingExporter.messageStartEventSubscriptionRecords(
            MessageStartEventSubscriptionIntent.CLOSED)
        .withWorkfloKey(workflowKey)
        .await();

    assertThatStateIsEmpty();
  }

  @Test
  public void testWorkflowWithErrorEvent() {
    engineRule
        .deployment()
        .withXmlResource(
            Bpmn.createExecutableProcess(PROCESS_ID)
                .startEvent()
                .serviceTask("task", t -> t.zeebeJobType("test"))
                .boundaryEvent("error", b -> b.error("ERROR"))
                .endEvent()
                .done())
        .deploy();

    final var workflowInstanceKey =
        engineRule.workflowInstance().ofBpmnProcessId(PROCESS_ID).withVariable("x", 1).create();

    engineRule
        .job()
        .ofInstance(workflowInstanceKey)
        .withType("test")
        .withErrorCode("ERROR")
        .throwError();

    RecordingExporter.workflowInstanceRecords(WorkflowInstanceIntent.ELEMENT_COMPLETED)
        .withWorkflowInstanceKey(workflowInstanceKey)
        .withElementType(BpmnElementType.PROCESS)
        .await();

    assertThatStateIsEmpty();
  }

  @Test
  public void testWorkflowWithIncident() {
    engineRule
        .deployment()
        .withXmlResource(
            Bpmn.createExecutableProcess(PROCESS_ID)
                .startEvent()
                .serviceTask("task", t -> t.zeebeJobType("test"))
                .endEvent()
                .done())
        .deploy();

    final var workflowInstanceKey =
        engineRule.workflowInstance().ofBpmnProcessId(PROCESS_ID).withVariable("x", 1).create();

    engineRule.job().ofInstance(workflowInstanceKey).withType("test").withRetries(0).fail();

    final var incidentCreated =
        RecordingExporter.incidentRecords(IncidentIntent.CREATED)
            .withWorkflowInstanceKey(workflowInstanceKey)
            .getFirst();

    engineRule.job().withKey(incidentCreated.getValue().getJobKey()).withRetries(1).updateRetries();

    engineRule
        .incident()
        .ofInstance(workflowInstanceKey)
        .withKey(incidentCreated.getKey())
        .resolve();

    engineRule
        .job()
        .ofInstance(workflowInstanceKey)
        .withType("test")
        .withVariable("y", 2)
        .complete();

    RecordingExporter.workflowInstanceRecords(WorkflowInstanceIntent.ELEMENT_COMPLETED)
        .withWorkflowInstanceKey(workflowInstanceKey)
        .withElementType(BpmnElementType.PROCESS)
        .await();

    assertThatStateIsEmpty();
  }

  @Test
  public void testWorkflowWithExclusiveGateway() {
    engineRule
        .deployment()
        .withXmlResource(
            Bpmn.createExecutableProcess(PROCESS_ID)
                .startEvent()
                .exclusiveGateway()
                .sequenceFlowId("s1")
                .conditionExpression("x > 10")
                .endEvent()
                .moveToLastGateway()
                .sequenceFlowId("s2")
                .conditionExpression("x <= 10")
                .endEvent()
                .done())
        .deploy();

    final var workflowInstanceKey =
        engineRule.workflowInstance().ofBpmnProcessId(PROCESS_ID).withVariable("x", 1).create();

    RecordingExporter.workflowInstanceRecords(WorkflowInstanceIntent.ELEMENT_COMPLETED)
        .withWorkflowInstanceKey(workflowInstanceKey)
        .withElementType(BpmnElementType.PROCESS)
        .await();

    assertThatStateIsEmpty();
  }

  @Test
  public void testWorkflowWithParallelGateway() {
    engineRule
        .deployment()
        .withXmlResource(
            Bpmn.createExecutableProcess(PROCESS_ID)
                .startEvent()
                .parallelGateway("fork")
                .endEvent()
                .moveToNode("fork")
                .endEvent()
                .done())
        .deploy();

    final var workflowInstanceKey =
        engineRule.workflowInstance().ofBpmnProcessId(PROCESS_ID).create();

    RecordingExporter.workflowInstanceRecords(WorkflowInstanceIntent.ELEMENT_COMPLETED)
        .withWorkflowInstanceKey(workflowInstanceKey)
        .withElementType(BpmnElementType.PROCESS)
        .await();

    assertThatStateIsEmpty();
  }

  @Test
  public void testWorkflowWithEventBasedGateway() {
    engineRule
        .deployment()
        .withXmlResource(
            Bpmn.createExecutableProcess(PROCESS_ID)
                .startEvent()
                .eventBasedGateway()
                .intermediateCatchEvent("timer", e -> e.timerWithDuration("PT0S"))
                .endEvent()
                .moveToLastGateway()
                .intermediateCatchEvent(
                    "message",
                    e -> e.message(m -> m.name("message").zeebeCorrelationKeyExpression("key")))
                .endEvent()
                .done())
        .deploy();

    final var workflowInstanceKey =
        engineRule
            .workflowInstance()
            .ofBpmnProcessId(PROCESS_ID)
            .withVariable("key", "key-1")
            .create();

    RecordingExporter.workflowInstanceRecords(WorkflowInstanceIntent.ELEMENT_COMPLETED)
        .withWorkflowInstanceKey(workflowInstanceKey)
        .withElementType(BpmnElementType.PROCESS)
        .await();

    assertThatStateIsEmpty();
  }

  @Test
  public void testWorkflowWithEventSubprocess() {
    engineRule
        .deployment()
        .withXmlResource(
            Bpmn.createExecutableProcess(PROCESS_ID)
                .eventSubProcess(
                    "event-subprocess",
                    subprocess ->
                        subprocess
                            .startEvent()
                            .interrupting(true)
                            .timerWithDuration("PT0.1S")
                            .endEvent())
                .startEvent()
                .serviceTask("task", t -> t.zeebeJobType("test"))
                .endEvent()
                .done())
        .deploy();

    final var workflowInstanceKey =
        engineRule.workflowInstance().ofBpmnProcessId(PROCESS_ID).create();

    RecordingExporter.workflowInstanceRecords(WorkflowInstanceIntent.ELEMENT_COMPLETED)
        .withWorkflowInstanceKey(workflowInstanceKey)
        .withElementType(BpmnElementType.PROCESS)
        .await();

    assertThatStateIsEmpty();
  }

  @Test
  public void testWorkflowWithCallActivity() {
    final var childWorkflow = Bpmn.createExecutableProcess("child").startEvent().endEvent().done();
    final var parentWorkflow =
        Bpmn.createExecutableProcess(PROCESS_ID)
            .startEvent()
            .callActivity("call", c -> c.zeebeProcessId("child"))
            .endEvent()
            .done();

    engineRule
        .deployment()
        .withXmlResource("child.bpmn", childWorkflow)
        .withXmlResource("parent.bpmn", parentWorkflow)
        .deploy();

    final var workflowInstanceKey =
        engineRule.workflowInstance().ofBpmnProcessId(PROCESS_ID).create();

    RecordingExporter.workflowInstanceRecords(WorkflowInstanceIntent.ELEMENT_COMPLETED)
        .withWorkflowInstanceKey(workflowInstanceKey)
        .withElementType(BpmnElementType.PROCESS)
        .await();

    assertThatStateIsEmpty();
  }

  @Test
  public void testWorkflowCreatedWithResult() {
    engineRule
        .deployment()
        .withXmlResource(Bpmn.createExecutableProcess(PROCESS_ID).startEvent().endEvent().done())
        .deploy();

    final var workflowInstanceKey =
        engineRule
            .workflowInstance()
            .ofBpmnProcessId(PROCESS_ID)
            .withVariable("x", 1)
            .withResult()
            .create();

    RecordingExporter.workflowInstanceRecords(WorkflowInstanceIntent.ELEMENT_COMPLETED)
        .withWorkflowInstanceKey(workflowInstanceKey)
        .withElementType(BpmnElementType.PROCESS)
        .await();

    assertThatStateIsEmpty();
  }

  @Test
  public void testWorkflowCanceled() {
    engineRule
        .deployment()
        .withXmlResource(
            Bpmn.createExecutableProcess(PROCESS_ID)
                .startEvent()
                .serviceTask("task", t -> t.zeebeJobType("test"))
                .endEvent()
                .done())
        .deploy();

    final var workflowInstanceKey =
        engineRule.workflowInstance().ofBpmnProcessId(PROCESS_ID).withVariable("x", 1).create();

    RecordingExporter.jobRecords(JobIntent.CREATED)
        .withWorkflowInstanceKey(workflowInstanceKey)
        .await();

    engineRule.workflowInstance().withInstanceKey(workflowInstanceKey).cancel();

    RecordingExporter.workflowInstanceRecords(WorkflowInstanceIntent.ELEMENT_TERMINATED)
        .withWorkflowInstanceKey(workflowInstanceKey)
        .withElementType(BpmnElementType.PROCESS)
        .await();

    assertThatStateIsEmpty();
  }

  private void assertThatStateIsEmpty() {
    // sometimes the state takes few moments until is is empty
    Awaitility.await()
        .untilAsserted(
            () -> {
              assertThat(variablesState.isEmpty())
                  .describedAs("Expected the variable state to be empty")
                  .isTrue();

              assertThat(elementInstanceState.isEmpty())
                  .describedAs("Expected the element instance state to be empty")
                  .isTrue();

              assertThat(eventScopeInstanceState.isEmpty())
                  .describedAs("Expected the event scope instance state to be empty")
                  .isTrue();

              assertThat(jobState.isEmpty())
                  .describedAs("Expected the job state to be empty")
                  .isTrue();

              assertThat(timerState.isEmpty())
                  .describedAs("Expected the timer state to be empty")
                  .isTrue();

              assertThat(incidentState.isEmpty())
                  .describedAs("Expected the incident state to be empty")
                  .isTrue();

              assertThat(messageState.isEmpty())
                  .describedAs("Expected the message state to be empty")
                  .isTrue();

              assertThat(messageSubscriptionState.isEmpty())
                  .describedAs("Expected the message subscription state to be empty")
                  .isTrue();

              assertThat(workflowInstanceSubscriptionState.isEmpty())
                  .describedAs("Expected the workflow instance subscription state to be empty")
                  .isTrue();

              assertThat(messageStartEventSubscriptionState.isEmpty())
                  .describedAs("Expected the message start event subscription state to be empty")
                  .isTrue();
            });
  }
}
