package org.example.camunda.process.solution;

import static io.camunda.process.test.api.CamundaAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import org.example.camunda.process.solution.service.MyService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * @see
 *     https://docs.camunda.io/docs/components/best-practices/development/testing-process-definitions/#writing-process-tests-in-java
 */
@SpringBootTest(classes = ProcessApplication.class) // will deploy BPMN & DMN models
@CamundaSpringProcessTest
public class ProcessUnitTest {

  @Autowired private CamundaClient camunda;

  @MockitoBean private MyService myService;

  @Test
  public void testHappyPath() throws Exception {
    // define mock behavior
    when(myService.myOperation(anyString())).thenReturn(true);

    // prepare data
    final ProcessVariables variables = new ProcessVariables().setBusinessKey("23");

    // start a process instance
    final ProcessInstanceEvent processInstance =
        camunda
            .newCreateInstanceCommand()
            .bpmnProcessId(ProcessConstants.BPMN_PROCESS_ID)
            .latestVersion()
            .variables(variables)
            .send()
            .join();

    // check that service task has been completed and process is ended with the right result
    assertThat(processInstance)
        .hasCompletedElements("Task_InvokeService")
        .hasVariable("result", true)
        .isCompleted();

    // verify that side effects have happened
    Mockito.verify(myService).myOperation("23");

    // ensure no other side effects
    Mockito.verifyNoMoreInteractions(myService);
  }
}
