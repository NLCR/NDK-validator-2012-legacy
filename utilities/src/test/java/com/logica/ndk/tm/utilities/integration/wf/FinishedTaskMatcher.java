package com.logica.ndk.tm.utilities.integration.wf;

import org.mockito.ArgumentMatcher;

import com.logica.ndk.tm.utilities.integration.wf.finishedTask.FinishedTask;

public class FinishedTaskMatcher extends ArgumentMatcher<FinishedTask> {
  FinishedTask expectedFinishedTask;   
  public FinishedTaskMatcher(FinishedTask expectedFinishedTask) {
    this.expectedFinishedTask = expectedFinishedTask;
  }

  @Override
  public boolean matches(Object o) {
    if (o instanceof FinishedTask) {
      FinishedTask finishedTask = (FinishedTask)o;
      if (
          (finishedTask.getNote() == null && expectedFinishedTask.getNote() != null)
          || (finishedTask.getNote() != null && expectedFinishedTask.getNote() == null)
          || (finishedTask.getNote() != null && expectedFinishedTask.getNote() != null && !finishedTask.getNote().equals(expectedFinishedTask.getNote()))
          ) {
        return false;
      }
      if (
          (finishedTask.getErrorMessages() == null && expectedFinishedTask.getErrorMessages() != null)
          || (finishedTask.getErrorMessages() != null && expectedFinishedTask.getErrorMessages() == null)
          || (finishedTask.getErrorMessages() != null && expectedFinishedTask.getErrorMessages() != null && !finishedTask.getErrorMessages().toString().equals(expectedFinishedTask.getErrorMessages().toString()))
          ) {
        return false;
      }
      
      if (expectedFinishedTask.isError() && finishedTask.isError() != expectedFinishedTask.isError()) {
        return false;
      }
      
      return true;
    } else {
      return true;
    }
  }

}
