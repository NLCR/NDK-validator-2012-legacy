package com.logica.ndk.tm.utilities.integration.aleph;

import java.io.Serializable;

/**
 * @author ondrusekl
 */
public class AlephItem implements Serializable {

  private static final long serialVersionUID = 7691633333760911019L;

  private String recKey;
  private String barcode;
  private String subLibrary;
  private String collection;
  private String itemStatus;
  private String note;
  private String callNo1;
  private String callNo2;
  private String description;
  private String chronologicalI;
  private String chronologicalJ;
  private String chronologicalK;
  private String enumerationA;
  private String enumerationB;
  private String enumerationC;
  private String library;
  private String onHold;
  private String requested;
  private String expected;

  public String getRecKey() {
    return recKey;
  }

  public void setRecKey(String recKey) {
    this.recKey = recKey;
  }

  public String getSubLibrary() {
    return subLibrary;
  }

  public String getBarcode() {
    return barcode;
  }

  public void setBarCode(String barcode) {
    this.barcode = barcode;
  }

  public void setSubLibrary(String subLibrary) {
    this.subLibrary = subLibrary;
  }

  public String getCollection() {
    return collection;
  }

  public void setCollection(String collection) {
    this.collection = collection;
  }

  public String getItemStatus() {
    return itemStatus;
  }

  public void setItemStatus(String itemStatus) {
    this.itemStatus = itemStatus;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public String getCallNo1() {
    return callNo1;
  }

  public void setCallNo1(String callNo1) {
    this.callNo1 = callNo1;
  }

  public String getCallNo2() {
    return callNo2;
  }

  public void setCallNo2(String callNo2) {
    this.callNo2 = callNo2;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getChronologicalI() {
    return chronologicalI;
  }

  public void setChronologicalI(String chronologicalI) {
    this.chronologicalI = chronologicalI;
  }

  public String getChronologicalJ() {
    return chronologicalJ;
  }

  public void setChronologicalJ(String chronologicalJ) {
    this.chronologicalJ = chronologicalJ;
  }

  public String getChronologicalK() {
    return chronologicalK;
  }

  public void setChronologicalK(String chronologicalK) {
    this.chronologicalK = chronologicalK;
  }

  public String getEnumerationA() {
    return enumerationA;
  }

  public void setEnumerationA(String enumerationA) {
    this.enumerationA = enumerationA;
  }

  public String getEnumerationB() {
    return enumerationB;
  }

  public void setEnumerationB(String enumerationB) {
    this.enumerationB = enumerationB;
  }

  public String getEnumerationC() {
    return enumerationC;
  }

  public void setEnumerationC(String enumerationC) {
    this.enumerationC = enumerationC;
  }

  public String getLibrary() {
    return library;
  }

  public void setLibrary(String library) {
    this.library = library;
  }

  public String getOnHold() {
    return onHold;
  }

  public void setOnHold(String onHold) {
    this.onHold = onHold;
  }

  public String getRequested() {
    return requested;
  }

  public void setRequested(String requested) {
    this.requested = requested;
  }

  public String getExpected() {
    return expected;
  }

  public void setExpected(String expected) {
    this.expected = expected;
  }

}
