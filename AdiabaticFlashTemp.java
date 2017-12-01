//Case 2

public class AdiabaticFlashTemp extends FlashSeparator implements Function {
  
  public static final double MIN_TEMPERATURE = 0.01;
  public static final double FLASH_TEMPERATURE_INCREMENT_FRACTION = 0.2;
  public static final double FLASH_TEMPERATURE_SUB_INCREMENT_FRACTION = 1.;
  public static final double FLASH_TEMPERATURE_TOLERANCE = 1.;
  public static final int FLASH_TEMPERATURE_MAX_EVALUATION_COUNT = 100000;
  
  private double Tref;
  
  /**********************************************************************************************************************
    * 1) Constructor
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public AdiabaticFlashTemp(double feedT, double tankP, double F, double[] z, int[] speciesIndices, 
                            Behaviour behaviour) throws StreamException {
    super("Adiabatic Flash Temperature", feedT, tankP, 
          new Stream("Feed Stream", feedT, F, z, speciesIndices), behaviour);
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 2) Copy Constructor
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public AdiabaticFlashTemp(AdiabaticFlashTemp source) throws StreamException {
    super(source);
    this.Tref = source.Tref;
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 3) clone()
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public AdiabaticFlashTemp clone() {
    try {
      return new AdiabaticFlashTemp(this);
    }
    catch (StreamException e) {
      return null;
    }
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 4) flashCalculation() : Performs flash calculation and return outlet liquid-phase and vapour-phase 
    *        outlet streams; computes the temperature of the flash under adiabatic conditions.
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public Stream[] flashCalculation() 
    throws FlashCalculationException, NumericalMethodException, 
    FunctionException, StreamException {
    
    Stream feedStream = super.getFeedStream(); // Feed Stream
    Stream flashStream = super.getFlashStream(); // Flash Stream (Liquid + Vapour)
    Stream[] outletStreams; // Outlet Streams (2)
    
    /*
     * I. Store Critical Temperatures, Tc
     * -----------------------------------------------------------------------------
     */
    int componentCount = flashStream.getComponentCount();
    boolean isStreamCondensable = false;
    double[] Tc = new double[componentCount];
    for (int i = 0; i < componentCount; i++) {
      Tc[i] = Menu.getSpecies(flashStream.getSpeciesIndex(i)).getTc(); // Stores Tc of each component
      if (feedStream.isComponentCondensable(i)) {
        isStreamCondensable = true;
      }
    }
    
    /*
     * II. Verify Condensable State of Feed
     * -----------------------------------------------------------------------------
     */
    // If all components are non-condensable, then force return of a vapour stream
    if (!isStreamCondensable) {
      this.setT(feedStream.getT());
      this.setStatus("The feed stream was not condensable.");
      outletStreams = super.splitPhases();
      return outletStreams;
    }
    
    /*
     * III. Search for Flash Temperature
     * -----------------------------------------------------------------------------
     */
    // See end of file for detailed explanation of this algorithm.
    boolean foundFlashTemp = false;
    double T_L = AdiabaticFlashTemp.MIN_TEMPERATURE; // Lower Temperature Bound for Condensable State Assumption
    double T_U = super.getFeedStream().getT() - 0.001; // Upper Temperature Bound for Condensable State Assumption
    double T_bp = 0.; // Bubble-Point Temperature
    double T_dp = 0.; // Dew-Point Temperature
    this.Tref = super.selectReferenceTemperature(); // Returns lowest pure-species normal boiling point
    do {
      
      // Step 1. Make a Condensable State Assumption
      T_L = AdiabaticFlashTemp.MIN_TEMPERATURE; // Set T_L to the minimum temperature by default
      
      // Set T_L to the next lowest critical temperature
      for (int i = 0; i < componentCount; i++) {
        // If Tc[i] is below the current upper bound but greater than the current lower bound,
        // the it is the next critical temperature
        if (Tc[i] < T_U && Tc[i] > T_L) {
          T_L = Tc[i];
        }
      }
      
      // Step 2. Update Condensable State of the Flash Stream
      flashStream.setT(0.5*(T_L + T_U), true, false); // Setting the stream temperature also updates isCondensable
      
      // Step 3. Calculate Bubble-Point and Dew-Point Temperatures
      T_bp = super.getBehaviour().calculateBubblePointTemperature(flashStream.clone());
      T_dp = super.getBehaviour().calculateDewPointTemperature(flashStream.clone());
      
      // Sub-Divide Temperature Range into 3
      int iterationCount = 0;
      do { // Only perform the first 3 iterations
        boolean attemptToSolve = false;
        double T_L2 = T_L; // Lower Temperature Bound of Sub-Division
        double T_U2 = T_U; // Upper Temperature Bound of Sub-Division
        
        // Step 4. Select a Sub-Division of the Temperature Segment
        
        // i) First Iteration: Attempt to completely evaporate the feed stream
        if (iterationCount == 0) {
          if (T_dp < T_U) {
            attemptToSolve = true;
            if (T_dp > T_L) {
              T_L2 = T_dp;
            }
          }
        }
        
        // ii) Second Iteration: Attempt to flash separate the feed stream
        else if (iterationCount == 1) {
          if (Math.abs(T_dp - T_bp) > Behaviour.BUBBLE_DEW_POINT_TOLERANCE) {
            attemptToSolve = true;
            if (T_bp > T_L && T_bp < T_U) {
              T_L2 = T_bp;
            }
            if (T_dp < T_U && T_dp > T_L) {
              T_U2 = T_dp;
            }
          }
        }
        
        // iii) Third Iteration: Attempt to keep the feed stream in the liquid phase
        else if (iterationCount == 2) {
          if (T_bp > T_L) {
            attemptToSolve = true;
            if (T_bp < T_U) {
              T_U2 = T_bp;
            }
          }
        }
        
        // Step 5. Attempt to Solve for Flash Temperature
        if (attemptToSolve) {
          try {
            // Pass AdiabaticFlashTemp as the function and search in the negative direction from
            // T_U2 to T_L2
            Menu.findRoot((Function) this, null, T_U2, T_L2, 
                          AdiabaticFlashTemp.FLASH_TEMPERATURE_INCREMENT_FRACTION
                            * Math.abs(T_U2 - T_L2), 
                          AdiabaticFlashTemp.FLASH_TEMPERATURE_SUB_INCREMENT_FRACTION,
                          AdiabaticFlashTemp.FLASH_TEMPERATURE_TOLERANCE, 
                          AdiabaticFlashTemp.FLASH_TEMPERATURE_MAX_EVALUATION_COUNT);
            foundFlashTemp = true;
          } 
          catch (NumericalMethodException | FunctionException e) {
            if (T_L2 == T_L && T_U2 == T_U) {
              iterationCount = 3;
            }
          }
        }
        
        iterationCount++;
        
      } while (!foundFlashTemp && iterationCount < 3);
      
      T_U = T_L; // If a another assumption is necessary, set T_U to the current T_L
      
    } while (T_L > AdiabaticFlashTemp.MIN_TEMPERATURE && !foundFlashTemp);
    
