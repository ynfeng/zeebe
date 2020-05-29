package io.zeebe.cloud.google.logging.stackdriver;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_EMPTY)
public final class Label {
  @JsonProperty("label_name")
  private String labelValue;

  public String getLabelValue() {
    return labelValue;
  }

  public void setLabelValue(final String labelValue) {
    this.labelValue = labelValue;
  }
}
