import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;


public class XMLTrec extends DefaultHandler{

	private static final String QID = "qid";
	private static final String RANK = "rank";
	private static final String SCORE = "score";

	private static final String DOC = "DOC";
	
	/**
	 * converts TREC SGML file into an xml file which can be parsed later on.
	 * @param inputFile
	 * @param outputXML
	 */
	public static void convertToXML(File inputFile, File outputXML) {
		try {
			BufferedReader reader  = new BufferedReader(new FileReader(inputFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputXML));
			String str;
			writer.write("<ROOT>\n");
			int qid = -1;
			int rank = -1;
			float score = -1;
			while((str=reader.readLine())!=null) {
				
				str = str.replaceAll("&Ggr;", "");
				str = str.replaceAll("&rsqb;", "");
				str = str.replaceAll("& ", "&amp; ");
				str = str.replaceAll("&$", "&amp;");
				str = str.replaceAll("&plus;", "+");
				str = str.replaceAll("&yen;", " Yen ");
				str = str.replaceAll("&pound;", " Pound ");
				str = str.replaceAll("&equals;", "=");
				str = str.replaceAll("&lsqb;", "");
				str = str.trim();
				
				if(str.isEmpty())
					continue;
				
				if(str.length()>=3 && str.substring(0, 3).equalsIgnoreCase(QID)) {
					String[] split = str.split("\t");
					for (String string : split) {
						String[] split2 = string.split(": ");
						if(split2[0].equalsIgnoreCase(QID))
							qid = Integer.parseInt(split2[1]);
						else if(split2[0].equalsIgnoreCase(RANK))
							rank = Integer.parseInt(split2[1]);
						else if(split2[0].equalsIgnoreCase(SCORE))
							score = Float.parseFloat(split2[1]);
					}
				}
				else {
					if(str.startsWith("<DOC>")) {
						writer.write("<DOC "+
								QID + "=\"" + qid + "\" " +
								RANK + "=\"" + rank + "\" " +
								SCORE + "=\"" + score + "\"" +
								">" + "\n");
					}
					else {
						str = str.replaceAll("<(\\w+) (\\w+)=([\\w\\-]+)>", "<$1 $2=\"$3\">");
						writer.write(str + "\n");
					}
				}
			}
			writer.write("</ROOT>");
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * parses the xmlFile and returns the list of documents in ranked order.
	 * @param xmlFile
	 * @return
	 */
	public static ArrayList<Document> parse(File xmlFile) {
		try {
			XMLTrec obj = new XMLTrec();
			XMLReader xmlr = XMLReaderFactory.createXMLReader();
			xmlr.setContentHandler(obj);
			xmlr.setErrorHandler(obj);

			System.out.println("starting parsing through file " + xmlFile);
			// Parse the intermediate XML file.
			xmlr.parse(new InputSource(new FileReader(xmlFile)));
			return obj.documents;
			
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	ArrayList<Document> documents;
	Document doc;
	String startEle;
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);

		if(qName.equalsIgnoreCase("ROOT"))
			documents = new ArrayList<Document>();
		else if(qName.equalsIgnoreCase(DOC)) {
			Integer qid = Integer.parseInt(attributes.getValue(QID));
			Integer rank = Integer.parseInt(attributes.getValue(RANK));
			Float score = Float.parseFloat(attributes.getValue(SCORE));
			doc = new Document(qid, rank, score);
		}
		else 
			startEle = qName;
	}

	@Override
	public void characters(char[] ch, int start, int length)
	throws SAXException {
		String line = new String(ch, start, length);
		if(line.isEmpty() || line.equals("\n")) 
			return;
		
		doc.addString(startEle, line);
	}

	@Override
	public void endElement(String uri, String localName, String qName)
	throws SAXException {

		super.endElement(uri, localName, qName);
		if(qName.equalsIgnoreCase(DOC)) {
			documents.add(doc);
		}
	}
}
