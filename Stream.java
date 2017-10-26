public class Stream {
  
  private double T; // [K]
  private double P; // [bar]
  private double F; // [mol/s]
  private double vapourQuality; // mole fraction
  private double[] x;
  private double[] y;
  private double[] z;
  private int[] speciesIndices;
  
  
/**********************************************************************************************************************
  1) Constructor A: Used by FlashSeparator to construct outlet stream objects.
---------------------------------------------------------------------------------------------------------------------*/
  public Stream(int componentCount) {
   this.T = 273.15;
   this.P = 10.;
   this.F = 0.;
   this.vapourQuality = 0.;
   this.x = new double[componentCount];
   this.y = new double[componentCount];
   this.z = new double[componentCount];
   this.speciesIndices = new int[componentCount];
  }
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  1) Constructor B: Used by AdiabaticFeedTemp to construct its feed stream object. 
---------------------------------------------------------------------------------------------------------------------*/
  public Stream(double F, double[] z, int[] speciesIndices) {
    this.T = 273.15;
    this.P = 10.;
    this.F = F;
    this.vapourQuality = 0.;
    this.x = new double[z.length];
    this.y = new double[z.length];
    this.z = z.clone();
    this.speciesIndices = speciesIndices.clone();
  }
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  1) Constructor C: Used by IsothermalHeat and AdiabaticFlashTemp to construct their feed stream objects.
---------------------------------------------------------------------------------------------------------------------*/
  public Stream(double T, double F, double[] z, int[] speciesIndices) {
    this.T = T;
    this.P = 10.;
    this.F = F;
    this.vapourQuality = 0.;
    this.x = new double[z.length];
    this.y = new double[z.length];
    this.z = z.clone();
    this.speciesIndices = speciesIndices.clone();
  }
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  1) Copy Constructor 
---------------------------------------------------------------------------------------------------------------------*/
  public Stream(Stream source) {
    this.T = source.T;
    this.P = source.P;
    this.F = source.F;
    this.vapourQuality = source.vapourQuality;
    this.x = source.x.clone();
    this.y = source.y.clone();
    this.z = source.z.clone();
    this.speciesIndices = source.speciesIndices.clone(); 
  }
/*********************************************************************************************************************/
  
  
  public double evaluateStreamEnthalpy(double T, double Tref) {
   
    double hL_i = 0.; 
    double Hv_i = 0.; 
    double H = 0.; 
    for (int i = 0; i < this.speciesIndices.length; i++) {
     
      Species species_i = Menu.getSpecies(this.speciesIndices[i]);
      
      if (this.x[i] > 0) {
        hL_i = species_i.evaluateEnthalpyLiquid(T, Tref);
        
        if (this.y[i] > 0) {
          Hv_i = species_i.evaluateEnthalpyVapour(T, Tref, hL_i);
        }
      }
      else if (this.y[i] > 0) {
        Hv_i = species_i.evaluateEnthalpyVapour(T, Tref);
      }
      
      H += this.F * (this.x[i] * (1 - this.vapourQuality) * hL_i + this.y[i] * this.vapourQuality * Hv_i);
    }
    
    return H;
    
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
  
  public double getVapourQuality() {
   return this.vapourQuality; 
  }
  
  public void setVapourQuality(double vapourQuality) {
   this.vapourQuality = vapourQuality; 
  }
  
  public double[] getX() {
    return this.x.clone();
  }
  
  public void setX(double[] x) {
    this.x = x.clone();
  }
  
  public double getXi(int componentIndex) {
    return this.x[componentIndex];
  }
  
  public void setXi(double x_i, int componentIndex) {
    this.x[componentIndex] = x_i;
  }
  
  public double[] getY() {
    return this.x.clone();
  }
  
  public void setY(double[] y) {
    this.y = y.clone();
  }
  
  public double getYi(int componentIndex) {
    return this.y[componentIndex];
  }
  
  public void setYi(double y_i, int componentIndex) {
    this.y[componentIndex] = y_i;
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
