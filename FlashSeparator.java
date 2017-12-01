import java.text.DecimalFormat;

public abstract class FlashSeparator {
  
  public static final double ENTHALPY_BALANCE_MIN_X = 0.;
  public static final double ENTHALPY_BALANCE_MAX_X = 100000.;
  public static final double ENTHALPY_BALANCE_INCREMENT_LENGTH = 10.;
  public static final double ENTHALPY_BALANCE_TOLERANCE = 0.01;
  public static final int ENTHALPY_BALANCE_MAX_EVALUATION_COUNT = 500000;
  public static final double ENTHALPY_BALANCE_SUB_INCREMENT_FRACTION = 1.;
  
  private String type;
  private String status;
  private double T;
  private double P;
  private double Q;
  private Stream feedStream;
  private Stream flashStream;
  private Stream[] outletStreams;
  private Behaviour behaviour;
  
  
  /**********************************************************************************************************************
    * 1) Constructor
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public FlashSeparator(String type, double T, double P, Stream feedStream, Behaviour behaviour) {
    this.type = type;
    this.status = "";
    
    this.T = T;
    this.P = P;
    this.Q = 0.;
    
    this.feedStream = feedStream.clone();
    
    this.flashStream = feedStream.clone();
    this.flashStream.setName("Flash Stream");
    this.flashStream.setT(this.T, true, false);
    this.flashStream.setP(this.P);
    
    this.outletStreams = new Stream[2];
    this.outletStreams[0] = this.flashStream.clone();
    this.outletStreams[1] = this.flashStream.clone();
    
    this.behaviour = behaviour.clone();
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 2) Copy Constructor
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public FlashSeparator(FlashSeparator source) {
    this.type = source.type;
    this.status = source.status;
    
    this.T = source.T;
    this.P = source.P;
    this.Q = source.Q;
    
    this.feedStream = source.feedStream.clone();
    
    this.flashStream = source.feedStream.clone();
    
    this.outletStreams = new Stream[2];
    this.outletStreams[0] = source.outletStreams[0].clone();
    this.outletStreams[1] = source.outletStreams[1].clone();
    
    this.behaviour = source.behaviour.clone();
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 3) clone()
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public abstract FlashSeparator clone();
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 4) toString() : Returns the state of the FlashSeparator object in the form of a String.
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public String toString() {
    
    String message = new String();
    DecimalFormat formatter = new DecimalFormat("###,###,##0.00");
    
    String behaviourCase = new String();
    if (this.behaviour instanceof NonIdealBehaviour) {
      behaviourCase = "Non-Ideal Behaviour";
    } else {
      behaviourCase = "Ideal Behaviour";
    }
    
    message = "Flash Separator: " + this.type + " - " + behaviourCase + " \r\n" 
      + "   Status: " + this.status + " \r\n"
      + "   T = " + formatter.format(this.T) + " K \r\n" 
      + "   P = " + formatter.format(this.P) + " bar \r\n" 
      + "   Q = " + formatter.format(this.Q) + " J/h \r\n\r\n" 
      + this.feedStream.toString() + "\r\n\r\n"
      + this.flashStream.toString() + "\r\n\r\n" 
      + "Outlet Streams: \r\n" 
      + this.outletStreams[0].toString() + "\r\n" 
      + this.outletStreams[1].toString() + "\r\n";
    
    return message;
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 5) flashCalculation() : .
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public abstract Stream[] flashCalculation() 
    throws FlashCalculationException, NumericalMethodException, 
    FunctionException, StreamException;
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 6) performFlash() : Attempts to flash the feed stream. Returns the flash stream if successful.
    *       If the tank pressure is outside of the bubble-point/dew-point range, then a single-phase
    *       stream is returned instead.
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public Stream performFlash() 
    throws FlashCalculationException, NumericalMethodException, FunctionException {
    
    try {
      // Call behaviour to generate the phase equilibrium data for the flash stream
      this.flashStream = this.behaviour.phaseEquilibrium(this.flashStream.clone());
      this.status = "Feed stream was flashed into liquid and vapour-phase outlet streams.";
    }
    catch (FlashCalculationException e) {
      
      // If a flash is not possible, then return either a liquid-phase or a vapour-phase stream
      
      double P_bp = e.getP_bp(); // Bubble-Point Pressure
      double P_dp = e.getP_dp(); // Dew-Point Pressure
      
      this.flashStream.setP_bp(P_bp);
      this.flashStream.setP_dp(P_dp);
      
      int componentCount = this.flashStream.getComponentCount();
      
      // Liquid-Phase Stream: Tank pressure is above bubble-point pressure.
      if (this.flashStream.getP() > P_bp) {
        this.flashStream.setVapourFraction(0.);
        
        for (int i = 0; i < componentCount; i++) {
          
          // If the component i is condensable, set the liquid-phase mole fraction to its 
          // appropriate value and the vapour-phase mole fraction to 0.
          if (this.flashStream.isComponentCondensable(i)) {
            double x = this.flashStream.getZi(i) / this.flashStream.getCondensableFraction();
            this.flashStream.setXi(x, i);
            this.flashStream.setYi(0., i);
          }
          // If the component i is non-condensable, set both phase mole fractions to 0.
          else {
            this.flashStream.setXi(0., i);
            this.flashStream.setYi(0., i);
          }
        }
        
        if (P_bp != 0 && P_dp != 0) {
          this.status = "Condensable components remained in the liquid phase.";
        }
        else {
          this.status = "The feed stream was not condensable.";
        }
        
      }
      // Vapour-Phase Stream: Tank pressure is below dew-point pressure.
      else if (this.flashStream.getP() < P_dp) {
        this.flashStream.setVapourFraction(1.);
        
        for (int i = 0; i < componentCount; i++) {
          // If the component i is condensable, set the vapour-phase mole fraction to its 
          // appropriate value and the liquid-phase mole fraction to 0.
          if (this.flashStream.isComponentCondensable(i)) {
            double y = this.flashStream.getZi(i) / this.flashStream.getCondensableFraction();
            this.flashStream.setXi(0., i);
            this.flashStream.setYi(y, i);
          }
          // If the component i is non-condensable, set both phase mole fractions to 0.
          else {
            this.flashStream.setXi(0., i);
            this.flashStream.setYi(0., i);
          }
        }
        
        this.status = "Condensable components were completely vaporized.";
      }
    }
    
    return this.flashStream.clone();
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 7) splitPhases() : Splits the flash stream into liquid (i = 0) and vapour/gas (i = 1) phase outlet streams and
    *       returns the outlet streams as an array.
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public Stream[] splitPhases() throws StreamException {
    
    int componentCountTotal = this.flashStream.getComponentCount(); // total number of components
    int componentCountLiquid = 0; // number of components in the liquid phase
    int componentCountGas = 0; // number of components in the vapour phase
    double F_liquid = 0.; // moles in the liquid phase
    double F_gas = 0.; // moles in the vapour/gase phase, including non-condensable components
    
    // Calculate the number of moles in the liquid and vapour/gas phases, and count the number of 
    // components in each phase.
    for (int i = 0; i < componentCountTotal; i++) {
      if (this.flashStream.isComponentCondensable(i)) {
        if (this.flashStream.getXi(i) > 0) {
          componentCountLiquid++;
          F_liquid += this.flashStream.getCondensableFraction() * (1 - this.flashStream.getVapourFraction())
            * this.flashStream.getXi(i) * this.flashStream.getF();
        }
        if (this.flashStream.getYi(i) > 0) {
          componentCountGas++;
          F_gas += this.flashStream.getCondensableFraction() * this.flashStream.getVapourFraction()
            * this.flashStream.getYi(i) * this.flashStream.getF();
        }
      } else {
        F_gas += this.flashStream.getZi(i) * this.flashStream.getF();
        componentCountGas++;
      }
    }
    
    int liquidIndex = 0; // iterator for the liquid phase components
    int gasIndex = 0; // iterator for the gas phase components
    double[] x = new double[componentCountLiquid]; // liquid phase mole fractions
    double[] y = new double[componentCountGas]; // gas phase mole fractions
    int[][] speciesIndices = new int[2][]; // indices of the species present in both phases
    speciesIndices[0] = new int[componentCountLiquid];
    speciesIndices[1] = new int[componentCountGas];
    
    // Calculate the mole fractions of both streams, and identify each component.
    for (int i = 0; i < componentCountTotal; i++) {
      if (this.flashStream.isComponentCondensable(i)) {
        if (this.flashStream.getXi(i) > 0) {
          x[liquidIndex] = this.flashStream.getXi(i) * (1 - this.flashStream.getVapourFraction())
            * this.flashStream.getCondensableFraction() * this.flashStream.getF() / F_liquid;
          speciesIndices[0][liquidIndex] = this.flashStream.getSpeciesIndex(i);
          liquidIndex++;
        }
        if (this.flashStream.getYi(i) > 0) {
          y[gasIndex] = this.flashStream.getYi(i) * this.flashStream.getVapourFraction()
            * this.flashStream.getCondensableFraction() * this.flashStream.getF() / F_gas;
          speciesIndices[1][gasIndex] = this.flashStream.getSpeciesIndex(i);
          gasIndex++;
        }
      } else {
        this.flashStream.setXi(0., i);
        this.flashStream.setYi(0., i);
        y[gasIndex] = this.flashStream.getZi(i) * this.flashStream.getF() / F_gas;
        speciesIndices[1][gasIndex] = this.flashStream.getSpeciesIndex(i);
        gasIndex++;
      }
    }
    
    // Build both stream objects
    this.outletStreams[0] = new Stream("Liquid Phase", this.T, this.P, F_liquid, 0, 0., 0., 0.,
                                       x, null, x, null, null, null, speciesIndices[0]); // Liquid phase
    this.outletStreams[1] = new Stream("Vapour/Gas Phase", this.T, this.P, F_gas, 1, 1., 0., 0.,
                                       null, y, y, null, null, null, speciesIndices[1]); // Vapour phase
    
    return this.getOutletStreams();
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 8) selectReferenceTemperature() : Selects the lowest normal boiling point among all components in 
    *          the stream as the reference temperature for the enthalpy balance.
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public double selectReferenceTemperature() {
    
    double Tref = Double.MAX_VALUE;
    
    for (int i = 0; i < this.feedStream.getComponentCount(); i++) {
      Tref = Math.min(Tref, Menu.getSpecies(this.feedStream.getSpeciesIndex(i)).getTb());
    }
    
    return Tref;
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 9) setFeedStreamTemperature() : Sets the temperature of the feed stream.
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public void setFeedStreamTemperature(double T, boolean updatePhaseFractions) {
    this.feedStream.setT(T, true, updatePhaseFractions);
  }
  /*********************************************************************************************************************/
  
