/*Validation of the RootFinder class.
 *Function obtained from Chapter 5.1 of Numerical Methods for Engineers, sixth ed.
 *Root at x = 14.78.
 *Asymptote at x = 0.*/

public class ValidationFunction extends BoundedFunction {
  
  public ValidationFunction(double minX, double maxX) {
    super(minX, maxX);
  }
  
  protected double evaluateWithinBounds(double x) {
    return (667.38/x) * (1 - Math.pow(Math.E, -0.146843 * x)) - 40;
  }
  
  protected double evaluateDerivativeWithinBounds(double x) {
    return (667.38/x) * (0.146843 * Math.pow(Math.E, -0.146843 * x)) 
      + (-667.38/Math.pow(x, 2)) * (1 - Math.pow(Math.E, -0.146843 * x));
  }
  
}