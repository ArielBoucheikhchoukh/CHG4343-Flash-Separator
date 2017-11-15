public class Behaviour implements Cloneable {
  
  private static final double RACHFORD_RICE_INCREMENT_LENGTH = 10.;
  private static final double RACHFORD_RICE_TOLERANCE = 0.0001;
  private static final int RACHFORD_RICE_MAX_EVALUATION_COUNT = 100000;
  
  
/**********************************************************************************************************************
  1) clone() 
---------------------------------------------------------------------------------------------------------------------*/
  public Behaviour clone() {
    try {
      return (Behaviour) super.clone(); // might be subject to change
    }
    catch (CloneNotSupportedException e) {
     return null; 
    }
  }
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  2) flash() : . 
---------------------------------------------------------------------------------------------------------------------*/
  public void flash(Stream stream) throws FlashCalculationException, NumericalMethodException, FunctionException {
    
    int componentCount = stream.getComponentCount();
    int condensableCount = 0;
    Species[] components = new Species[componentCount];
    for (int i = 0; i < componentCount; i++) {
      components[i] = Menu.getSpecies(stream.getSpeciesIndex(i));
      if (stream.isComponentCondensable(i)) {
        condensableCount++;
      }
    }
    
    int j = 0;
    double[] z_cd = new double[condensableCount];
    for (int i = 0; i < componentCount; i++) {
      if (stream.isComponentCondensable(i)) {
        z_cd[j] = stream.getZi(i) / stream.getCondensableFraction();
        j++;
      }
    }
    
    this.bubbleDewPoint(stream, components);
    System.out.println("Test - Behaviour Class: Bubble/dew point calculation is fine.");
    
    double[] K = this.partitionCoefficients(stream, components, condensableCount);
    Function rachfordRice = new RachfordRice(z_cd, K);
    double vapourFraction = 0.;
    try {
      vapourFraction = Menu.findRoot(rachfordRice, null, 0., RACHFORD_RICE_INCREMENT_LENGTH, RACHFORD_RICE_TOLERANCE, 
                                                  RACHFORD_RICE_MAX_EVALUATION_COUNT);
      System.out.println("Test - Behaviour Class: Rachford-Rice calculation is fine. Vapour fraction = " + vapourFraction);
    }
    catch(NumericalMethodException e) {
      System.out.println(e.getMessage());
      throw new FlashNotPossibleException();
    }
    catch(FunctionException e) {
      System.out.println(e.getMessage());
      throw new FlashNotPossibleException();
    }
    
    stream.setVapourFraction(vapourFraction);
    
    j = 0;
    for (int i = 0; i < componentCount; i++) {
      if (stream.isComponentCondensable(i)) {
        double x = z_cd[j] / (1 + (K[j] - 1) * vapourFraction);
        double y = x * K[j];
        
        stream.setXi(x, i);
        stream.setYi(y, i);
        
        j++;
      }
      else {
        stream.setXi(0, i);
        stream.setYi(0, i);
      }
    }
  }
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  3) bubbleDewPoint() : . 
---------------------------------------------------------------------------------------------------------------------*/
  protected void bubbleDewPoint(Stream stream, Species[] components) 
    throws FlashNotPossibleException, NumericalMethodException, FunctionException {
    
    double P_bp = 0.;
    for (int i = 0; i < stream.getComponentCount(); i++) {
      if (stream.isComponentCondensable(i)) {
        P_bp += stream.getZi(i) * components[i].evaluateVapourPressure(stream.getT(), false);
      }
    }
    
    double P_dp = 0.;
    for (int i = 0; i < stream.getComponentCount(); i++) {
      if (stream.isComponentCondensable(i)) {
        P_dp += stream.getZi(i) * components[i].evaluateVapourPressure(stream.getT(), false);
      }
    }
    P_dp = 1. / P_dp;
    
    System.out.println("Flash Pressure: " + stream.getP());
    System.out.println("Bubble Point Pressure: " + P_bp);
    System.out.println("Dew Point Pressure: " + P_dp);
    
    if (stream.getP() >= P_bp || stream.getP() <= P_dp) {
      throw new FlashNotPossibleException();
    }
  }
/*********************************************************************************************************************/
  

/**********************************************************************************************************************
  4) partitionCoefficients() : . 
---------------------------------------------------------------------------------------------------------------------*/
  protected double[] partitionCoefficients(Stream stream, Species[] components, int condensableCount)
    throws FunctionException {
    
    int j = 0;
    double[] K = new double[condensableCount];
    for (int i = 0; i < components.length; i++) {
      if (stream.isComponentCondensable(i)) {
        K[j] = components[i].evaluateVapourPressure(stream.getT(), false) / stream.getP();
        System.out.println("K value of " +components[i].getName() + ": " + K[j]);
        j++;
      }
    }
    
    return K;
  }
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  5) evaluateStreamEnthalpy() : . 
---------------------------------------------------------------------------------------------------------------------*/
  public double evaluateStreamEnthalpy(double Tref, Stream stream, boolean derivative) throws FunctionException {
    return stream.evaluateStreamEnthalpy(Tref, derivative); 
  }
/*********************************************************************************************************************/
  
}