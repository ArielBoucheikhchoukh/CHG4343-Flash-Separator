public class Stream {
  
  private double T; // [K]
  private double P; // [bar]
  private double F; // [mol/s]
  private double[] z;
  private int[] speciesIndices;
  
  public Stream(int componentCount) {
   this.T = 273.15;
   this.P = 10.;
   this.F = 0.;
   this.z = new double[componentCount];
   this.speciesIndices = new int[componentCount];;
  }
  
  public Stream(double F, double[] z, int[] speciesIndices) {
    this.T = 273.15;
    this.P = 10.;
    this.F = F;
    this.z = z.clone();
    this.speciesIndices = speciesIndices.clone();
  }
  
  public Stream(double T, double F, double[] z, int[] speciesIndices) {
    this.T = T;
    this.P = 10.;
    this.F = F;
    this.z = z.clone();
    this.speciesIndices = speciesIndices.clone();
  }
  
  public double getT() {
   return this.T; 
  }
  
  public void setT(double T) {
   this.T = T; 
  }
  
  public double getP() {
   return this.P; 
  }
  
  public void setP(double P) {
   this.P = P; 
  }
  
  public double getF() {
   return this.F; 
  }
  
  public void setF(double F) {
   this.F = F; 
  }
  
  public double[] getZ() {
    return this.z.clone();
  }
  
  public void setZ(double[] z) {
    this.z = z.clone();
  }
  
  public double getZi(int componentIndex) {
    return this.z[componentIndex];
  }
  
  public void setZi(double z_i, int componentIndex) {
    this.z[componentIndex] = z_i;
  }
  
  public int[] getSpeciesIndices() {
    return this.speciesIndices.clone();
  }
  
  public void setSpeciesIndices(int[] speciesIndices) {
    this.speciesIndices = speciesIndices.clone();
  }
  
  public int getSpeciesIndex(int componentIndex) {
    return this.speciesIndices[componentIndex];
  }
  
  public void setSpeciesIndex(int speciesIndex, int componentIndex) {
    this.speciesIndices[componentIndex] = speciesIndex;
  }
  
}
