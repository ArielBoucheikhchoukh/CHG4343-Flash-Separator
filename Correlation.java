import java.lang.CloneNotSupportedException;

public abstract class Correlation implements Cloneable{
  
  private double[] C;
  private double minX;
  private double maxX;
  private int form;
  
  public Correlation() {
   this.C = null;
   this.minX = 0;
   this.maxX = 0;
   this.form = 0;
  }
  
  public Correlation(double[] C, double minX, double maxX, int form) {
    this.C = C.clone();
    this.minX = minX;
    this.maxX = maxX;
    this.form = form;
  }
  
  public abstract double evaluate(double[] x);
  
  public abstract int getConstantCount();
  
  protected void setParameters(double[] C, double minX, double maxX, int form) {
    this.C = C.clone();
    this.minX = minX;
    this.maxX = maxX;
    this.form = form;
  }
  
  protected double[] getC() {
   return this.C.clone();
  }
  
  protected void setC(double[] C) {
   this.C = C.clone();
  }
  
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