/**
 * 
 */
package com.logica.ndk.tm.cdm;

import java.io.File;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;

import com.google.common.collect.ImmutableMap;

/**
 * @author kovalcikm
 */
public class CDMModsHelper{

  Document modsDoc;
  XPath xPath;

  public CDMModsHelper(Document modsDoc) {
    this.modsDoc = modsDoc;
  }

  public String getModsId() {
    String modsId = null;
    xPath = DocumentHelper.createXPath("//mods:mods/@ID");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(modsDoc);
    if (node != null) {
      modsId = node.getText();
      return modsId;
    }
    else {
      return null;
    }
  }

  public String getTitle() {
    String title;
    xPath = DocumentHelper.createXPath("//mods:title");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(modsDoc);
    if (node != null) {
      title = node.getText();
      return title;
    }
    else {
      return null;
    }
  }

  public String getNameType() {
    String nameType;
    xPath = DocumentHelper.createXPath("//mods:name/@type");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(modsDoc);
    if (node != null) {
      nameType = node.getText();
      return nameType;
    }
    else {
      return null;
    }
  }

  public String getNamePart() {
    String namePart;
    xPath = DocumentHelper.createXPath("//mods:name/mods:namePart");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(modsDoc);
    if (node != null) {
      namePart = node.getText();
      return namePart;
    }
    else {
      return null;
    }
  }

  public String getNamePartType() {
    String namePartType;
    xPath = DocumentHelper.createXPath("//mods:name/mods:namePart/@type");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(modsDoc);
    if (node != null) {
      namePartType = node.getText();
      return namePartType;
    }
    else {
      return null;
    }
  }

  public String getNameRole() {
    String role;
    xPath = DocumentHelper.createXPath("//mods:name/mods:role");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(modsDoc);
    if (node != null) {
      role = node.getText();
      return role;
    }
    else {
      return null;
    }
  }

  public String getRoleTerm() {
    String roleTerm;
    xPath = DocumentHelper.createXPath("//mods:name/mods:role/mods:roleTerm");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(modsDoc);
    if (node != null) {
      roleTerm = node.getText();
      return roleTerm;
    }
    else {
      return null;
    }
  }

  public String getGenre() {
    String genre;
    xPath = DocumentHelper.createXPath("//mods:genre");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(modsDoc);
    if (node != null) {
      genre = node.getText();
      return genre;
    }
    else {
      return null;
    }
  }
  
  public String getGenreType() {
    String genre;
    xPath = DocumentHelper.createXPath("//mods:genre/@type");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(modsDoc);
    if (node != null) {
      genre = node.getText();
      return genre;
    }
    else {
      return null;
    }
  }

  public String getOriginInfo() {
    String originInfo;
    xPath = DocumentHelper.createXPath("//mods:originInfo");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(modsDoc);
    if (node != null) {
      originInfo = node.getText();
      return originInfo;
    }
    else {
      return null;
    }
  }

  public String getDateIssued() {
    String dateIssued;
    xPath = DocumentHelper.createXPath("//mods:originInfo/mods:dateIssued");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(modsDoc);
    if (node != null) {
      dateIssued = node.getText();
      return dateIssued;
    }
    else {
      return null;
    }
  }

  public String getIssuance() {
    String issuance;
    xPath = DocumentHelper.createXPath("//mods:originInfo/mods:issuance");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(modsDoc);
    if (node != null) {
      issuance = node.getText();
      return issuance;
    }
    else {
      return null;
    }
  }

  public String getLanguage() {
    String language;
    xPath = DocumentHelper.createXPath("//mods:language");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(modsDoc);
    if (node != null) {
      language = node.getText();
      return language;
    }
    else {
      return null;
    }
  }

  public String getLanguageTerm() {
    String languageTerm;
    xPath = DocumentHelper.createXPath("//mods:language/mods:languageTerm");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(modsDoc);
    if (node != null) {
      languageTerm = node.getText();
      return languageTerm;
    }
    else {
      return null;
    }
  }

  public String getLanguageTermAuthority() {
    String authority;
    xPath = DocumentHelper.createXPath("//mods:language/mods:languageTerm/@authority");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(modsDoc);
    if (node != null) {
      authority = node.getText();
      return authority;
    }
    else {
      return null;
    }
  }

  public String getLanguageTermObjectPart() {
    String objectPart;
    xPath = DocumentHelper.createXPath("//mods:language/mods:languageTerm/@objectPart");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(modsDoc);
    if (node != null) {
      objectPart = node.getText();
      return objectPart;
    }
    else {
      return null;
    }
  }

  public String getLanguageTermType() {
    String type;
    xPath = DocumentHelper.createXPath("//mods:language/mods:languageTerm/@type");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(modsDoc);
    if (node != null) {
      type = node.getText();
      return type;
    }
    else {
      return null;
    }
  }

