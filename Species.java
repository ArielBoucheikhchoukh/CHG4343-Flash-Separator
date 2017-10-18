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
  
  public Species() {
    
  }
  
  public double getLatentHeat() {
    return this.latentHeat;
  }
  
  public double evaluateEnthalpyLiquid(double T, double Tref) {
    return enthalpyLiquid.evaluate(T, Tref, this.Tc);
  }
  
  public double evaluateEnthalpyVapour(double T, double Tref) {
    return enthalpyVapour.evaluate(T, this.Tb, this.evaluateEnthalpyLiquid(this.Tb, Tref), this.latentHeat);
  }
  
}