    if (T_L <= AdiabaticFlashTemp.MIN_TEMPERATURE && !foundFlashTemp) {
      throw new FlashCalculationException(flashStream.getT(), flashStream.getP(), 0., 0., T_bp, T_dp);
    }
    
    /*
     * IV. Split Flash Stream into Liquid and Vapour Phases
     * -----------------------------------------------------------------------------
     */
    outletStreams = super.splitPhases();
    
    return outletStreams;
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 5) calculateFlashTemperature() : Calculates a new flash temperature.
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public double calculateFlashTemperature(double T) 
    throws FunctionException {
    
    Stream feedStream = super.getFeedStream(); // Feed Stream
    
    // Step 1. Guess a Flash Temperature
    super.setT(T);
    
    // Step 2. Attempt Flash Separation
    Stream flashStream; // Flash Stream
    try {
      flashStream = super.performFlash();
    } catch (FlashCalculationException | NumericalMethodException | IllegalArgumentException e) {
      throw new UndefinedFunctionException("Flash Calculation", null, T);
    }
    
    // Step 3. Calculate New Flash Temperature
    
    // The flash stream is passed to the enthalpy balance as an outlet stream of unknown temperature, 
    // whereas the feed stream is passed as an inlet stream of known temperature
    EnthalpyBalance enthalpyBalance = new EnthalpyBalance(Tref,
                                                          new Stream[] { new Stream(flashStream) }, 
                                                          new Stream[] { new Stream(feedStream) },
                                                          null, super.getBehaviour(), false, false);
    
    // Start at the feed temperature and search in the negative direction for the flash temperature
    double T_flash = 1.;
    try {
      T_flash = Menu.findRoot(enthalpyBalance, null, feedStream.getT() 
                                - FlashSeparator.ENTHALPY_BALANCE_TOLERANCE, 
                              AdiabaticFlashTemp.MIN_TEMPERATURE,
                              FlashSeparator.ENTHALPY_BALANCE_INCREMENT_LENGTH, 
                              FlashSeparator.ENTHALPY_BALANCE_SUB_INCREMENT_FRACTION,
                              FlashSeparator.ENTHALPY_BALANCE_TOLERANCE + 0.01,
                              FlashSeparator.ENTHALPY_BALANCE_MAX_EVALUATION_COUNT);
    } catch(NumericalMethodException | FunctionException | IllegalArgumentException e) {
      throw new UndefinedFunctionException(enthalpyBalance.getID(), enthalpyBalance, super.getT());
    }
    
    return T_flash;
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 6) evaluate() : Returns the difference between the new and old flash temperatures.
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public double evaluate(double x, double[] constants) throws FunctionException  {
    
    double newFlashTemp = this.calculateFlashTemperature(x);
    return super.getT() - newFlashTemp;
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 7) evaluateDerivative() : Not defined.
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public double evaluateDerivative(double x, double[] constants) throws FunctionException {
    throw new DerivativeNotDefinedException("FlashSeparator: AdiabaticFlashTemp", this);
  }
  /*********************************************************************************************************************/
  
  
  public double getTref() {
    return Tref;
  }
  
  public void setTref(double tref) {
    Tref = tref;
  }
  
  // Explanation of algorithm in flashCalculation(), part III. //
  /* The MIN_TEMPERATURE is the absolute lowest possible temperature, and the feed temperature is the 
   * absolute highest. The temperature range between MIN_TEMPERATURE and the feed temperature is 
   * divided into segments; specifically, the critical temperatures of each component in the stream that 
   * fall within this range serve as the dividing points. The upper and lower bounds of these segments 
   * are denoted by T_U and T_L at any given iteration of the do-while loop. At the outset of each 
   * iteration of the do-while loop, an assumption is made: the flash temperature lies between T_U and T_L,
   * meaning that all components with Tc above T_L are condensable, and all components with Tc below T_L
   * are not condensable. The isCondensable instance variable of flashStream is set accordingly, and the
   * flash calculation is carried out using this assumption. Additionally, each segment (T_L, T_U) is 
   * further sub-divided into three more segments: (T_dp, T_U) where the flash stream is in the vapour
   * phase, (T_bp, T_dp) where the flash stream is at VLE, and (T_L, T_bp) where the flash stream
   * is in the liquid phase. The algorithm moves down the temperature axis, and so will first attempt to
   * completely evaporate the feed, and then attempt a flash, and finally try to keep the flash stream in
   * the liquid phase. 
   */
  
}