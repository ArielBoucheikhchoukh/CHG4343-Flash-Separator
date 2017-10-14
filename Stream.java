public class Stream {
  
  private double T;
  private double P;
  private double F;
  private double[] z;
  private Species[] components;
  
  public Stream() {
   this.T = 0;
   this.P = 0;
   this.F = 0;
   z = null;
   components = null;
  }
  
  public Stream(double T, double P, double F, double[] z, Species[] components) {
    
  }
  
  public double getT() {
   return this.T; 
  }
  
  public void setT(double T) {
   this.T = T; 
  }
  
  public double getF() {
   return this.F; 
  }
  
  public void setF(double F) {
   this.F = F; 
  }
  
  public void setComponentCount(int componentCount) {
   this.z = new double[componentCount]; 
  }
  
  public void setZ(double z_i, int i) {
    this.z[i] = z_i;
  }
  
}
