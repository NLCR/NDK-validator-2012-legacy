package com.logica.ndk.tm.utilities.transformation.sip1.infoXmlFile;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "info")
@XmlType(propOrder = { "created", "packageid", "titleid", "collection", "institution", "creator", "size", "itemlist", "checkSum", "note", "metadataversion" })
public class XMLSIP1Info {

	private String created;
	private String packageid;
	private List<Title> titleid;
	private String collection;
	private String institution;
	private String creator;
	private String size;
	private ItemList itemlist;
	private CheckSum checkSum;
	private ArrayList<String> note;
	private String metadataversion;

	public XMLSIP1Info() {
		note = new ArrayList<String>();
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public String getPackageid() {
		return packageid;
	}

	public void setPackageid(String packageid) {
		this.packageid = packageid;
	}

	public List<Title> getTitleid() {
		return titleid;
	}

	public void setTitleid(List<Title> titleid) {
		this.titleid = titleid;
	}

	public String getCollection() {
		return collection;
	}

	public void setCollection(String collection) {
		this.collection = collection;
	}

	public String getInstitution() {
		return institution;
	}

	public void setInstitution(String institution) {
		this.institution = institution;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public ItemList getItemlist() {
		return itemlist;
	}

	public void setItemlist(ItemList itemlist) {
		this.itemlist = itemlist;
	}

	public CheckSum getCheckSum() {
		return checkSum;
	}

	public void setCheckSum(CheckSum checkSum) {
		this.checkSum = checkSum;
	}

	public ArrayList<String> getNote() {
		return note;
	}

	public void setNote(ArrayList<String> note) {
		this.note = note;
	}

	public void addNote(String note) {
		this.note.add(note);
	}

	public String getMetadataversion() {
		return metadataversion;
	}

	public void setMetadataversion(String metadataversion) {
		this.metadataversion = metadataversion;
	}

}
