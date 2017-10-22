public class VapourPressure extends Correlation {
  
  public VapourPressure() {
    super();
  }
  
  public VapourPressure(double[] C, double minX, double maxX, int form) {
    super(C, minX, maxX, form);
  }
  
  public double evaluate(double[] x) {
    double T = x[0];
    double[] C = super.getC();
    
    return (Math.pow(Math.E, C[0] + C[1] / T + C[2] * Math.log(T) + C[3] * Math.pow(T, C[4]))) / 10000.; 
  }
  
  public int getConstantCount() {
    return 5;
  }
  
}