/* Evaluates the vapour pressure of a species.  
 * T = [K]
 * Psat = [Pa]
 */

public class VapourPressure extends Correlation {
  
  public static final int constantCount = 5;
  
  public VapourPressure() {
    super();
  }
  
  public VapourPressure(double[] C, double minX, double maxX, int form) {
    super(C, minX, maxX, form);
  }
  
  protected double evaluateWithinBounds(double x, double[] constants) throws FunctionException, NumericalMethodException {
    double T = x;
    double[] C = super.getC();
    
    return Math.pow(Math.E, C[0] + C[1] / T + C[2] * Math.log(T) + C[3] * Math.pow(T, C[4])); 
  }
  
  protected double evaluateDerivativeWithinBounds(double x, double[] constants) throws FunctionException, NumericalMethodException {
    double T = x;
    double[] C = super.getC();
    
    return (-C[1]/Math.pow(T, 2) + C[2]/T + C[3]*C[4]*Math.pow(T, C[4]-1)) 
      * Math.pow(Math.E, C[0] + C[1] / T + C[2] * Math.log(T) + C[3] * Math.pow(T, C[4])); 
  }
  
  public int getConstantCount() {
    return VapourPressure.constantCount;
  }
  
}