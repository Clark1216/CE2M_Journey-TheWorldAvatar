package uk.ac.cam.ceb.como.jaxb.parser.g09;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.xmlcml.cml.element.CMLParameter;

import uk.ac.cam.ceb.como.compchem.CompChem;
import uk.ac.cam.ceb.como.io.chem.file.jaxb.Parameter;
import uk.ac.cam.ceb.como.io.chem.file.jaxb.Scalar;
import uk.ac.cam.ceb.como.io.chem.file.parser.compchem.CompChemParser;
import uk.ac.cam.ceb.como.io.chem.file.parser.g09.FrequencyParser;
import uk.ac.cam.ceb.como.thermo.calculator.rotation.internal.util.IRCompChemWrapper;

public class ParsingBasisSet {

	public static String getBasisSetString(File f) throws IOException {

		String basisSetString = "";

		/**
		 * 
		 * @author nk510
		 *         <p>
		 *         try block works under JavaSE 1.7
		 *         </p>
		 * 
		 */

		BufferedReader br = new BufferedReader(new FileReader(f));

		for (String line; (line = br.readLine()) != null;) {

			if (line.contains("#p ")) {

				/**
				 * 
				 * @author nk510
				 *         <p>
				 *         Returns substring that starts with "/" string and ends with first
				 *         appearing " " character.
				 *         </p>
				 * 
				 */

				/**
				 * Split string that starts with '#p ' on two substrings before and after
				 * charatcter '/'.
				 */

				String splitOne[] = line.split("/");
				line = splitOne[1];

				/**
				 * Split second substring into two strings before white space and after white
				 * space.
				 */

				String splitTwo[] = line.split(" ");

				basisSetString = basisSetString + splitTwo[0];

				System.out.println(basisSetString);

				break;
			}
		}

		br.close();

		return basisSetString;

	}

	/**
	 * 
	 * @param f
	 * @param numberOfAtoms
	 * @return Jaxb Parameter instance that contains information about basis set value.
	 * @throws Exception
	 */
	public static Parameter getBasisSetParameter(File f, int numberOfAtoms) throws Exception {

		Parameter parameter = new Parameter();
		Scalar scalar = new Scalar();

		if (numberOfAtoms > 1) {

			FrequencyParser parser = new FrequencyParser();
			parser.set(f);
			parser.parse();

			CompChem cc = (CompChem) parser.get();

			CompChemParser ccp = new CompChemParser();

			ccp.parse(cc);

			IRCompChemWrapper irccw = new IRCompChemWrapper(cc);

			List<CMLParameter> cmlP = irccw.getParameters();

			for (CMLParameter p : cmlP) {

				/**
				 *
				 * Finds basis set when attribute dictRef has value that is equal to 'cc:basis'
				 *
				 */
				if (p.getDictRef().equals("cc:basis")) {

					parameter.setDictRef(p.getDictRef());
					scalar.setValue(p.getValue());
					scalar.setDataType("xsd:string");

					parameter.getScalarOrArrayOrMatrix().add(scalar);
				}
			}

		} else {
			
			parameter.setDictRef("cc:basis");
			scalar.setValue(getBasisSetString(f));
			scalar.setDataType("xsd:string");

			parameter.getScalarOrArrayOrMatrix().add(scalar);
		}

		return parameter;
	}

}