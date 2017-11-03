import java.lang.CloneNotSupportedException;

public abstract class Correlation extends BoundedFunction implements Cloneable {
  
  private double[] C;
  private int form;
  
  public Correlation() {
   super(0., 1.);
   this.C = null;
   this.form = 0;
  }
  
  public Correlation(double minX, double maxX) {
    super(minX, maxX);
    this.C = null;
    this.form = 0;
  }
  
  public Correlation(double[] C, double minX, double maxX, int form) {
    super(minX, maxX);
    this.C = C.clone();
    this.form = form;
  }
  
  public abstract double evaluateWithinBounds(double x, double[] constants) throws FunctionException, NumericalMethodException;
  
  public abstract double evaluateDerivativeWithinBounds(double x, double[] constants) throws FunctionException, NumericalMethodException;
  
  public abstract int getConstantCount();
  
  protected void setParameters(double[] C, double minX, double maxX, int form) {
    this.C = C.clone();
    super.setMinX(minX);
    super.setMaxX(maxX);
    this.form = form;
  }
  
  protected double[] getC() {
   return this.C.clone();
  }
  
  protected void setC(double[] C) {
   this.C = C.clone();
  }
  
  protected int getForm() {
   return this.form; 
  }
  
  protected void setForm() {
   this.form = form; 
  }
  
  public Object clone() {
    try {
      return super.clone();
    }
    catch (CloneNotSupportedException e) {
     return null; 
    }
  }
  
}