public class VapourPressure extends Correlation {
 
  public VapourPressure(double[] C, double minX, double maxX) {
    super(C, minX, maxX);
  }
  
  public double evaluate(double T) {
   return (Math.pow(Math.E, super.C[0] + super.C[1] / T + super.C[2] * Math.log(T) + super.C[3] * Math.pow(T, super.C[4]))) / 10000.; 
  }
  
}