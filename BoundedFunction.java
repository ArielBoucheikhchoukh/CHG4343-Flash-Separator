import java.util.Scanner;

public abstract class BoundedFunction implements Function {
  
  private String id;
  private double minX;
  private double maxX;
  
  
  /**********************************************************************************************************************
    * 1) Constructor
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public BoundedFunction(String id, double minX, double maxX) {
    this.id = id;
    this.minX = minX;
    this.maxX = maxX;
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 2) Copy Constructor
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public BoundedFunction(BoundedFunction source) {
    this.id = source.id;
    this.minX = source.minX;
    this.maxX = source.maxX;
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 3) clone()
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public abstract BoundedFunction clone();
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 4) evaluate()
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public double evaluate(double x, double[] constants) throws FunctionException {
    
    if (Double.isNaN(x) || Double.isInfinite(x)) {
      throw new IllegalArgumentException(this.id);
    }
    
    double y = this.evaluateWithinBounds(x, constants);
    
    if (Double.isNaN(y) || Double.isInfinite(y)) {
      throw new UndefinedFunctionException(this.id, this, x);
    }
    
    if (x < this.minX || x > this.maxX) {
      Menu.appendToMessages("\r\nWarning: " + this.id 
                              + " was evaluated outside of the function bounds.");
    }
    
    return y;
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 5) evaluateDerivative()
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public double evaluateDerivative(double x, double[] constants) throws FunctionException {
    
    if (Double.isNaN(x) || Double.isInfinite(x)) {
      throw new IllegalArgumentException(this.id);
    }
    
    double y = this.evaluateDerivativeWithinBounds(x, constants);
    
    if (Double.isNaN(y)) {
      throw new UndefinedFunctionException(this.id, this, x);
    }
    
    if (x < this.minX || x > this.maxX) {
      Menu.appendToMessages("\r\nWarning: " + this.id 
                              + " was evaluated outside of the function bounds.");
    }
    
    return y;
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 6) evaluateWithinBounds()
    * ---------------------------------------------------------------------------------------------------------------------
    */
  protected abstract double evaluateWithinBounds(double x, double[] constants) 
    throws FunctionException;
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 7) evaluateDerivativeWithinBounds()
    * ---------------------------------------------------------------------------------------------------------------------
    */
  protected abstract double evaluateDerivativeWithinBounds(double x, double[] constants) 
    throws FunctionException;
  /*********************************************************************************************************************/
  
  
  public String getID() {
    return this.id;
  }
  
  public void setID(String id) {
    this.id = id;
  }
  
  public double getMinX() {
    return this.minX;
  }
  
  public void setMinX(double minX) {
    this.minX = minX;
  }
  
  public double getMaxX() {
    return this.maxX;
  }
  
  public void setMaxX(double maxX) {
    this.maxX = maxX;
  }
  
}