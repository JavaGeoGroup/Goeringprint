package ge.gogina;

import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.Chromaticity;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.MediaTray;
import javax.print.attribute.standard.PageRanges;
import javax.print.attribute.standard.PrinterName;
import javax.print.attribute.standard.RequestingUserName;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

public class AttributesUtil {

	private static Map<String, MediaSizeName> mediaSize = new HashMap<String, MediaSizeName>() {
		private static final long serialVersionUID = 6710473856804556533L;

		{
			put("a4", MediaSizeName.ISO_A4);
			put("a5", MediaSizeName.ISO_A5);
			put("a6", MediaSizeName.ISO_A6);
			put("b5", MediaSizeName.ISO_B5);
			put("b6", MediaSizeName.ISO_B6);
			put("jisb5", MediaSizeName.JIS_B5);
			put("jisb6", MediaSizeName.JIS_B6);
			put("letter", MediaSizeName.NA_LETTER);
			put("legal", MediaSizeName.NA_LEGAL);
			put("postcardus", MediaSizeName.JAPANESE_POSTCARD);
			put("executive", MediaSizeName.EXECUTIVE);
		}
	};

	private static PrintService getPrintService(String[] arr) {

		PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].equals("-printer")) {
				PrintServiceAttributeSet printServiceAttributeSet = new HashPrintServiceAttributeSet();
				printServiceAttributeSet.add(new PrinterName(arr[i + 1], null));
				defaultService = PrintServiceLookup.lookupPrintServices(null, printServiceAttributeSet)[0];
			}
		}
		return defaultService;
	}

	public static void printDevices() {
		PrintService services[] = PrinterJob.lookupPrintServices();
		for (int j = 0; j < services.length; j++) {
			System.out.println(j + ") " + services[j].getName());
		}
	}

	public static Attributes getHashPrintRequestAttributeSet(String[] arr, PDDocument doc) {

		PrintService service = getPrintService(arr);
		Attributes attributes = new Attributes();
		attributes.setService(service);

		Map<Integer, Media> trayMap = new HashMap<>();
		DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;

		PDRectangle rectangle = doc.getPage(0).getMediaBox();
		int pageFormat = PageFormat.LANDSCAPE;
		if (rectangle.getWidth() < rectangle.getHeight()) {
			pageFormat = PageFormat.PORTRAIT;
		}

		Object o = service.getSupportedAttributeValues(Media.class, flavor, null);
		if (o != null && o.getClass().isArray()) {
			for (Media media : (Media[]) o) {
				if (media instanceof MediaTray) {
					trayMap.put(media.getValue(), media);
				}
			}
		}

		HashPrintRequestAttributeSet hashPrintRequestAttributeSet = new HashPrintRequestAttributeSet();
		int copies = 1;
		Chromaticity chromaticity = Chromaticity.COLOR;
		MediaSizeName mediaSizeName = MediaSizeName.ISO_A4;
		MediaSize ms = MediaSize.getMediaSizeForName(mediaSizeName);
		attributes.setPdfSize(getPdfSize(doc, ms));
		boolean ispaperInputPassed = false;

		String userName = System.getProperty("user.name");
		for (int i = 0; i < arr.length; i += 2) {
			if (arr[i].equals("-copies")) {
				copies = Integer.parseInt(arr[i + 1]);
			}

			if (arr[i].equals("-color") && arr[i + 1].equals("n")) {
				chromaticity = Chromaticity.MONOCHROME;
			}
			if (arr[i].equals("-user")) {
				userName = arr[i + 1];
			}
			if (arr[i].equals("-paperinput")) {
				if (trayMap.containsKey(Integer.parseInt(arr[i + 1]))) {
					ispaperInputPassed = true;
					hashPrintRequestAttributeSet.add(trayMap.get(Integer.parseInt(arr[i + 1])));
				} else {
					System.out.println("Tray : " + arr[i + 1] + " is not supported to print, using default");
				}

			}
			if (arr[i].equals("-printpage")) {
				if (arr[i + 1].equals("firstpage")) {
					hashPrintRequestAttributeSet.add(new PageRanges(1));
				} else if (arr[i + 1].equals("lastpage")) {
					hashPrintRequestAttributeSet.add(new PageRanges(doc.getNumberOfPages()));
				} else if (arr[i + 1].contains("-lastpage")) {
					hashPrintRequestAttributeSet
							.add(new PageRanges(Integer.parseInt(arr[i + 1].substring(0, 1)), doc.getNumberOfPages()));
				} else if (arr[i + 1].contains("-")) {
					hashPrintRequestAttributeSet.add(new PageRanges(Integer.parseInt(arr[i + 1].substring(0, 1)),
							Integer.parseInt(arr[i + 1].substring(2))));
				} else {
					hashPrintRequestAttributeSet.add(new PageRanges(Integer.parseInt(arr[i + 1])));
				}
			}

			if (arr[i].equals("-pagesize")) {
				mediaSizeName = getMediaSizeName(arr[i + 1]);
				ms = MediaSize.getMediaSizeForName(mediaSizeName);
				attributes.setPdfSize(getPdfSize(doc, ms));
			}
			if (arr[i].equals("-job")) {
				attributes.setJobName(arr[i + 1]);
			}

			if (arr[i].equals("-pageformat")) {
				if (arr[i + 1].equals("landscape")) {
					pageFormat = PageFormat.LANDSCAPE;
				} else if (arr[i + 1].equals("portrait"))
					pageFormat = PageFormat.PORTRAIT;
			}

			if (arr[i].equals("-pdfsize")) {
				attributes.setPdfSize(arr[i + 1]);
				if (arr[i + 1].equals("actualsize")) {
					for (int j = 0; j < doc.getNumberOfPages(); j++) {
						PDRectangle mediaBox = doc.getPage(j).getMediaBox();
						if (mediaBox.getWidth() > ms.getX(MediaSize.MM)) {
							System.out.println("PDf page Width: " + mediaBox.getWidth() + "Paper width: "
									+ ms.getX(MediaSize.MM) + " page's width Is too large");
							System.exit(0);
						}
						if (mediaBox.getHeight() > ms.getY(MediaSize.MM)) {
							System.out.println("PDf page Height: " + mediaBox.getHeight() + "Paper height:"
									+ ms.getY(MediaSize.MM) + "page's height Is too large");
							System.exit(0);
						}
					}
				}
			}

		}

		PDRectangle mediaBox = doc.getPage(0).getMediaBox();
		if (mediaBox.getWidth() < ms.getX(MediaSize.MM)) {
			attributes.setSmall(true);
		}
		if (mediaBox.getHeight() > ms.getY(MediaSize.MM)) {
			attributes.setSmall(true);
		}
		if (!ispaperInputPassed) {
			hashPrintRequestAttributeSet.add(mediaSizeName);
		}
		hashPrintRequestAttributeSet.add(new Copies(copies));
		hashPrintRequestAttributeSet.add(chromaticity);
		hashPrintRequestAttributeSet.add(new RequestingUserName(userName, Locale.getDefault()));

		attributes.setHashPrintRequestAttributeSet(hashPrintRequestAttributeSet);
		attributes.setPageFormat(pageFormat);

		return attributes;
	}

	private static String getPdfSize(PDDocument doc, MediaSize ms) {
		String pdfSize = "";
		PDRectangle mediaBox = doc.getPage(0).getMediaBox();
		if (mediaBox.getWidth() > ms.getX(MediaSize.MM) || mediaBox.getHeight() > ms.getY(MediaSize.MM)) {
			pdfSize = "adjust";
		} else {
			pdfSize = "actualsize";
		}
		return pdfSize;

	}

	private static MediaSizeName getMediaSizeName(String name) {
		MediaSizeName mediaSizeName = MediaSizeName.ISO_A4;
		mediaSizeName = mediaSize.get(name);
		return mediaSizeName;
	}

	public static Map<String, MediaSizeName> getMediaSizeMap() {
		return mediaSize;
	}

}
