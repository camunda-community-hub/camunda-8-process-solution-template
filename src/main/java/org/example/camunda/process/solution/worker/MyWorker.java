package org.example.camunda.process.solution.worker;

import io.camunda.client.CamundaClient;
import io.camunda.client.annotation.JobWorker;
import io.camunda.client.annotation.VariablesAsType;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.api.worker.JobClient;
import org.example.camunda.process.solution.ProcessVariables;
import org.example.camunda.process.solution.service.MyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MyWorker {

  private static final Logger LOG = LoggerFactory.getLogger(MyWorker.class);

  private final MyService myService;
  private final CamundaClient camundaClient;

  public MyWorker(MyService myService, CamundaClient camundaClient) {
    this.myService = myService;
    this.camundaClient = camundaClient;
  }

  @JobWorker(type = "myService")
  public ProcessVariables invokeMyService(@VariablesAsType ProcessVariables variables) {
    LOG.info("Invoking myService with variables: " + variables);

    boolean result = myService.myOperation(variables.getBusinessKey());

    return new ProcessVariables()
        .setResult(result); // new object to avoid sending unchanged variables
  }

  @JobWorker(type = "runMyService", timeout = 1000, maxRetries = 1)
  public void runMyService(JobClient jobClient, ActivatedJob job) {

    // Tell the engine we need more time
    camundaClient.newUpdateJobCommand(job)
        .updateTimeout(5000)
        .send()
        .join();

    try {

      // Perform business logic here

      jobClient
          .newCompleteCommand(job.getKey())
          .send()
          .join();

    } catch (Exception e) {

      // Tell the engine that the job failed and we don't want to retry

      jobClient
          .newFailCommand(job.getKey())
          .retries(1)
          .errorMessage(e.getMessage())
          .send()
          .join();
    }
  }
}
