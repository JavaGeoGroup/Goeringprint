package ge.gogina;

import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.MediaTray;
import javax.print.attribute.standard.PrinterName;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.apache.pdfbox.printing.PDFPrintable;
import org.apache.pdfbox.printing.Scaling;

public class App {

	private static void printDevices(String[] args) {

		PrintService service = PrintServiceLookup.lookupDefaultPrintService();
		boolean hasTray = false;
		boolean hasPageSize = false;
		if (args.length % 2 == 0) {
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("-printer")) {
					if (args[i + 1].equals("?")) {
						AttributesUtil.printDevices();
					} else {
						PrintServiceAttributeSet printServiceAttributeSet = new HashPrintServiceAttributeSet();
						printServiceAttributeSet.add(new PrinterName(args[i + 1], null));
						service = PrintServiceLookup.lookupPrintServices(null, printServiceAttributeSet)[0];
					}
				}
				if (args[i].equals("-paperinput")) {
					hasTray = true;
				}
				if(args[i].equals("-pagesize")) {
					hasPageSize = true;
				}
			}

			if (hasTray) {
				Map<Integer, Media> trayMap = new HashMap<>();
				DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
				Object o = service.getSupportedAttributeValues(Media.class, flavor, null);
				if (o != null && o.getClass().isArray()) {
					for (Media media : (Media[]) o) {
						if (media instanceof MediaTray) {
							trayMap.put(media.getValue(), media);
						}
					}
					System.out.println("List of all MediaTrays");
					int t = 0;
					for (Map.Entry<Integer, Media> entry : trayMap.entrySet()) {
						System.out
								.println(t + ") name :" + entry.getValue() + ", value :" + entry.getValue().getValue());
						t++;
					}
				}
			}
			if(hasPageSize) {
				Map<String,MediaSizeName> pageSizes = AttributesUtil.getMediaSizeMap();
				pageSizes.forEach((name,media) ->System.out.println("name : " + name + ", mediaName : " + media));
			}
			
			System.exit(0);
		}

	}

	public static void main(String args[]) {

		printDevices(args);

		PrinterJob job = PrinterJob.getPrinterJob();
		try (PDDocument doc = PDDocument.load(new FileInputStream(args[args.length - 1]))) {

			// initialize job with doc
			job.setPageable(new PDFPageable(doc));

			Attributes attributes = AttributesUtil.getHashPrintRequestAttributeSet(args, doc);

			// set passed or default service to job
			job.setPrintService(attributes.getService());

			PageFormat pf = job.defaultPage();

			// initializes PDF files orientation
			pf.setOrientation(attributes.getPageFormat());
			// sets jobs name and orientation
			if (attributes.getJobName() != null) {
				job.setJobName(attributes.getJobName());
			}
			if (!(attributes.getPdfSize().equals("actualsize") && attributes.isSmall())) {
				job.setPrintable(new PDFPrintable(doc, Scaling.SCALE_TO_FIT), pf);
			} else {
				job.setPrintable(new PDFPrintable(doc), pf);
			}

			job.print(attributes.getHashPrintRequestAttributeSet());

		} catch (PrinterException | IOException e) {
			System.out.println(e.getMessage());
		}

	}
}
