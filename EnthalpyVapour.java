public class EnthalpyVapour extends Correlation {
  
  private static final double R = 8.314;
  
  public EnthalpyVapour() {
   super(); 
  }
  
  public EnthalpyVapour(double[] C, double minX, double maxX, int form) {
    super(C, minX, maxX, form);
  }
  
  public double evaluate(double[] x) {
    
    double T = x[0];
    double Tb = x[1];
    double hL = x[2];
    double lambda = x[3];
    double[] C = super.getC();
    
    double Hv = this.R * ((C[0] * (T - Tb)) 
                       + (0.5 * C[1] * (Math.pow(T, 2) - Math.pow(Tb, 2))) 
                       + ((1/3) * C[2] * (Math.pow(T, 3) - Math.pow(Tb, 3)))
                       + (-1 * C[3] * (Math.pow(T, -1) - Math.pow(Tb, -1))));
    
    return hL + lambda + Hv;
  }
  
  public int getConstantCount() {
    return 4;
  }
  
}