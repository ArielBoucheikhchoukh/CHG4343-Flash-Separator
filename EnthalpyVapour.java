/* Evaluates the enthalpy of a species in the vapour phase.  
 * T = [K]
 * H = [J/mol*K]
 */

public class EnthalpyVapour extends Correlation {
  
  public static final int constantCount = 4;
  private static final double R = 8.314;
  
  public EnthalpyVapour() {
   super(); 
  }
  
  public EnthalpyVapour(double[] C, double minX, double maxX, int form) {
    super(C, minX, maxX, form);
  }
  
  protected double evaluateWithinBounds(double x, double[] constants)  throws FunctionException, NumericalMethodException {
    
    double T = x;
    double Tb = constants[0];
    double hL = constants[1];
    double lambda = constants[2];
    double[] C = super.getC();
    
    double Hv = this.R * ((C[0] * (T - Tb)) 
                       + (0.5 * C[1] * (Math.pow(T, 2) - Math.pow(Tb, 2))) 
                       + ((1./3.) * C[2] * (Math.pow(T, 3) - Math.pow(Tb, 3)))
                       + (-1. * C[3] * (Math.pow(T, -1) - Math.pow(Tb, -1))));
    
    return hL + lambda + Hv;
  }
  
  protected double evaluateDerivativeWithinBounds(double x, double[] constants) throws FunctionException, NumericalMethodException {
     double T = x;
     double Tb = constants[0];
     double dhLdT = constants[1];
     double[] C = super.getC();
     
     double dHvdT = this.R 
       * (C[0] 
            + C[1] * T
            + C[2] * Math.pow(T, 2) 
            + C[3] * Math.pow(T, -2));
     
     return dhLdT + dHvdT;
  }
  
  public int getConstantCount() {
    return EnthalpyVapour.constantCount;
  }
  
}