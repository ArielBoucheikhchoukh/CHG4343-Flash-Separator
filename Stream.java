public class Stream {
  
  private String name;
  private double T; // [K]
  private double P; // [bar]
  private double F; // [mol/s]
  private double condensableFraction; // mole fraction
  private double vapourFraction; // mole fraction; pertains only to the condensable fraction
  private double[] x;
  private double[] y;
  private double[] z;
  private boolean[] isCondensable;
  private int[] speciesIndices;
  
  
/**********************************************************************************************************************
  1.1) Constructor A: Sets all instance variables to default values.
---------------------------------------------------------------------------------------------------------------------*/
  public Stream(int componentCount) {
   this.name = "Stream";
   this.T = 273.15;
   this.P = 10.;
   this.F = 0.;
   this.condensableFraction = 1.;
   this.vapourFraction = 0.;
   this.x = new double[componentCount];
   this.y = new double[componentCount];
   this.z = new double[componentCount];
   this.isCondensable = new boolean[componentCount];
   this.speciesIndices = new int[componentCount];
  }
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  1.2) Constructor B: Used by FlashSeparator children to construct their feed stream objects.
---------------------------------------------------------------------------------------------------------------------*/
  public Stream(String name, double T, double F, double[] z, int[] speciesIndices) {
    this.name = name;
    this.T = T;
    this.P = 10.;
    this.F = F;
    
    this.condensableFraction = 1.;
    this.vapourFraction = 0.;
    
    int componentCount = speciesIndices.length;
    this.x = new double[componentCount];
    this.y = new double[componentCount];
    this.z = z.clone();
    this.isCondensable = new boolean[componentCount];
    this.speciesIndices = speciesIndices.clone();
    
    this.updateCondensableState();
    
    for (int i = 0; i < componentCount; i++) {
      this.x[i] = 1.;
    }
  }
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  1.3) Constructor C: Used by FlashSeparator to construct outlet streams.
                      Define the number and type of phases in the stream via the phaseIndex variable:
                         Liquid only:            phaseIndex = 0
                         Vapour/Gas only:        phaseIndex = 1
                         Liquid, Vapour and Gas: phaseIndex = 2
---------------------------------------------------------------------------------------------------------------------*/
  public Stream(String name, double T, double P, double F, int phaseIndex, double[] x, double[] y, double[] z, 
                int[] speciesIndices) {
   
    this.name = name;
    this.T = T;
    this.P = P;
    this.F = F;
    
    int componentCount = speciesIndices.length;
    switch (phaseIndex) {
      
      case 0:
        this.x = x.clone();
        this.y = new double[componentCount];
        this.z = z.clone();
        break;
        
      case 1:
        this.x = new double[componentCount];
        this.y = y.clone();
        this.z = z.clone();
        break;
        
      case 2:
        this.x = x.clone();
        this.y = y.clone();
        this.z = z.clone();
        break;
        
      default:
        break;
    }
    
    this.isCondensable = new boolean[componentCount];
    this.speciesIndices = speciesIndices.clone();
    
    this.updateCondensableState();
    this.calculateVapourFraction();
  }
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  2) Copy Constructor 
---------------------------------------------------------------------------------------------------------------------*/
  public Stream(Stream source) {
    this.name = source.name;
    this.T = source.T;
    this.P = source.P;
    this.F = source.F;
    this.condensableFraction = source.condensableFraction;
    this.vapourFraction = source.vapourFraction;
    this.x = source.x.clone();
    this.y = source.y.clone();
    this.z = source.z.clone();
    this.isCondensable = source.isCondensable.clone();
    this.speciesIndices = source.speciesIndices.clone(); 
  }
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  3) clone()
---------------------------------------------------------------------------------------------------------------------*/
  public Stream clone() {
    return new Stream(this);
  }
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  4) toString()
---------------------------------------------------------------------------------------------------------------------*/
  public String toString() {
    
    String message = new String();
    
    message = this.name + ": \n" 
      + "   T = " + this.T + " K \n"
      + "   P = " + this.P + " bar \n"
      + "   F = " + this.F + " mol/s \n"
      + "   fraction of condensable moles = " + this.condensableFraction * 100. + " %\n"
      + "   fraction of condensable moles in the vapour phase = " + this.vapourFraction * 100. + "% \n"
      + "   Components: \n";
    
    for (int i = 0; i < this.getComponentCount(); i++) {
      
      message += "      " + (i + 1) + ". " + Menu.getSpeciesName(this.getSpeciesIndex(i)) + ": \n"
        + "         x = " + this.x[i] * 100 + "% \n"
        + "         y = " + this.y[i] * 100 + "% \n"
        + "         z = " + this.z[i] * 100 + "% \n";
      
    }
    
    return message;
  }
