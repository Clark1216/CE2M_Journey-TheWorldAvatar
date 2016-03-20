/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.cam.ceb.como.math.fourier.series.examples;

/**
 *
 * @author pb556
 */
public class ExampleFourierSeries1PlusX extends ExampleFourierSeries {

    public ExampleFourierSeries1PlusX() {
        super();
        lower = -Math.PI;
        upper = Math.PI;
        T = upper - lower;
    }
    
    @Override
    public double f(double x) {
        x = checkValue(x);
        return 1.0 + x;
    }

    @Override
    public double a(int index) throws ExampleFourierSeriesOutOfValueRangeException {
        if (index < 0) {
            throw new ExampleFourierSeriesOutOfValueRangeException("Index cannot be negative.");
        }
        if (index == 0) {
            return 2.0;
        }
        return 0.0;
    }

    @Override
    public double b(int index) throws ExampleFourierSeriesOutOfValueRangeException {
        if (index < 0) {
            throw new ExampleFourierSeriesOutOfValueRangeException("Index cannot be negative.");
        }
        double dIndex = (double) index;
        return (2.0*(Math.pow(-1, index + 1))) / (dIndex);
    }
}
