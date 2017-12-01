public class Behaviour implements Cloneable {
  
  public static final double BUBBLE_DEW_POINT_INCREMENT_LENGTH = 25.;
  public static final double BUBBLE_DEW_POINT_SUB_INCREMENT_FRACTION = 1.;
  public static final double BUBBLE_DEW_POINT_TOLERANCE = 0.01;
  public static final int BUBBLE_DEW_POINT_MAX_EVALUATION_COUNT = 100000;
  public static final double RACHFORD_RICE_INCREMENT_LENGTH = 0.1;
  public static final double RACHFORD_RICE_TOLERANCE = 0.0001;
  public static final int RACHFORD_RICE_MAX_EVALUATION_COUNT = 100000;
  
  
  /**********************************************************************************************************************
    * 1) clone()
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public Behaviour clone() {
    return new Behaviour();
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 2) phaseEquilibrium() : Calculates the phase mole fractions of the flashStream, if possible.
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public Stream phaseEquilibrium(Stream flashStream)
    throws FlashCalculationException, NumericalMethodException, FunctionException {
    
    /*
     * I. Bubble-Point and Dew-Point Calculations
     * -----------------------------------------------------------------------------
     */
    double P_bp = this.calculateBubblePointPressure(flashStream, null, null); // Bubble-Point Pressure
    double P_dp = this.calculateDewPointPressure(flashStream, null, null); // Dew-Point Pressure
    
    //Check if the tank pressure is within the bubble-point/dew-point range.
    if (flashStream.getP() > P_bp || flashStream.getP() < P_dp 
          || Math.abs(P_bp - P_dp) < Behaviour.BUBBLE_DEW_POINT_TOLERANCE) {
      
      throw new FlashCalculationException(flashStream.getT(), flashStream.getP(), P_bp, P_dp, 0., 0.);
    }
    flashStream.setP_bp(P_bp);
    flashStream.setP_dp(P_dp);
    
    /*
     * II. Calculate Partition Coefficients
     * -----------------------------------------------------------------------------
     */
    
    // Store the condensable mole fractions of all components 
    int componentCount = flashStream.getComponentCount();
    double[] z = new double[componentCount];
    for (int i = 0; i < componentCount; i++) {
      if (flashStream.isComponentCondensable(i)) {
        z[i] = flashStream.getZi(i) / flashStream.getCondensableFraction();
      }
      else {
        z[i] = 0; // Non-condensable components are essentially ignored.
      }
    }
    
    double[] K = this.calculatePartitionCoefficients(flashStream);
    
    /*
     * III. Calculate Vapour Fraction
     * -----------------------------------------------------------------------------
     */
    RachfordRice rachfordRice = new RachfordRice(z, K, flashStream.getIsCondensable());
    double vapourFraction = 0.;
    vapourFraction = Menu.findRoot(rachfordRice, null, 0., 1., 
                                   Behaviour.RACHFORD_RICE_INCREMENT_LENGTH, 
                                   BracketingRootFinder.DEFAULT_SUB_INCREMENT_FRACTION, 
                                   Behaviour.RACHFORD_RICE_TOLERANCE, 
                                   Behaviour.RACHFORD_RICE_MAX_EVALUATION_COUNT);
    
    flashStream.setVapourFraction(vapourFraction);
    flashStream.setK(K);
    
    /*
     * IV. Calculate Phase Mole Fractions
     * -----------------------------------------------------------------------------
     */
    for (int i = 0; i < componentCount; i++) {
      if (flashStream.isComponentCondensable(i)) {
        double x = z[i] / (1 + (K[i] - 1) * vapourFraction);
        double y = x * K[i];
        
        flashStream.setXi(x, i);
        flashStream.setYi(y, i);
      } else {
        flashStream.setXi(0, i);
        flashStream.setYi(0, i);
      }
    }
    
    return flashStream.clone();
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 5) calculateBubblePointPressure() : Calculates the bubble point pressure of a stream.
    *           The gamma and phi arrays do not affect the algorithm, and are only
    *           present in the argument list to match with the signature of the
    *           polymorphic method in NonIdealBehaviour.
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public double calculateBubblePointPressure(Stream stream, double[] gamma, double[] phi) 
    throws NumericalMethodException, FunctionException {
    
    int componentCount = stream.getComponentCount(); // Number of components
    double P_bp = 0.; // Bubble-Point Pressure
    gamma = new double[componentCount];
    phi = new double[componentCount];
    
    for (int i = 0; i < componentCount; i++) {
      Species component = Menu.getSpecies(stream.getSpeciesIndex(i));
      if (stream.isComponentCondensable(i)) {
        P_bp += (stream.getZi(i) / stream.getCondensableFraction())
          * component.evaluateVapourPressure(stream.getT(), false);
      }
      gamma[i] = 1.;
      phi[i] = 1.;
    }
    
    return P_bp;
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 6) calculateDewPointPressure() : Calculates the dew point pressure of a stream.
    *          The gamma and phi arrays do not affect the algorithm, and are only
    *          present in the argument list to match with the signature of the
    *          polymorphic method in NonIdealBehaviour.
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public double calculateDewPointPressure(Stream stream, double[] gamma, double[] phi) 
    throws NumericalMethodException, FunctionException {
    
    int componentCount = stream.getComponentCount(); // Number of components
    double P_dp = 0.; // Dew-Point Pressure
    gamma = new double[componentCount]; 
    phi = new double[componentCount];
    
    for (int i = 0; i < componentCount; i++) {
      Species component = Menu.getSpecies(stream.getSpeciesIndex(i));
      if (stream.isComponentCondensable(i)) {
        P_dp += (stream.getZi(i) / stream.getCondensableFraction())
          / component.evaluateVapourPressure(stream.getT(), false);
      }
      gamma[i] = 1.;
      phi[i] = 1.;
    }
    if (P_dp != 0.) {
      P_dp = 1. / P_dp;
    }
    
    return P_dp;
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 7) calculateBubblePointTemperature() : Calculates the bubble point temperature of a stream.
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public double calculateBubblePointTemperature(Stream stream) 
    throws NumericalMethodException, FunctionException {
    
    /*
     * I. Initialize Arrays
     * -----------------------------------------------------------------------------
     */
    int componentCount = stream.getComponentCount(); // Number of components
    boolean[] isCondensable = stream.getIsCondensable();
    double[] x = new double[componentCount]; // overall mole fractions
    int j = -1; // Index of the first condensable component
    for (int i = 0; i < componentCount; i++) {
      if (stream.isComponentCondensable(i)) {
        x[i] = stream.getZi(i) / stream.getCondensableFraction(); // Store the overall mole fractions
        if (j < 0) {
          j = i; // select species i to be species j
        }
      }
    }
    // If j is still equal to -1, then no condensable species are present
    if (j == -1) {
      return 0.;
    }
    
    /*
     * I. Calculate Saturation Temperature
     * -----------------------------------------------------------------------------
     */
    double[] T_sat = new double[componentCount]; // Saturation temperatures of each component
    for (int i = 0; i < componentCount; i++) {
      if (isCondensable[i]) {
        try {
          T_sat[i] = Menu.findRoot(
                                   Menu.getSpecies(stream.getSpeciesIndex(i)).getCorrelation(Species.VAPOUR_PRESSURE), 
                                   new double[] {stream.getP()}, 1., true, 
                                   Behaviour.BUBBLE_DEW_POINT_INCREMENT_LENGTH, 
                                   Behaviour.BUBBLE_DEW_POINT_SUB_INCREMENT_FRACTION, 
                                   Behaviour.BUBBLE_DEW_POINT_TOLERANCE, 
                                   Behaviour.BUBBLE_DEW_POINT_MAX_EVALUATION_COUNT, false);
        } catch (NumericalMethodException e) {
          T_sat[i] = 0.; //assume this species has no contribution to VLE
        }
      }
      else {
        T_sat[i] = 0.;
      }
    }
    
    /*
     * III. Calculate Initial Estimate of the Bubble-Point Temperature
     * -----------------------------------------------------------------------------
     */
    double T_bp = 0.; // Bubble-Point Temperature
    for (int i = 0; i < componentCount; i++) {
      T_bp += x[i] * T_sat[i];
    }
    
    /*
     * IV. Iterate until Convergence
     * -----------------------------------------------------------------------------
     */
    int iterationCount = 0;
    double error = 0.;
    boolean isIncreasing = true;
    do {
      // Step 1. Calculate Vapour Pressures of each Component
      double[] P_sat = new double[componentCount]; // Vapour Pressures
      for (int i = 0; i < componentCount; i++) {
        if (isCondensable[i]) {
          P_sat[i] = Menu.getSpecies(stream.getSpeciesIndex(i)).evaluateVapourPressure(T_bp, false);
        }
        else {
          P_sat[i] = 0.;
        }
      }
      
      // Step 2. Calculate the Vapour Pressure of Species j
      double P_j = 0.;
      for (int i = 0; i < componentCount; i++) {
        if (isCondensable[i]) {
          P_j += x[i] * P_sat[i] / P_sat[j];
        } 
      }
      P_j = stream.getP() / P_j;
      
      // Step 3. Calculate the New Bubble-Point Temperature
      double T_new = Menu.findRoot(
                                   Menu.getSpecies(stream.getSpeciesIndex(j)).getCorrelation(Species.VAPOUR_PRESSURE), 
                                   new double[] {P_j}, 1., true, 
                                   Behaviour.BUBBLE_DEW_POINT_INCREMENT_LENGTH, 
                                   Behaviour.BUBBLE_DEW_POINT_SUB_INCREMENT_FRACTION, 
                                   Behaviour.BUBBLE_DEW_POINT_TOLERANCE, 
                                   Behaviour.BUBBLE_DEW_POINT_MAX_EVALUATION_COUNT, false);
      
      if (T_new > T_bp) {
        isIncreasing = true;
      }
      else {
        isIncreasing = false;
      }
      
      // Step 4. Check Error
      error = Math.abs(T_bp - T_new); // Calculate error between new and old T_bp
      T_bp = T_new;
      if (Double.isNaN(T_bp) || Double.isInfinite(T_bp) || T_bp < 0.) {
        break;
      }
      
      iterationCount++;
      if (iterationCount > Behaviour.BUBBLE_DEW_POINT_MAX_EVALUATION_COUNT) {
        throw new TooManyFunctionEvaluationsException("Simple Iteration", 
                                                      "Ideal Bubble-Point Temperature", null, null);
      }
      
    } while (error > Behaviour.BUBBLE_DEW_POINT_TOLERANCE);
    
    /*
     * V. Verify Integrity of Bubble-Point Temperature
     * -----------------------------------------------------------------------------
     */
    if (Double.isNaN(T_bp) || Double.isInfinite(T_bp) || T_bp < 0.) {
      if (isIncreasing || T_bp > 0.) {
        T_bp = Double.MAX_VALUE;
      }
      else {
        T_bp = 0.;
      }
    }
    
    return T_bp;
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 8) calculateDewPointTemperature() : Calculates the dew point temperature of a stream.
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public double calculateDewPointTemperature(Stream stream) 
    throws NumericalMethodException, FunctionException {
    
    /*
     * I. Initialize Arrays
     * -----------------------------------------------------------------------------
     */
    int componentCount = stream.getComponentCount(); // Number of Components
    boolean[] isCondensable = stream.getIsCondensable();
    double[] y = new double[componentCount]; // Overall Mole Fractions
    int j = -1; // Index of the first condensable species
    for (int i = 0; i < componentCount; i++) {
      if (stream.isComponentCondensable(i)) {
        y[i] = stream.getZi(i) / stream.getCondensableFraction(); // Store overall mole fractions
        if (j < 0) {
          j = i; // Select species i to be species j
        }
      }
    }
    // If j is still equal to -1, then no condensable species are present
    if (j == -1) {
      return 0.;
    }
    
    /*
     * II. Calculate Saturation Temperature
     * -----------------------------------------------------------------------------
     */
    double[] T_sat = new double[componentCount]; //Saturations Temperatures
    for (int i = 0; i < componentCount; i++) {
      if (isCondensable[i]) {
        try {
          T_sat[i] = Menu.findRoot(
                                   Menu.getSpecies(stream.getSpeciesIndex(i)).getCorrelation(Species.VAPOUR_PRESSURE), 
                                   new double[] {stream.getP()}, 1., true, 
                                   Behaviour.BUBBLE_DEW_POINT_INCREMENT_LENGTH, 
                                   Behaviour.BUBBLE_DEW_POINT_SUB_INCREMENT_FRACTION, 
                                   Behaviour.BUBBLE_DEW_POINT_TOLERANCE, 
                                   Behaviour.BUBBLE_DEW_POINT_MAX_EVALUATION_COUNT, false);
        } catch (NumericalMethodException e) {
          T_sat[i] = 0.00001; //assume this species has no contribution to VLE
        }
      }
    }
    
    /*
     * III. Calculate Initial Estimate of the Dew-Point Temperature
     * -----------------------------------------------------------------------------
     */
    double T_dp = 0.; // Dew-Point Temperature
    for (int i = 0; i < componentCount; i++) {
      T_dp += y[i] * T_sat[i];
    }
    
    /*
     * IV. Iterate until Convergence
     * -----------------------------------------------------------------------------
     */
    int iterationCount = 0;
    double error = 0.;
    boolean isIncreasing = true;
    do {
      
      // Step 1. Calculate Vapour Pressures of each Component
      double[] P_sat = new double[componentCount]; //Vapour Pressures
      for (int i = 0; i < componentCount; i++) {
        if (isCondensable[i]) {
          P_sat[i] = Menu.getSpecies(stream.getSpeciesIndex(i)).evaluateVapourPressure(T_dp, false);
        }
        else {
          P_sat[i] = 0.;
        }
      }
      
      // Step 2. Calculate Vapour Pressure of Species j
      double P_j = 0.;
      for (int i = 0; i < componentCount; i++) {
        if (isCondensable[i]) {
          P_j += y[i] * P_sat[j] / P_sat[i];
        }
      }
      P_j = stream.getP() * P_j;
      
      // Step 3. Calculate the New Dew-Point Temperature
      double T_new = Menu.findRoot(
                                   Menu.getSpecies(stream.getSpeciesIndex(j)).getCorrelation(Species.VAPOUR_PRESSURE), 
                                   new double[] {P_j}, 1., true, 
                                   Behaviour.BUBBLE_DEW_POINT_INCREMENT_LENGTH, 
                                   Behaviour.BUBBLE_DEW_POINT_SUB_INCREMENT_FRACTION, 
                                   Behaviour.BUBBLE_DEW_POINT_TOLERANCE, 
                                   Behaviour.BUBBLE_DEW_POINT_MAX_EVALUATION_COUNT, false);
      
      if (T_new > T_dp) {
        isIncreasing = true;
      }
      else {
        isIncreasing = false;
      }
      
      //Step 4. Check Error
      error = Math.abs(T_dp - T_new); // Calculate error between new and old T_dp
      T_dp = T_new;
      if (Double.isNaN(T_dp) || Double.isInfinite(T_dp) || T_dp < 0.) {
        break;
      }
      
      iterationCount++;
      if (iterationCount > Behaviour.BUBBLE_DEW_POINT_MAX_EVALUATION_COUNT) {
        throw new TooManyFunctionEvaluationsException("Simple Iteration", 
                                                      "Ideal Dew-Point Temperature", null, null);
      }
      
    } while (error > Behaviour.BUBBLE_DEW_POINT_TOLERANCE);
    
    /*
     * V. Verify Integrity of Bubble-Point Temperature
     * -----------------------------------------------------------------------------
     */
    if (Double.isNaN(T_dp) || Double.isInfinite(T_dp) || T_dp < 0.) {
      if (isIncreasing || T_dp > 0.) {
        T_dp = Double.MAX_VALUE;
      }
      else {
        T_dp = 0.;
      }
    }
    
    return T_dp;
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 9) calculatePartitionCoefficients() : Calculates and returns the partition coefficients of the stream via
    *           Raoult's Law, where K = y/x.
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public double[] calculatePartitionCoefficients(Stream stream) throws FunctionException {
    
    int componentCount = stream.getComponentCount();
    double[] K = new double[componentCount];
    for (int i = 0; i < componentCount; i++) {
      if (stream.isComponentCondensable(i)) {
        K[i] = Menu.getSpecies(stream.getSpeciesIndex(i))
          .evaluateVapourPressure(stream.getT(), false) / stream.getP();
      }
      else {
        K[i] = 0.; // Partition coefficients of non-condensable components are set to 0
      }
    }
    
    return K;
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 10) evaluateStreamEnthalpy() : Calculates the enthalpy of the stream.
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public double evaluateStreamEnthalpy(double Tref, Stream stream, boolean derivative) 
    throws FunctionException {
    return stream.evaluateStreamEnthalpy(Tref, derivative);
  }
  /*********************************************************************************************************************/
  
}