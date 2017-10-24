public abstract class BoundedFunction implements Function {
  
  private double minX;
  private double maxX;
  
  public BoundedFunction(double minX, double maxX) {
    this.minX = minX;
    this.maxX = maxX;
  }
  
  public double evaluate(double x) throws OutOfFunctionBoundsException {
    if (x < this.minX || x > this.maxX) {
      throw new OutOfFunctionBoundsException();
    }
    return this.evaluateWithinBounds(x);
  }
  
  public double evaluateDerivative(double x) throws OutOfFunctionBoundsException {
    if (x < this.minX || x > this.maxX) {
     throw new OutOfFunctionBoundsException();
    }
    return this.evaluateDerivativeWithinBounds(x);
  }
  
  protected abstract double evaluateWithinBounds(double x);
  
  protected abstract double evaluateDerivativeWithinBounds(double x);
  
  protected double getMinX() {
   return this.minX; 
  }
  
  protected void setMinX(double minX) {
   this.minX = minX; 
  }
  
  protected double getMaxX() {
   return this.maxX; 
  }
  
  protected void setMaxX(double maxX) {
   this.maxX = maxX; 
  }
  
}