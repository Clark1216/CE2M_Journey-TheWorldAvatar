/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.cam.ceb.como.enthalpy.estimation.balanced_reaction.reaction.selector;

import uk.ac.cam.ceb.como.enthalpy.estimation.balanced_reaction.reaction.ReactionList;
import uk.ac.cam.ceb.como.math.variability.Variance;

/**
 *
 * @author pb556
 */
public class ReactionListValidation {
    
    protected ReactionList list = null;
    
    public ReactionListValidation(ReactionList list) {
        this.list = list;
    }
    
    public boolean isValidDistribution(double threshold) throws Exception {
        Variance var = new Variance();
        double[] values = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            values[i] = list.get(i).calculateHf();
        }
        double varSol = var.calculate(values);
        System.out.println(varSol);
        return varSol <= threshold;
    }
}
