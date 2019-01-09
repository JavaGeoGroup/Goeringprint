package ge.gogina;

import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;

public class Attributes {

	private HashPrintRequestAttributeSet hashPrintRequestAttributeSet;
	private boolean isSmall;
	private String jobName;
	private int pageFormat;// = PageFormat.LANDSCAPE;
	private PrintService service;
	private String pdfSize;

	public String getPdfSize() {
		return pdfSize;
	}

	public void setPdfSize(String pdfSize) {
		this.pdfSize = pdfSize;
	}

	public PrintService getService() {
		return service;
	}

	public void setService(PrintService service) {
		this.service = service;
	}

	public HashPrintRequestAttributeSet getHashPrintRequestAttributeSet() {
		return hashPrintRequestAttributeSet;
	}

	public void setHashPrintRequestAttributeSet(HashPrintRequestAttributeSet hashPrintRequestAttributeSet) {
		this.hashPrintRequestAttributeSet = hashPrintRequestAttributeSet;
	}

	public boolean isSmall() {
		return isSmall;
	}

	public void setSmall(boolean isSmall) {
		this.isSmall = isSmall;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public int getPageFormat() {
		return pageFormat;
	}

	public void setPageFormat(int pageFormat) {
		this.pageFormat = pageFormat;
	}

}
