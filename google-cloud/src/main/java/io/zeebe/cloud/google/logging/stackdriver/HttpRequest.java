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

/**
 * See https://cloud.google.com/logging/docs/reference/v2/rest/v2/LogEntry#HttpRequest for
 * documentation
 */
@JsonInclude(Include.NON_EMPTY)
final class HttpRequest {
  private String requestMethod;
  private String requestUrl;
  private long requestSize;
  private int status;
  private long responseSize;
  private String userAgent;
  private String remoteIp;
  private String serverIp;
  private String referer;
  private String latency;
  private boolean cacheLookup;
  private boolean cacheHit;
  private boolean cacheValidatedWithOriginServer;
  private long cacheFillBytes;
  private String protocol;

  public String getRequestMethod() {
    return requestMethod;
  }

  public void setRequestMethod(final String requestMethod) {
    this.requestMethod = requestMethod;
  }

  public String getRequestUrl() {
    return requestUrl;
  }

  public void setRequestUrl(final String requestUrl) {
    this.requestUrl = requestUrl;
  }

  public long getRequestSize() {
    return requestSize;
  }

  public void setRequestSize(final long requestSize) {
    this.requestSize = requestSize;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(final int status) {
    this.status = status;
  }

  public long getResponseSize() {
    return responseSize;
  }

  public void setResponseSize(final long responseSize) {
    this.responseSize = responseSize;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(final String userAgent) {
    this.userAgent = userAgent;
  }

  public String getRemoteIp() {
    return remoteIp;
  }

  public void setRemoteIp(final String remoteIp) {
    this.remoteIp = remoteIp;
  }

  public String getServerIp() {
    return serverIp;
  }

  public void setServerIp(final String serverIp) {
    this.serverIp = serverIp;
  }

  public String getReferer() {
    return referer;
  }

  public void setReferer(final String referer) {
    this.referer = referer;
  }

  public String getLatency() {
    return latency;
  }

  public void setLatency(final String latency) {
    this.latency = latency;
  }

  public boolean isCacheLookup() {
    return cacheLookup;
  }

  public void setCacheLookup(final boolean cacheLookup) {
    this.cacheLookup = cacheLookup;
  }

  public boolean isCacheHit() {
    return cacheHit;
  }

  public void setCacheHit(final boolean cacheHit) {
    this.cacheHit = cacheHit;
  }

  public boolean isCacheValidatedWithOriginServer() {
    return cacheValidatedWithOriginServer;
  }

  public void setCacheValidatedWithOriginServer(final boolean cacheValidatedWithOriginServer) {
    this.cacheValidatedWithOriginServer = cacheValidatedWithOriginServer;
  }

  public long getCacheFillBytes() {
    return cacheFillBytes;
  }

  public void setCacheFillBytes(final long cacheFillBytes) {
    this.cacheFillBytes = cacheFillBytes;
  }

  public String getProtocol() {
    return protocol;
  }

  public void setProtocol(final String protocol) {
    this.protocol = protocol;
  }
}
