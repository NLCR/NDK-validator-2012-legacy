package com.logica.ndk.tm.utilities.integration.wf;

import java.util.List;

/**
 * Generic list of items - API serialization
 * 
 * @author majdaf
 * @param <T>
 */
public class ItemsList<T> {
  List<T> items;

  public List<T> getItems() {
    return items;
  }

  public void setItems(List<T> items) {
    this.items = items;
  }

  @Override
  public String toString() {
    return "ItemsList{" +
        "items=" + items +
        '}';
  }
}
