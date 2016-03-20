/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.cam.ceb.como.math.data.fitting;

import java.util.List;
import uk.ac.cam.ceb.como.math.function.Function;
import uk.ac.cam.ceb.como.math.function.FunctionCalculationException;

/**
 * 
 * @author pb556
 */
public class LinearLeastSquaresFitting extends Function<Double[], Double[]>{

    // http://mathworld.wolfram.com/LeastSquaresFitting.html
    private double[] a = null;
    private double[] b = null;
    
    public LinearLeastSquaresFitting(List<Double[]> xData, List<Double[]> yData) throws FittingException {
        fit(xData, yData);
    }
    
    public double[] getCoefficientA() {
        return a;
    }
    
    public double[] getCoefficientB() {
        return b;
    }
    
    // http://mathworld.wolfram.com/LeastSquaresFittingExponential.html
    protected final void fit(List<Double[]> xData, List<Double[]> yData) throws FittingException {
        int dim = -1;
        if (xData.size() != yData.size()) {
            throw new FittingException("Inconsistent dimensionelity.");
        }
        for (Double[] d : xData) {
            if (dim == -1) {
                dim = d.length;
            } else if (dim != d.length) {
                throw new FittingException("Inconsistent dimensionelity.");
            }
        }
        for (Double[] d : yData) {
            if (dim == -1) {
                dim = d.length;
            } else if (dim != d.length) {
                throw new FittingException("Inconsistent dimensionelity.");
            }
        }
        
        a = new double[dim];
        b = new double[dim];
        
        for (int j = 0; j < dim; j++) {
            
            // iteration and calculation of the individual terms
            double termNominator_a1_1 = 0.0;
            double termNominator_a1_2 = 0.0;
            double termNominator_a1;
            
            double termNominator_a2_1 = 0.0;
            double termNominator_a2_2 = 0.0;
            double termNominator_a2;
            
            double termDenominator_1 = 0.0;
            double termDenominator_2 = 0.0;
            
            double termNominator_b1 = 0.0;
            
            double termNominator_b2_1 = 0.0;
            double termNominator_b2_2 = 0.0;
            double termNominator_b2;
            
            for (int i = 0; i < xData.size(); i++) {
                termNominator_a1_1 += yData.get(i)[j];
                termNominator_a1_2 += xData.get(i)[j] * xData.get(i)[j];
                
                termNominator_a2_1 += xData.get(i)[j];
                termNominator_a2_2 += xData.get(i)[j] * yData.get(i)[j];

                termNominator_b1 += xData.get(i)[j] * yData.get(i)[j];

                termNominator_b2_1 += xData.get(i)[j];
                termNominator_b2_2 += yData.get(i)[j];
                
                termDenominator_1 += xData.get(i)[j] * xData.get(i)[j];
                termDenominator_2 += xData.get(i)[j];
            }
            
            termNominator_a1 = termNominator_a1_1 * termNominator_a1_2;
            termNominator_a2 = termNominator_a2_1 * termNominator_a2_2;
            
            termNominator_b1 = xData.size() * termNominator_b1;
            termNominator_b2 = termNominator_b2_1 * termNominator_b2_2; 
            
            double termDenominator = xData.size() * termDenominator_1 - termDenominator_2 * termDenominator_2;
            
            a[j] = (termNominator_a1 - termNominator_a2) / (termDenominator);
            b[j] = (termNominator_b1 - termNominator_b2) / (termDenominator);
        }
    }
    
    @Override
    public Double[] f(Double[] x, Object... additionalData) throws FunctionCalculationException {
        
        Double[] y = new Double[x.length];
        // check dimensionality before calculating the data
        if (x.length != a.length) {
            throw new FunctionCalculationException("Inconsistent dimensionelity."); 
        }
        
        for (int i = 0; i < x.length; i++) {
            y[i] = a[i] + (b[i] * x[i]);
        }
        
        return y;
    }
    
}
