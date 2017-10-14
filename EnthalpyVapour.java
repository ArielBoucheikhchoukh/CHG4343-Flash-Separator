public class EnthalpyVapour extends Correlation {
 
  private static final double R = 8.314;
  
  public EnthalpyVapour(double[] C, double minX, double maxX) {
    super(C, minX, maxX);
  }
  
  public double evaluate(double T, double Tb, double hL, double lambda) {
    double Hv = this.R * ((super.C[0] * (T - Tb)) 
                       + (0.5 * super.C[1] * (Math.pow(T, 2) - Math.pow(Tb, 2))) 
                       + ((1/3) * super.C[2] * (Math.pow(T, 3) - Math.pow(Tb, 3)))
                       + (-1 * super.C[3] * (Math.pow(T, -1) - Math.pow(Tb, -1))));
    
    return hL + lambda + Hv;
  }
}