  public String getType() {
    return this.type;
  }
  
  public void setType(String type) {
    this.type = type;
  }
  
  public String getStatus() {
    return status;
  }
  
  public void setStatus(String status) {
    this.status = status;
  }
  
  public double getT() {
    return this.T;
  }
  
  public void setT(double T) {
    this.T = T;
    this.flashStream.setT(T, true, false);
    this.outletStreams[0].setT(T, true, false);
    this.outletStreams[1].setT(T, true, false);
  }
  
  public double getP() {
    return this.P;
  }
  
  public void setP(double P) {
    this.P = P;
    this.flashStream.setP(P);
    this.outletStreams[0].setP(P);
    this.outletStreams[1].setP(P);
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
  
  public Stream getFlashStream() {
    return this.flashStream.clone();
  }
  
  public void setFlashStream(Stream flashStream) {
    this.flashStream = flashStream.clone();
  }
  
  public Stream[] getOutletStreams() {
    
    Stream[] arrayCopy = new Stream[this.outletStreams.length];
    
    for (int i = 0; i < this.outletStreams.length; i++) {
      arrayCopy[i] = this.outletStreams[i].clone();
    }
    
    return arrayCopy;
  }
  
  public void setOutletStreams(Stream[] outletStreams) {
    
    this.outletStreams = new Stream[outletStreams.length];
    
    for (int i = 0; i < this.outletStreams.length; i++) {
      this.outletStreams[i] = outletStreams[i].clone();
    }
  }
  
  public Behaviour getBehaviour() {
    return this.behaviour.clone();
  }
  
  public void setBehaviour(Behaviour behaviour) {
    this.behaviour = behaviour.clone();
  }
  
}