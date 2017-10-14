public class Species {

  private String name;
  private int index;
  private double molarMass; //g/mol
  private double Tb;
  private double latentHeat;
  private double accentricFactor;
  private double Tc;
  private double Pc;
  private double Vc;
  private double Zc;
  private VapourPressure vapourPressure;
  private EnthalpyLiquid enthalpyLiquid;
  private EnthalpyVapour enthalpyVapour;
  private static ResidualEnthalpy residualEnthalpy;
  
  public Species() {
    
  }
  
  public double getLatentHeat() {
    return this.latentHeat;
  }
  
  public double evaluateEnthalpyLiquid(double T, double Tref) {
    return enthalpyLiquid.evaluate(T, Tref, this.Tc);
  }
  
  public double valuateEnthalpyVapour(double T, double Tref) {
    double HR = 0;
    double HRref = 0;
    
    if (Species.residualEnthalpy != null) {
      HR = Species.residualEnthalpy.evaluate(T, this);
      HRref = Species.residualEnthalpy.evaluate(Tref, this);
    }
    
    return enthalpyVapour.evaluate(T, this.Tb, this.evaluateEnthalpyLiquid(this.Tb, Tref), this.latentHeat) + HR - HRref;
  }
  
  public static void createResidualEnthalpy(boolean create) {
    if (create) {
      Species.residualEnthalpy = new ResidualEnthalpy();
    }
    else {
      Species.residualEnthalpy = null;
    }
  }
  
}