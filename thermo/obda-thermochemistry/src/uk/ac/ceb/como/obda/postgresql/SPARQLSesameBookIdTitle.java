package uk.ac.ceb.como.obda.postgresql;
import it.unibz.inf.ontop.sesame.RepositoryConnection;

import it.unibz.inf.ontop.sesame.SesameVirtualRepo;

import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.resultio.text.csv.SPARQLResultsCSVWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

/**
 * 
 * @author NK510 (caresssd@hermes.cam.ac.uk)
 * 
 *         This class demonstrate how to perform SPARQL query on book database
 *         visa Book ontology using ontop Sesame virtual repository api and
 *         openrdf api.
 *         
 *         This example demonstrates how to access data in 'books' Postgresql database via exampleBooks ontology.
 *
 */
public class SPARQLSesameBookIdTitle {

	/**
	 * Book ontology
	 */
	final String owlFile = "./resources/books/exampleBooks.owl";
	/**
	 * Mapping between exampleBooks ontology and books database stored in Postgresql.
	 */
	final String obdaFile = "./resources/postgresql/bk_code_bk_title.obda";

	/**
	 * SPARQL query performed on Postgresql book database via exampleBooks ontology.
	 */
	final String sparqlFile = "./resources/books/book_id_title.rq";

	private BufferedReader br;

	public static void main(String[] args) {

		try {

			SPARQLSesameBookIdTitle example = new SPARQLSesameBookIdTitle();

			example.runSPARQL();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Performs SPARQL query on Book database via Book ontology and returns as
	 * result book id and its title.
	 */
	public void runSPARQL() throws Exception {

		String queryString = readSPARQL(sparqlFile);

		/**
		 * Create and initialize repository
		 */
		boolean existential = false;

		/**
		 * A type of rewriting technique.
		 * 
		 * Reference paper that explains tree-withness rewriting technique is
		 * 
		 * "R. Kontchakov, M. Rodriguez-Muro and M. Zakharyaschev. Ontology-Based Data
		 * Access with Databases: A Short Course. In S. Rudolph, G. Gottlob, I.
		 * Horrocks, F. van Harmelen, editors, Summer School on Reasoning Web (Mannheim,
		 * 30 July - 2 August), vol. 8067 of LNCS, pp. 194-229. Springer, 2013."
		 */
		String rewriting = "TreeWitness";

		@SuppressWarnings("resource")
		SesameVirtualRepo repo = new SesameVirtualRepo("test_repo", owlFile, obdaFile, existential, rewriting);

		repo.initialize();

		try (RepositoryConnection conn = repo.getConnection()) {

			/**
			 * Execute SPARQL query
			 */
			Query query = conn.prepareQuery(QueryLanguage.SPARQL, queryString);

			TupleQuery tq = (TupleQuery) query;

			FileOutputStream resultingFile = new FileOutputStream(
					new File("./resources/sparql_sesame_result_postgresql_book_id_title.txt"));

			TupleQueryResultHandler writer = new SPARQLResultsCSVWriter(resultingFile);

			/**
			 * Stores SPARQL results into target file.
			 */

			tq.evaluate(writer);
		}

		repo.shutDown();

	}

	private String readSPARQL(String sparqlFile) throws IOException {

		String queryString = "";

		br = new BufferedReader(new FileReader(sparqlFile));

		String line;

		while ((line = br.readLine()) != null) {
			queryString += line + "\n";
		}

		return queryString;
	}
}