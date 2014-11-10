package com.logica.ndk.tm.utilities.transformation.format.migration;

import com.logica.ndk.tm.utilities.jhove.MixEnvBean;

public class PackageMetadata {

	public static final String DEFAULT_LOCALITY_CODE = "NKCR_SLK";

	private String barcode;
	private String field001;
	private String issueNumber;
	private String alephLocation;
	private String alephCode;
	private String localityCode = DEFAULT_LOCALITY_CODE;
	private String template;
	private String pageType;
	private Boolean rename;
	@Nullable
	private Integer dpi;
	@Nullable
	private String note;
	
	private MixEnvBean color = new MixEnvBean();
	private MixEnvBean grayscale = new MixEnvBean();

	public PackageMetadata() {
	}

	public PackageMetadata(String barcode, String field001, String issueNumber,
	    String alephLocation, String alephCode,
			String localityCode, String template, String pageType,
			boolean rename, int dpi, String note, MixEnvBean envBeanColor,
			MixEnvBean envBeanGreyscale) {
		super();
		this.barcode = barcode;
		this.field001 = field001;
		this.issueNumber = issueNumber;
		this.alephLocation = alephLocation;
		this.alephCode = alephCode;
		this.localityCode = localityCode;
		this.template = template;
		this.pageType = pageType;
		this.rename = rename;
		this.dpi = dpi;
		this.color = envBeanColor;
		this.grayscale = envBeanGreyscale;
		this.note = note;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public String getField001() {
		return field001;
	}

	public void setField001(String field001) {
		this.field001 = field001;
	}

	public String getIssueNumber() {
		return issueNumber;
	}

	public void setIssueNumber(String issueNumber) {
		this.issueNumber = issueNumber;
	}

	public String getAlephLocation() {
		return alephLocation;
	}

	public void setAlephLocation(String alephLocation) {
		this.alephLocation = alephLocation;
	}

	public String getAlephCode() {
		return alephCode;
	}

	public void setAlephCode(String alephCode) {
		this.alephCode = alephCode;
	}

	public String getLocalityCode() {
		return localityCode;
	}

	public void setLocalityCode(String localityCode) {
		this.localityCode = localityCode;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getPageType() {
		return pageType;
	}

	public void setPageType(String pageType) {
		this.pageType = pageType;
	}

	public Boolean isRename() {
		return rename;
	}

	public void setRename(Boolean rename) {
		this.rename = rename;
	}

	public Integer getDpi() {
		return dpi;
	}

	public void setDpi(Integer dpi) {
		this.dpi = dpi;
	}

	public MixEnvBean getEnvBeanColor() {
		return color;
	}

	public void setEnvBeanColor(MixEnvBean envBeanColor) {
		this.color = envBeanColor;
	}

	public MixEnvBean getEnvBeanGrayscale() {
		return grayscale;
	}

	public void setEnvBeanGrayscale(MixEnvBean envBeanGreyscale) {
		this.grayscale = envBeanGreyscale;
	}

  public void setNote(String note) {
    this.note = note;
  }

  public String getNote() {
    return note;
  }

	
}
