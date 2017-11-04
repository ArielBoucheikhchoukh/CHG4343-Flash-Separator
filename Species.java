public class Species {
  
  private static final int VAPOUR_PRESSURE = 0;
  private static final int ENTHALPY_LIQUID = 1;
  private static final int ENTHALPY_VAPOUR = 2;
  
  public static final int propertyCount = 31;
  public static final int physicalPropertyCount = 8;
  public static final int correlationCount = 3;
  
  private String name;
  private int index;
  private double molarMass; // [g/mol]
  private double Tb;
  private double latentHeat;
  private double accentricFactor;
  private double Tc;
  private double Pc;
  private double Vc;
  private double Zc;
  private Correlation[] correlations;
  
/**********************************************************************************************************************
  1) Constructor
----------------------------------------------------------------------------------------------------------------------*/
  public Species(String name, int index, double[] properties) {
    
    //check if properties is of the correct length (31)
    
    //Store Physical Properties
    this.name = name;
    this.index = index;
    this.molarMass = properties[0];
    this.Tb = properties[1];
    this.latentHeat = properties[2];
    this.accentricFactor = properties[3];
    this.Tc = properties[4];
    this.Pc = properties[5];
    this.Vc = properties[6];
    this.Zc = properties[7];
    
    //Initialize Correlations
    this.correlations = new Correlation[Species.correlationCount];
    
    this.correlations[VAPOUR_PRESSURE] = new VapourPressure();
    this.correlations[ENTHALPY_LIQUID] = new EnthalpyLiquid();
    this.correlations[ENTHALPY_VAPOUR] = new EnthalpyVapour();
    
    int position = Species.physicalPropertyCount;
    double[] C;
    for (int i = 0; i < Species.correlationCount; i++) {
      int constantCount = this.correlations[i].getConstantCount();
      
      C = new double[constantCount];
      System.arraycopy(properties, position, C, 0, constantCount);
      
      position += constantCount;
      this.correlations[i].setParameters(C, properties[position], properties[position + 1], (int) properties[position + 2]);
      
      position += 3;
    }
    
  }
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  2) Copy Constructor
----------------------------------------------------------------------------------------------------------------------*/
  public Species(Species source) {
   
    //Store Physical Properties
    this.name = source.name;
    this.index = source.index;
    this.molarMass = source.molarMass;
    this.Tb = source.Tb;
    this.latentHeat = source.latentHeat;
    this.accentricFactor = source.accentricFactor;
    this.Tc = source.Tc;
    this.Pc = source.Pc;
    this.Vc = source.Vc;
    this.Zc = source.Zc;
    
    //Initialize Correlations and Store Parameters
    this.correlations = new Correlation[Species.correlationCount];
    for (int i = 0; i < Species.correlationCount; i++) {
      this.correlations[i] = source.correlations[i].clone();
    }
    
  }
/*********************************************************************************************************************/
  
  
  public double[] getPhysicalProperties() {
    return new double[]{this.molarMass, this.Tb, this.latentHeat, this.accentricFactor, this.Tc, this.Pc, this.Vc, this.Zc}; 
  }
  
  public double evaluateVapourPressure(double T) throws FunctionException, NumericalMethodException {
    return this.correlations[Species.VAPOUR_PRESSURE].evaluate(T, null);
  }
  
  public double evaluateEnthalpyLiquid(double T, double Tref) throws FunctionException, NumericalMethodException {
    return this.correlations[Species.ENTHALPY_LIQUID].evaluate(T, new double[]{Tref, this.Tc});
  }
  
  public double evaluateEnthalpyVapour(double T, double Tref) throws FunctionException, NumericalMethodException {
    double hL = this.correlations[Species.ENTHALPY_LIQUID].evaluate(this.Tb, new double[]{Tref, this.Tc});
    return this.correlations[Species.ENTHALPY_VAPOUR].evaluate(T, new double[]{this.Tb, hL, 1000*this.latentHeat});
  }
  
  public double evaluateEnthalpyVapour(double T, double Tref, double hL) throws FunctionException, NumericalMethodException {
    return this.correlations[Species.ENTHALPY_VAPOUR].evaluate(T, new double[]{this.Tb, hL, 1000*this.latentHeat});
  }
  
  public String getName() {
   return this.name; 
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public int getIndex() {
   return this.index; 
  }
  
  public void setIndex(int index) {
    this.index = index;
  }
  
  public double getTb() {
    return this.Tb;
  }
  
  public void setTb(double Tb) {
    this.Tb = Tb;
  }
  
  public double getLatentHeat() {
    return this.latentHeat;
  }
  
  public void setLatentHeat(double latentHeat) {
    this.latentHeat = latentHeat;
  }
  
  public double getAccentricFactor() {
    return this.accentricFactor;
  }
  
  public void setAccentricFactor(double accentricFactor) {
    this.accentricFactor = accentricFactor;
  }
  
  public double getTc() {
    return this.Tc;
  }
  
  public void setTc(double Tc) {
    this.Tc = Tc;
  }
  
  public double getPc() {
    return this.Pc;
  }
  
  public void setPc(double Pc) {
    this.Pc = Pc;
  }
  
  public double getVc() {
    return this.Vc;
  }
  
  public void setVc(double Vc) {
    this.Vc = Vc;
  }
  
  public double getZc() {
    return this.Zc;
  }
  
  public void setZc(double Zc) {
    this.Zc = Zc;
  }
  
  public Correlation getCorrelation(int correlationIndex) {
   return (Correlation) this.correlations[correlationIndex].clone(); 
  }
  
}