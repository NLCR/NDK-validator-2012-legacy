package com.logica.ndk.tm.utilities.integration.wf.task;

import java.util.List;

public class TaskHeaderList {
  List<TaskHeader> items;

  public List<TaskHeader> getItems() {
    return items;
  }

  public void setItems(List<TaskHeader> items) {
    this.items = items;
  }

  @Override
  public String toString() {
    return "TaskHeaderList{" +
        "items=" + items +
        '}';
  }
}
