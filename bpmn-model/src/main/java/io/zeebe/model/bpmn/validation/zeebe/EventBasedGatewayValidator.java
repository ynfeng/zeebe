/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zeebe.model.bpmn.validation.zeebe;

import io.zeebe.model.bpmn.instance.EventBasedGateway;
import io.zeebe.model.bpmn.instance.EventDefinition;
import io.zeebe.model.bpmn.instance.FlowNode;
import io.zeebe.model.bpmn.instance.IntermediateCatchEvent;
import io.zeebe.model.bpmn.instance.MessageEventDefinition;
import io.zeebe.model.bpmn.instance.SequenceFlow;
import io.zeebe.model.bpmn.instance.TimerEventDefinition;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.camunda.bpm.model.xml.validation.ModelElementValidator;
import org.camunda.bpm.model.xml.validation.ValidationResultCollector;

public class EventBasedGatewayValidator implements ModelElementValidator<EventBasedGateway> {

  private static final List<Class<? extends EventDefinition>> SUPPORTED_EVENTS =
      Arrays.asList(TimerEventDefinition.class, MessageEventDefinition.class);

  private static final String ERROR_UNSUPPORTED_TARGET_NODE =
      "Event-based gateway must not have an outgoing sequence flow to other elements than message/timer intermediate catch events.";

  @Override
  public Class<EventBasedGateway> getElementType() {
    return EventBasedGateway.class;
  }

  @Override
  public void validate(
      EventBasedGateway element, ValidationResultCollector validationResultCollector) {

    final Collection<SequenceFlow> outgoingSequenceFlows = element.getOutgoing();

    if (outgoingSequenceFlows.size() < 2) {
      validationResultCollector.addError(
          0, "Event-based gateway must have at least 2 outgoing sequence flows.");
    }

    final boolean isValid =
        outgoingSequenceFlows.stream().allMatch(this::isValidOutgoingSequenceFlow);
    if (!isValid) {
      validationResultCollector.addError(0, ERROR_UNSUPPORTED_TARGET_NODE);
    }
  }

  private boolean isValidOutgoingSequenceFlow(SequenceFlow flow) {
    final FlowNode targetNode = flow.getTarget();

    if (targetNode instanceof IntermediateCatchEvent) {
      return isValidEvent((IntermediateCatchEvent) targetNode);
    } else {
      return false;
    }
  }

  private boolean isValidEvent(final IntermediateCatchEvent event) {
    final Collection<EventDefinition> eventDefinitions = event.getEventDefinitions();

    if (eventDefinitions.size() != 1) {
      return false;

    } else {
      final EventDefinition eventDefinition = eventDefinitions.iterator().next();
      return SUPPORTED_EVENTS
          .stream()
          .anyMatch(e -> e.isAssignableFrom(eventDefinition.getClass()));
    }
  }
}