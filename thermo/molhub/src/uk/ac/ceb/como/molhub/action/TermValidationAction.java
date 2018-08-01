package uk.ac.ceb.como.molhub.action;

import java.util.HashMap;

import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import com.opensymphony.xwork2.ActionSupport;

import aima.core.logic.propositional.inference.DPLL;
import aima.core.logic.propositional.inference.DPLLSatisfiable;
import aima.core.logic.propositional.kb.data.Clause;
import aima.core.logic.propositional.kb.data.Literal;
import aima.core.logic.propositional.parsing.PLParser;
import aima.core.logic.propositional.parsing.ast.PropositionSymbol;
import aima.core.logic.propositional.parsing.ast.Sentence;

import uk.ac.cam.ceb.como.chem.periodictable.Element;
import uk.ac.cam.ceb.como.chem.periodictable.PeriodicTable;
import uk.ac.cam.ceb.como.io.chem.file.parser.formula.EmpiricalFormulaParser;
import uk.ac.ceb.como.molhub.bean.MoleculeProperty;
import uk.ac.ceb.como.molhub.bean.Term;
import uk.ac.ceb.como.molhub.controler.ConnectionToTripleStore;
import uk.ac.ceb.como.molhub.model.QueryManager;

import uk.ac.ceb.como.molhub.model.SentenceManager;

// TODO: Auto-generated Javadoc
/**
 * The Class TermValidationAction.
 */
public class TermValidationAction extends ActionSupport {

	private static final Logger log = Logger.getLogger(TermValidationAction.class);

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1222255700658500383L;

	/** The term. */
	private Term term;

	/** The formula. */
	private String formula;

	/** The satisfiable. */
	private boolean satisfiable;

	/** The periodic table element. */
	private String periodicTableElement;

	/** The server url. */
	private String serverUrl = "http://localhost:8080/rdf4j-server/";

	Map<String, MoleculeProperty> finalSearchResultMap = new HashMap<String, MoleculeProperty>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.xwork2.ActionSupport#execute()
	 */
	@Override
	public String execute() throws Exception {

		PLParser parser = new PLParser();

		DPLL dpll = new DPLLSatisfiable();

		EmpiricalFormulaParser empiricalFormulaParser = new EmpiricalFormulaParser();

		try {

			Sentence sentence = parser.parse(getSearchTerm(term));

			/**
			 * @author nk510 Gets a set of all clauses.
			 */
			Set<Clause> clauseSet = SentenceManager.getClauseSet(sentence);

			/**
			 * 
			 * @author nk510 Iterates over set of clauses. Validation of query string: 1.
			 *         Checks whether each clause starts with one or more letter and ends
			 *         with one or more digit. 2. Removes numbers at the end of each clause
			 *         and checks whether the literal belongs to periodic table.
			 * 
			 */

			for (Clause c : clauseSet) {

				Set<PropositionSymbol> ps = c.getSymbols();

				for (PropositionSymbol ppSymbol : ps) {
					/**
					 * 
					 * @author nk510 Extracts each propositional letter (propositional symbol) in
					 *         each clause and checks whether that symbol is member of periodic
					 *         table. To check whether propositional symbol belongs to period table
					 *         we use <b>{@author pb556}</b> parser.
					 * 
					 */

					Element elementSymbol = PeriodicTable
							.getElementBySymbol(empiricalFormulaParser.getAtomName(ppSymbol.getSymbol().toString()));

					log.info("ppSymbol.getSymbol() : " + ppSymbol.getSymbol());

					if (elementSymbol.getSymbol() == null) {

						addFieldError("term.name", "There is at least one propositional letter (" + ppSymbol.getSymbol()
								+ ") that is not member of periodic table.");

						return ERROR;

					}
				}

			}

			/**
			 * 
			 * @author nk510 Checks whether input propositional sentence (query string) is
			 *         satisfiable. It si checked by using Davis�Putnam�Logemann�Loveland
			 *         (DPLL) procedure.
			 * 
			 */
			setSatisfiable(dpll.dpllSatisfiable(sentence));

			if (dpll.dpllSatisfiable(sentence)) {

				setFormula(getSearchTerm(term));

				return SUCCESS;

			} else {

				addFieldError("term.name", "Query string is not Davis�Putnam�Logemann�Loveland (DPLL) satisfiable.");

				return ERROR;
			}

		} catch (Exception e) {

			/**
			 * 
			 * @author nk510
			 * 
			 *         Checks whether input query string is propositionally valid. For
			 *         example "(P and not P" is not propositionally valid statement.
			 * 
			 */

			addFieldError("term.name", "Query string is not propositionally valid sentence. Please try again.");

			return ERROR;
		}

	}

	public Map<String, MoleculeProperty> getFinalSearchResultMap() {
		return finalSearchResultMap;
	}

	public void setFinalSearchResultMap(Map<String, MoleculeProperty> finalSearchResultMap) {
		this.finalSearchResultMap = finalSearchResultMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.xwork2.ActionSupport#validate()
	 */
	public void validate() {

		/**
		 * 
		 * @author nk510 Checks whether input query string is empty.
		 * 
		 */
		if (term.getName().length() == 0) {

			addFieldError("term.name", "Query string is empty.");
		}
	}

	/**
	 * Gets the term.
	 *
	 * @return the term
	 */
	public Term getTerm() {

		return term;

	}

	/**
	 * Sets the term.
	 *
	 * @param term
	 *            the new term
	 */
	public void setTerm(Term term) {

		this.term = term;

	}

	/**
	 * Gets the formula.
	 *
	 * @return the formula
	 */
	public String getFormula() {
		return formula;
	}

	/**
	 * Sets the formula.
	 *
	 * @param formula
	 *            the new formula
	 */
	public void setFormula(String formula) {

		this.formula = formula;
	}

	/**
	 * Checks if is satisfiable.
	 *
	 * @return true, if is satisfiable
	 */
	public boolean isSatisfiable() {
		return satisfiable;
	}

	/**
	 * Sets the satisfiable.
	 *
	 * @param satisfiable
	 *            the new satisfiable
	 */
	public void setSatisfiable(boolean satisfiable) {
		this.satisfiable = satisfiable;
	}

	/**
	 * Gets the search term.
	 *
	 * @param term
	 *            the term
	 * @return the search term
	 */
	public String getSearchTerm(Term term) {

		String formula = "";

		/**
		 * @author nk510 Converts all letter into lower case.
		 */

		formula = term.getName().replaceAll("and", "&");
		formula = formula.replaceAll("or", "|");
		formula = formula.replaceAll("not", "~");
		formula = formula.replaceAll("implies", "=>");
		formula = formula.replaceAll("euqal", "<=>");

		return formula;
	}

	/**
	 * Gets the periodic table element.
	 *
	 * @return the periodic table element
	 */
	public String getPeriodicTableElement() {
		return periodicTableElement;
	}

	/**
	 * Sets the periodic table element.
	 *
	 * @param periodicTableElement
	 *            the new periodic table element
	 */
	public void setPeriodicTableElement(String periodicTableElement) {
		this.periodicTableElement = periodicTableElement;
	}

}