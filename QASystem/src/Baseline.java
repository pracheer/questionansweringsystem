import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.xml.sax.helpers.DefaultHandler;

import lemurproject.indri.IndexEnvironment;
import lemurproject.indri.ParsedDocument;
import lemurproject.indri.QueryEnvironment;
import lemurproject.indri.ScoredExtentResult;


public class Baseline extends DefaultHandler{

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			File dir = new File("D:/study/natural language processing/Assignments/QASystem/DATA_201-399/topdocs_201-399/top_docs");
			File[] files = dir.listFiles();
			HashMap<Integer, ArrayList<Document>> map = new HashMap<Integer, ArrayList<Document>>();
			for (File inputFile : files) {
				File xmlFile = new File(inputFile.getParentFile(), inputFile.getName()+".xml");
				XMLTrec.convertToXML(inputFile, xmlFile);
				ArrayList<Document> docs = XMLTrec.parse(xmlFile);
				map.put(docs.get(0).qid, docs);
			}
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