/*********************************************************************************************************************/
  
    
/**********************************************************************************************************************
  5) evaluateStreamEnthalpy()
---------------------------------------------------------------------------------------------------------------------*/
  public double evaluateStreamEnthalpy(double Tref, boolean derivative) throws FunctionException {
    
    double H = 0.; 
    for (int i = 0; i < this.speciesIndices.length; i++) {
      
      double hL_i = 0.; 
      double Hv_i = 0.; 
      Species species_i = Menu.getSpecies(this.speciesIndices[i]);
      
      if (this.isCondensable[i]) {
        
        if (this.x[i] > 0) {
          hL_i = species_i.evaluateEnthalpyLiquid(this.T, Tref, derivative);
          
          if (this.y[i] > 0) {
            Hv_i = species_i.evaluateEnthalpyVapour(this.T, Tref, hL_i, derivative);
          }
        }
        else if (this.y[i] > 0) {
          Hv_i = species_i.evaluateEnthalpyVapour(this.T, Tref, derivative);
        }
        
        H += this.condensableFraction * this.F * (this.x[i] * (1 - this.vapourFraction) * hL_i + this.y[i] * this.vapourFraction * Hv_i);
      }
      else {
        Hv_i = species_i.evaluateEnthalpyVapour(this.T, Tref, derivative);
        H += this.z[i] * this.F * Hv_i;
      }
    }
    
    return H;
    
  }
/*********************************************************************************************************************/
  
  
  public int getComponentCount() {
    return this.speciesIndices.length;
  }
  
  public void updateCondensableState() {
    this.condensableFraction = 0.;
    for (int i = 0; i < this.speciesIndices.length; i++) {
      if (this.T < Menu.getSpecies(this.speciesIndices[i]).getTc()) {
        this.condensableFraction += this.z[i];
        this.isCondensable[i] = true; 
      }
      else {
        this.isCondensable[i] = false; 
      }
    }
  }
  
  public void calculateVapourFraction() {
   
    double F_vapour = 0.;
    
    for (int i = 0; i < this.getComponentCount(); i++) {
      F_vapour += this.y[i] * this.condensableFraction * this.F;
    }
    
    this.vapourFraction = F_vapour / (this.condensableFraction * this.F);
  }
  
  public String getName() {
   return this.name; 
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public double getT() {
   return this.T; 
  }
  
  public void setT(double T) {
   this.T = T;
   this.updateCondensableState();
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
  
  public double getCondensableFraction() {
   return this.condensableFraction; 
  }
  
  public void setCondensableFraction(double condensableFraction) {
   this.condensableFraction = condensableFraction; 
  }
  
  public double getVapourFraction() {
   return this.vapourFraction; 
  }
  
  public void setVapourFraction(double vapourFraction) {
    this.vapourFraction = vapourFraction; 
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
  
  public boolean[] getIsCondensable() {
    return this.isCondensable.clone();
  }
  
  public void setIsCondensable(boolean[] isCondensable) {
    this.isCondensable = isCondensable.clone();
  }
  
  public boolean isComponentCondensable(int componentIndex) {
    return this.isCondensable[componentIndex];
  }
  
  public void setComponentCondensableState(boolean isCondensable, int componentIndex) {
    this.isCondensable[componentIndex] = isCondensable;
  }
  
}
