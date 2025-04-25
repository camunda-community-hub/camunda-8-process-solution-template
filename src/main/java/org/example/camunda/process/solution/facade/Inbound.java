package org.example.camunda.process.solution.facade;

import io.camunda.zeebe.client.ZeebeClient;
import org.example.camunda.process.solution.ProcessConstants;
import org.example.camunda.process.solution.ProcessVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inbound")
public class Inbound {

  private static final Logger LOG = LoggerFactory.getLogger(Inbound.class);
  private final ZeebeClient zeebe;

  public Inbound(ZeebeClient client) {
    this.zeebe = client;
  }

  @PostMapping("/start")
  public String webhookStart(@RequestBody ProcessVariables variables) {

    LOG.info(
        "Inbound connector `webhook1` was triggered for process `" + ProcessConstants.BPMN_PROCESS_ID + "` with variables: " + variables);

    if(variables == null || variables.getBusinessKey() == null) {
      return "businessKey is required";
    }

    zeebe.newCreateInstanceCommand()
        .bpmnProcessId(ProcessConstants.BPMN_PROCESS_ID)
        .latestVersion()
        .variables(variables)
        .send();

    return "success";
  }

  @PostMapping("/webhook")
  public String webhook(@RequestBody ProcessVariables variables) {

    LOG.info(
        "Inbound connector `webhook1` was triggered for process `" + ProcessConstants.BPMN_PROCESS_ID + "` with variables: " + variables);

    if(variables == null || variables.getBusinessKey() == null) {
      return "businessKey is required";
    }

    zeebe.newPublishMessageCommand()
        .messageName("webhook")
        .correlationKey("businessKey")
        .variables(variables)
        .send();

    return "success";
  }

  @PostMapping("/message/{messageName}/{correlationKey}")
  public void publishMessage(
      @PathVariable String messageName,
      @PathVariable String correlationKey,
      @RequestBody ProcessVariables variables) {

    LOG.info(
        "Publishing message `{}` with correlation key `{}` and variables: {}",
        messageName,
        correlationKey,
        variables);

    zeebe
        .newPublishMessageCommand()
        .messageName(messageName)
        .correlationKey(correlationKey)
        .variables(variables)
        .send();
  }
}
