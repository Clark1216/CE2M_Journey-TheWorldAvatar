/*
 * 
 */
package uk.ac.cam.ceb.como.compchem.xslt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import uk.ac.cam.ceb.como.jaxb.parsing.utils.FileUtility;
import uk.ac.cam.ceb.como.jaxb.parsing.utils.Utility;

/**
 * 
 * The Class Transformation.
 *
 * @author <p>nk510 This class implements methods for xslt transformations from
 *         ontochem XML files to RDF graph as Abox assertions of CoMo ontochem
 *         ontology ver 0.1.</p>
 *         
 */ 

public class Transformation {

	/**
	 * The folder path.
	 *
	 * @author nk510 <p>Folder path where generated Compchem XML files are saved.</p>
	 * 
	 */

	static String xmlFolderPath = "src/test/resources/ontochem_xml/";

	/**
	 * The xslt path.
	 *
	 * @author nk510 Path to XSLT file.
	 */

	static String xsltPath = "src/test/resources/xslt/ontochem_rdf.xsl";

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws TransformerException the transformer exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	
	public static void main(String[] args) throws TransformerException, IOException {

		Utility utility = new FileUtility();
		
		/**
		 * Gets the file list.
		 *
		 * @author nk510
		 * @param folderPath <p>File list of all ontochem xml files stored in folder path.</p>
		 * @return <p>Method reads all ontochem XML files in given folder path. Supported
		 *         file extension is '.xml'.</p>
		 */

		File[] fileList = utility.getFileList(xmlFolderPath,".xml");

		/**
		 * 
		 * @author nk510 <p>Iterates over file list given in folder 'src/test/resources/ontochem_xml/' </p>
		 * 
		 */
        
		for (File f : fileList) {

			InputStream xmlSource = new FileInputStream(f.getPath());

			StreamSource xsltSource = new StreamSource(xsltPath);

			/**
			 * 
			 * @author nk510 <p>Creates output path for each compchem XML file from file list. Generated files have rdf file extension.</p>
			 * 
			 */

			String outputPath = "src/test/resources/ontology/ontochem_abox/" + f.getName().replace(".xml", "").toString() + ".rdf";

			FileOutputStream outputStream = new FileOutputStream(new File(outputPath));

			/**
			 * @author nk510
			 * @param randomStr is used to create an IRI as an instance of 'ontochem:G09' class.
			 */
			String randomStr= UUID.randomUUID().toString();

			/**
			 * @author nk510 <p>Runs XSLT transformation for each ontochem XML file form file
			 *         list.</p> 
			 */			
			trasnformation(randomStr,xmlSource, outputStream, xsltSource);
		}
	}

	/**
	 * Trasnformation.
	 *
	 * @author nk510
	 * @param XmlSource            is a source ontochem XML file generated by parsing Gaussian files
	 *            (g09)
	 * @param outputStream            is RDF file generated by using XSLT transformations.
	 * @param xsltSource            is file that contains the implementation of transformations
	 *            (business logic) from ontochem xml file to RDF file.
	 * @throws TransformerException             <p>This method implements transformation from ontochem XML files to
	 *             RDF files by using Java 8 @see <a href=
	 *             "https://docs.oracle.com/javase/7/docs/api/javax/xml/transform/TransformerFactory.html">TransformerFactory</a>
	 *             class.</p>
	 */

	public static void trasnformation(String xmlFolderName, InputStream XmlSource, FileOutputStream outputStream, StreamSource xsltSource)
			throws TransformerException {
		
		/**
		 * In case of using SaxonHE parser, we need to set/add the following System property:
		 * System.setProperty("javax.xml.transform.TransformerFactory","net.sf.saxon.TransformerFactoryImpl");
		 */
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();	
		Transformer transformer = transformerFactory.newTransformer(xsltSource);
		
		transformer.setParameter("xmlFolderName", xmlFolderName);
		
		transformer.transform(new StreamSource(XmlSource), new StreamResult(outputStream));
	
	}	
}