  public String getPhysicalDescription() {
    String physicalDescription;
    xPath = DocumentHelper.createXPath("//mods:physicalDescription");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(modsDoc);
    if (node != null) {
      physicalDescription = node.getText();
      return physicalDescription;
    }
    else {
      return null;
    }
  }
  
  public String getPhysicalDescriptionForm(){
    String form;
    xPath = DocumentHelper.createXPath("//mods:physicalDescription/mods:form");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(modsDoc);
    if (node != null) {
      form = node.getText();
      return form;
    }
    else {
      return null;
    }
  }
  
  public String getPhysicalDescriptionFormAuthority(){
    String authority;
    xPath = DocumentHelper.createXPath("//mods:physicalDescription/mods:form/@authority");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(modsDoc);
    if (node != null) {
      authority = node.getText();
      return authority;
    }
    else {
      return null;
    }
  }
  
  public String getTopic(){
    String topic;
    xPath = DocumentHelper.createXPath("//mods:topic");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(modsDoc);
    if (node != null) {
      topic = node.getText();
      return topic;
    }
    else {
      return null;
    }
  }
  
  public String getClasification(){
    String classification;
    xPath = DocumentHelper.createXPath("//mods:classification");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(modsDoc);
    if (node != null) {
      classification = node.getText();
      return classification;
    }
    else {
      return null;
    }
  }

  public String getClasificationAuthority(){
    String authority;
    xPath = DocumentHelper.createXPath("//mods:classification/@authority");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(modsDoc);
    if (node != null) {
      authority = node.getText();
      return authority;
    }
    else {
      return null;
    }
  }
  
  public String getIdentifier(){
    String identifier;
    xPath = DocumentHelper.createXPath("//mods:identifier");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(modsDoc);
    if (node != null) {
      identifier = node.getText();
      return identifier;
    }
    else {
      return null;
    }
  }
  
  public String getIdentifierType(){
    String type;
    xPath = DocumentHelper.createXPath("//mods:identifier/@type");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(modsDoc);
    if (node != null) {
      type = node.getText();
      return type;
    }
    else {
      return null;
    }
  }
  
  public String getLocation(){
    String location;
    xPath = DocumentHelper.createXPath("//mods:location");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(modsDoc);
    if (node != null) {
      location = node.getText();
      return location;
    }
    else {
      return null;
    }
  }
  
  public String getPhysicalLocation(){
    String physicalLocation;
    xPath = DocumentHelper.createXPath("//mods:location/mods:physicalLocation");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(modsDoc);
    if (node != null) {
      physicalLocation = node.getText();
      return physicalLocation;
    }
    else {
      return null;
    }
  }
  
  public String getRecordInfo(){
    String recordInfo;
    xPath = DocumentHelper.createXPath("//mods:recordInfo");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(modsDoc);
    if (node != null) {
      recordInfo = node.getText();
      return recordInfo;
    }
    else {
      return null;
    }
  }
  
  public String getRecordCreationDate(){
    String creationDate;
    xPath = DocumentHelper.createXPath("//mods:recordInfo/mods:recordCreationDate");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(modsDoc);
    if (node != null) {
      creationDate = node.getText();
      return creationDate;
    }
    else {
      return null;
    }
  }
  
  public String getRecordCreationDateEncoding(){
    String encoding;
    xPath = DocumentHelper.createXPath("//mods:recordInfo/mods:recordCreationDate/@encoding");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(modsDoc);
    if (node != null) {
      encoding = node.getText();
      return encoding;
    }
    else {
      return null;
    }
  }
  
  public String getSubject(){
    String subject;
    xPath = DocumentHelper.createXPath("//mods:subject");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(modsDoc);
    if (node != null) {
      subject = node.getText();
      return subject;
    }
    else {
      return null;
    }
  }
  
  public String getBarCode(){
    String barCode;
    xPath = DocumentHelper.createXPath("//mods:identifier[@type='barCode']");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(modsDoc);
    if (node != null) {
      barCode = node.getText();
      return barCode;
    }
    else{
      return null;
    }
  }
  
  public String getSubTitle(){
    String title;
    xPath = DocumentHelper.createXPath("//mods:subTitle");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(modsDoc);
    if (node != null) {
      title = node.getText();
      return title;
    }
    else {
      return null;
    }
  }
  
  public String getShelfLocator(){
    String selfLocator;
    xPath = DocumentHelper.createXPath("//mods:location/mods:shelfLocator");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(modsDoc);
    if (node != null) {
      selfLocator = node.getText();
      return selfLocator;
    }
    else {
      return null;
    }
  }
  
  public String getTitleInfo(){
    String titleInfo;
    xPath = DocumentHelper.createXPath("//mods:titleInfo");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    Node node = xPath.selectSingleNode(modsDoc);
    if (node != null) {
      titleInfo = node.getText();
      return titleInfo;
    }
    else {
      return null;
    }
    
  }
}


