public abstract class BoundedFunction implements Function {
  
  private double minX;
  private double maxX;
  
  public BoundedFunction(double minX, double maxX) {
    this.minX = minX;
    this.maxX = maxX;
  }
  
  public double evaluate(double x, double[] constants) throws FunctionException {
    if (x < this.minX || x > this.maxX) {
      throw new OutOfFunctionBoundsException();
    }
    
    //System.out.println("Test - BoundedFunction Class - evaluate() Method");
    return this.evaluateWithinBounds(x, constants);
  }
  
  public double evaluateDerivative(double x, double[] constants) throws FunctionException {
    if (x < this.minX || x > this.maxX) {
     throw new OutOfFunctionBoundsException();
    }
    
    //System.out.println("Test - BoundedFunction Class - evaluateDerivative() Method");
    return this.evaluateDerivativeWithinBounds(x, constants);
  }
  
  protected abstract double evaluateWithinBounds(double x, double[] constants) throws FunctionException;
  
  protected abstract double evaluateDerivativeWithinBounds(double x, double[] constants) throws FunctionException;
  
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