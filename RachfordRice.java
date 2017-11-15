public class RachfordRice extends BoundedFunction {
 
  private double[] z;
  private double[] K;
  
  public RachfordRice(double[] z, double[] K) {
    super(0., 1.);
    this.z = z.clone();
    this.K = K.clone();
  }
  
  public double evaluateWithinBounds(double x, double[] constants) {
    
    double f = 0.;
    
    for (int i = 0; i < this.z.length; i++) {
      f += (this.z[i] * (this.K[i] - 1)) / (1 + (this.K[i] - 1) * x); 
    }
    
    return f;
  }
  
  public double evaluateDerivativeWithinBounds(double x, double[] constants) {
    
    double df = 0.;
    
    for (int i = 0; i < this.z.length; i++) {
      df -= this.z[i] * Math.pow((this.K[i] - 1) / (1 + (this.K[i] - 1) * x), 2); 
    }
    
    return df;
  }
  
}