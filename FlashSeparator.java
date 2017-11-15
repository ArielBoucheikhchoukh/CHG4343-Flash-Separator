public abstract class FlashSeparator {
 
  protected static final double ENTHALPY_BALANCE_MIN_X = 0.;
  protected static final double ENTHALPY_BALANCE_MAX_X = 100000.;
  protected static final double ENTHALPY_BALANCE_INCREMENT_LENGTH = 10.;
  protected static final double ENTHALPY_BALANCE_TOLERANCE = 0.01;
  protected static final int ENTHALPY_BALANCE_MAX_EVALUATION_COUNT = 100000;
  
  private double T;
  private double P;
  private double Q;
  private Behaviour behaviour;
  private Stream feedStream;
  
  
/**********************************************************************************************************************
  1) Constructor A : . 
---------------------------------------------------------------------------------------------------------------------*/
  public FlashSeparator(double P, Stream feedStream) {
    this.T = 273.15;
    this.P = P;
    this.Q = 0.;
    this.feedStream = new Stream(feedStream);
  }
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  1) Constructor B : . 
---------------------------------------------------------------------------------------------------------------------*/
  public FlashSeparator(double T, double P, Stream feedStream) {
    this.T = T;
    this.P = P;
    this.Q = 0.;
    this.feedStream = new Stream(feedStream);
  }
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  2) toString() : Returns the state of the FlashSeparator object in the form of a String. 
---------------------------------------------------------------------------------------------------------------------*/
  public String toString() {
   
    String message = new String();
    
    message = "Flash Separator: \n" 
      + "   T = " + this.T + " K \n"
      + "   P = " + this.P + " bar \n"
      + "   Q = " + this.Q + " J/s \n"
      + this.feedStream.toString() + "\n";
    
    return message;
  }
/*********************************************************************************************************************/
    
  
/**********************************************************************************************************************
  3) flashCalculation() : . 
---------------------------------------------------------------------------------------------------------------------*/
  public abstract Stream[] flashCalculation() throws FlashCalculationException, FunctionException, NumericalMethodException;
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  4) flash() : . 
---------------------------------------------------------------------------------------------------------------------*/
  protected void flash(Stream flashStream) throws FlashCalculationException, NumericalMethodException, FunctionException {
    this.behaviour.flash(flashStream); /* This method should perform a dew-point/bubble-point calculation,
                                          evaluate the K-values, and solve the Rachford-Rice equation.
                                          Should return an exception that extends FlashCalculationException in the 
                                          case where a flash calculation cannot be performed. */
  }
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  5) splitPhases() : Splits a stream into liquid (i = 0) and vapour/gas (i = 1) streams. 
---------------------------------------------------------------------------------------------------------------------*/
  public Stream[] splitPhases(Stream stream) {
   
    int componentCountTotal = stream.getComponentCount();
    int componentCountLiquid = 0;
    int componentCountGas = 0;
    double F_liquid = 0.;
    double F_gas = 0.;
    double[][] n = new double[2][componentCountTotal];
    Stream[] outletStreams = new Stream[2];
    System.out.println("Test - FlashSeparator Class - splitPhases() Method: Test 1");
    
    for (int i = 0; i < componentCountTotal; i++) {
      if (stream.isComponentCondensable(i)) {
        if (stream.getXi(i) > 0) {
          componentCountLiquid++;
          F_liquid += stream.getCondensableFraction() * stream.getXi(i) * stream.getF();
        }
        if (stream.getYi(i) > 0) {
          componentCountGas++;
          F_gas += stream.getCondensableFraction() * stream.getYi(i) * stream.getF();
        }
      }
      else {
        F_gas += stream.getZi(i) * stream.getF();
        componentCountGas++;
      }
    }
    System.out.println("Test - FlashSeparator Class - splitPhases() Method: Test 2");
    
    int liquidIndex = 0;
    int gasIndex = 0;
    double[] x = new double[componentCountLiquid];
    double[] y = new double[componentCountGas];
    int[][] speciesIndices = new int[2][];
    speciesIndices[0] = new int[componentCountLiquid];
    speciesIndices[1] = new int[componentCountGas];
    for (int i = 0; i < componentCountTotal; i++) {
      if (stream.isComponentCondensable(i)) {
        if (stream.getXi(i) > 0) {
          x[liquidIndex] = stream.getXi(i) * stream.getCondensableFraction() * stream.getF() / F_liquid;
          speciesIndices[0][liquidIndex] = stream.getSpeciesIndex(i);
          liquidIndex++;
        }
        if (stream.getYi(i) > 0) {
          y[gasIndex] = stream.getYi(i) * stream.getCondensableFraction() * stream.getF() / F_gas;
          speciesIndices[1][gasIndex] = stream.getSpeciesIndex(i);
          gasIndex++;
        }
      }
      else {
        y[gasIndex] = stream.getZi(i) *  stream.getF() / F_gas;
        speciesIndices[1][gasIndex] = stream.getSpeciesIndex(i);
        gasIndex++;
      }
    }
    System.out.println("Test - FlashSeparator Class - splitPhases() Method: Test 3");
    
    outletStreams[0] = new Stream("Liquid Phase", stream.getT(), stream.getP(), F_liquid, 0, x, null, x, speciesIndices[0]);
    outletStreams[1] = new Stream("Vapour/Gas Phase", stream.getT(), stream.getP(), F_gas, 1, null, y, y, speciesIndices[1]);
    System.out.println("Test - FlashSeparator Class - splitPhases() Method: Test 4");
    
    return outletStreams;
    
  }
/*********************************************************************************************************************/
    
  
/**********************************************************************************************************************
  6) selectReferenceTemperature() : Selects the lowest normal boiling point among all components in the stream as the
                                    reference temperature for the enthalpy balance. 
---------------------------------------------------------------------------------------------------------------------*/
  public double selectReferenceTemperature() {
    
    double Tref = 0.;
    
    for (int i = 0; i < this.feedStream.getComponentCount(); i++) {
      Tref = Math.min(Tref, Menu.getSpecies(this.feedStream.getSpeciesIndex(i)).getTb());
    }
    
    return Tref;
  }
  
/*********************************************************************************************************************/
  
/**********************************************************************************************************************
  7) setFeedStreamTemperature() : Sets the temperature of the feed stream. 
---------------------------------------------------------------------------------------------------------------------*/
  public void setFeedStreamTemperature(double T) {
    this.feedStream.setT(T);
  }
/*********************************************************************************************************************/
  
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
  
  public double getQ() {
   return this.Q; 
  }
  
  public void setQ(double Q) {
   this.Q = Q; 
  }
  
  public Stream getFeedStream() {
   return this.feedStream.clone(); 
  }
  
  public void setFeedStream(Stream feedStream) {
   this.feedStream = feedStream.clone();
  }
  
  protected Behaviour getBehaviour() {
    return this.behaviour.clone(); 
  }
  
  protected void setBehaviour(boolean nonIdealBehaviour) {
    if (!nonIdealBehaviour) {
     this.behaviour = new Behaviour(); 
    }
    else {
      this.behaviour = new NonIdealBehaviour();
    }
  }
  
}