package org.example.camunda.process.solution;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class ProcessVariables {

  private String businessKey;
  private Boolean result;

}
