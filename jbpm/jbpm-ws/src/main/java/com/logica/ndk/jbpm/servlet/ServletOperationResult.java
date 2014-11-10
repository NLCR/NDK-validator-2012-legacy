package com.logica.ndk.jbpm.servlet;

import java.util.ArrayList;
import java.util.List;

public class ServletOperationResult {
  private State state;
  private List<String> messages;

  public ServletOperationResult() {
    this.state = State.OK;
    this.messages = new ArrayList<String>();
  }

  public enum State {
    OK, ERROR;
  }

  public void addMessage(String message, State state) {
    if (this.messages == null) {
      this.messages = new ArrayList<String>();
    }
    this.messages.add(message);
    this.state = state;
  }

  public State getState() {
    return state;
  }

  public void setState(State state) {
    this.state = state;
  }

  public List<String> getMessages() {
    return messages;
  }

  public void setMessages(List<String> messages) {
    this.messages = messages;
  }

}
