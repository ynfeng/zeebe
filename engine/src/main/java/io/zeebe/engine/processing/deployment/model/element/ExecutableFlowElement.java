/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.0. You may not use this file
 * except in compliance with the Zeebe Community License 1.0.
 */
package io.zeebe.engine.processing.deployment.model.element;

import io.zeebe.protocol.record.value.BpmnElementType;
import org.agrona.DirectBuffer;

public interface ExecutableFlowElement {

  DirectBuffer getId();

  BpmnElementType getElementType();

  ExecutableFlowElement getFlowScope();
}
