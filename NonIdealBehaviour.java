public class NonIdealBehaviour extends Behaviour {
  
  public static final double PRESSURE_TOLERANCE = 0.01;
  public static final double GAMMA_TOLERANCE = 0.01;
  public static final double VAPOUR_FRACTION_TOLERANCE = 0.001;
  public static final double X_Y_TOLERANCE = 0.001;
  
  
  /**********************************************************************************************************************
    * 1) clone()
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public NonIdealBehaviour clone() {
    return new NonIdealBehaviour();
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
    
    int componentCount = flashStream.getComponentCount(); // Number of Components
    
    double[] gamma_bp = new double[componentCount]; // Bubble-Point Activity Coefficients
    double[] gamma_dp = new double[componentCount]; // Dew-Point Activity Coefficients
    double[] phi_bp = new double[componentCount]; // Bubble-Point Fugacity Coefficients
    double[] phi_dp = new double[componentCount];  // Dew-Point Fugacity Coefficients
    
    double P_bp = this.calculateBubblePointPressure(flashStream.clone(), gamma_bp, phi_bp); // Bubble-Point Pressure
    double P_dp = this.calculateDewPointPressure(flashStream.clone(), gamma_dp, phi_dp); // Dew-Point Pressure
    
    // Check if the tank pressure is within the bubble-point/dew-point range
    if (flashStream.getP() > P_bp || flashStream.getP() < P_dp 
          || Math.abs(P_bp - P_dp) < Behaviour.BUBBLE_DEW_POINT_TOLERANCE) {
      
      throw new FlashCalculationException(flashStream.getT(), flashStream.getP(), P_bp, P_dp, 0., 0.);
    }
    flashStream.setP_bp(P_bp);
    flashStream.setP_dp(P_dp);
    
    
    /*
     * II. Calculate Initial Values
     * -----------------------------------------------------------------------------
     */
    // Store Condensable Mole Fractions
    double[] z = new double[componentCount];
    for (int i = 0; i < componentCount; i++) {
      if (flashStream.isComponentCondensable(i)) {
        z[i] = flashStream.getZi(i) / flashStream.getCondensableFraction();
      }
      else {
        z[i] = 0;
      }
    }
    
    // Interpolate between the bubble-point and dew-point values of the vapour fraction, the activity 
    // coefficients and the fugacity coefficients to obtain initial estimates for these same quantities
    // at the tank pressure.
    double interpolate = (flashStream.getP() - P_dp) / (P_bp - P_dp); // Interpolation Factor
    
    double vapourFraction = (P_bp - flashStream.getP()) / (P_bp - P_dp); // Vapour Fraction
    
    double[] gamma = new double[componentCount]; // Activity Coefficients at the Tank Pressure
    double[] phi = new double[componentCount]; // Fugacity Coefficients at the Tank Pressure
    for (int i = 0; i < componentCount; i++) {
      gamma[i] = gamma_dp[i] + (gamma_bp[i] - gamma_dp[i]) * interpolate;
      phi[i] = phi_dp[i] + (phi_bp[i] - phi_dp[i]) * interpolate;
    }
    flashStream.setGamma(gamma);
    flashStream.setPhi(phi);
    
    /*
     * III. Iterate until Convergence
     * -----------------------------------------------------------------------------
     */
    
    boolean criteria = false; // When true, v {x} and {y} have converged, and the loop may end
    double[] K = new double[componentCount]; // Partition Coefficients
    while (!criteria) {
      
      // Step 1. Calculate the Partition Coefficients
      K = calculatePartitionCoefficients(flashStream);
      flashStream.setK(K);
      
      // Step 2. Calculate the Vapour Fraction
      RachfordRice rachfordRice = new RachfordRice(z, K, flashStream.getIsCondensable());
      double newVapourFraction = 0.; // New Vapour Fraction
      newVapourFraction = Menu.findRoot(rachfordRice, null, 0., 1., 
                                        Behaviour.RACHFORD_RICE_INCREMENT_LENGTH,
                                        BracketingRootFinder.DEFAULT_SUB_INCREMENT_FRACTION, 
                                        Behaviour.RACHFORD_RICE_TOLERANCE,
                                        Behaviour.RACHFORD_RICE_MAX_EVALUATION_COUNT);
      
      criteria = true;
      
      // Verify whether the vapour fraction has converged
      flashStream.setVapourFraction(newVapourFraction);
      if (Math.abs(newVapourFraction - vapourFraction) > NonIdealBehaviour.VAPOUR_FRACTION_TOLERANCE) {
        criteria = false; // Vapour fraction has not converged
      } 
      vapourFraction = newVapourFraction;
      
      // Step 3. Calculate Phase Mole Fractions
      for (int i = 0; i < componentCount; i++) {
        if (flashStream.isComponentCondensable(i)) {
          double x = z[i] / (1 + (K[i] - 1) * vapourFraction);
          double y = x * K[i];
          
          // Verify whether x[i] has converged
          if (Math.abs(x - flashStream.getXi(i)) > NonIdealBehaviour.X_Y_TOLERANCE) {
            criteria = false; // x[i] has not converged
          }
          // Verify whether y[i] has converged
          if (Math.abs(y - flashStream.getYi(i)) > NonIdealBehaviour.X_Y_TOLERANCE) {
            criteria = false; // y[i] has not converged
          }
          
          flashStream.setXi(x, i);
          flashStream.setYi(y, i);
        } else {
          flashStream.setXi(0, i);
          flashStream.setYi(0, i);
        }
      }
      
      // Step 4. Calculate Activity and Fugacity Coefficients
      gamma = calculateActivityCoefficients(flashStream, false);
      phi = calculateFugacityCoefficients(flashStream, false);
    }
    
    return flashStream.clone();
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 3) calculateBubblePointPressure() : Calculates the bubble point pressure of a stream.
    *           The gamma and phi arguments may be passed by reference if the fugacity
    *           and activity coefficients need to be retrieved.  
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public double calculateBubblePointPressure(Stream stream, double[] gamma_bp, double[] phi_bp) 
    throws NumericalMethodException, FunctionException {
    
    /*
     * I. Initialize Arrays
     * -----------------------------------------------------------------------------
     */
    int componentCount = stream.getComponentCount(); // Number of Components
    double P_bp = 0.; // Bubble-Point Pressure
    double[] phi = new double[componentCount]; // Fugacity Coefficients
    double[] gamma = calculateActivityCoefficients(stream, true); // Activity Coefficients
    double[] y = new double[componentCount]; // Condensable Mole Fractions
    double[] P_sat = new double[componentCount]; // Vapour Pressures
    
    /*
     * II. Calculate Vapour Pressures
     * -----------------------------------------------------------------------------
     */
    for (int i = 0; i < componentCount; i++) {
      phi[i] = 1.; // Set all phi = 1
      
      // Calculate the vapour Pressure of each condensable component
      if (stream.isComponentCondensable(i)) {
        P_sat[i] = Menu.getSpecies(stream.getSpeciesIndex(i)).evaluateVapourPressure(stream.getT(), false);
      }
      else {
        P_sat[i] = 0.;
      }
    }
    
    /*
     * III. Calculate Initial Estimate of Bubble-Point Pressure
     * -----------------------------------------------------------------------------
     */
    for (int i = 0; i < componentCount; i++) {
      if (stream.isComponentCondensable(i)) {
        P_bp += (stream.getZi(i) / stream.getCondensableFraction()) * gamma[i]
          * P_sat[i] / phi[i];
      }
    }
    
    /*
     * IV. Iterate until Convergence
     * -----------------------------------------------------------------------------
     */
    int iterationCount = 0;
    double error = 0.;
    boolean isIncreasing = true;
    do {
      
      // Step 1. Calculate Vapour Phase Mole Fractions, {y} 
      for (int i = 0; i < componentCount; i++) {
        y[i]= ((stream.getZi(i) / stream.getCondensableFraction()) * gamma[i] * P_sat[i]) 
          / (phi[i] * P_bp);
        stream.setYi(y[i], i);
        stream.setP(P_bp);
      }
      
      // Step 2. Calculate Fugacity Coefficients at {y}
      phi = calculateFugacityCoefficients(stream, false);
      
      // Step 3. Calculate New Bubble-Point Pressure
      double P_new = 0.;
      for (int i = 0; i < componentCount; i++) {
        if (stream.isComponentCondensable(i)) {
          P_new += ((stream.getZi(i) / stream.getCondensableFraction()) * gamma[i]
                      * P_sat[i]) / phi[i];
        }
      }
      
      if (P_new > P_bp) {
        isIncreasing = true;
      }
      else {
        isIncreasing = false;
      }
      
      // Step 4. Check Error
      error = Math.abs(P_new - P_bp);
      P_bp = P_new;
      if (Double.isNaN(P_bp) || Double.isInfinite(P_bp) || P_bp <= 0.) {
        break;
      }
      
      iterationCount++;
      if (iterationCount > Behaviour.BUBBLE_DEW_POINT_MAX_EVALUATION_COUNT) {
        throw new TooManyFunctionEvaluationsException("Simple Iteration", 
                                                      "Non-Ideal Bubble-Point Pressure", null, null);
      }
      
    } while (error > NonIdealBehaviour.PRESSURE_TOLERANCE);
    
    if (Double.isNaN(P_bp) || Double.isInfinite(P_bp) || P_bp < 0.) {
      if (isIncreasing || P_bp > 0.) {
        P_bp = Double.MAX_VALUE;
      }
      else {
        P_bp = 0.;
      }
    }
    
    if (gamma_bp != null && gamma_bp.length == componentCount) {
      for (int i = 0; i < componentCount; i++) {
        gamma_bp[i] = gamma[i];
      }
    }
    if (phi_bp != null && phi_bp.length == componentCount) {
      for (int i = 0; i < componentCount; i++) {
        phi_bp[i] = phi[i];
      }
    }
    
    return P_bp;
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 4) calculateDewPointPressure() : Calculates the dew point pressure of a stream.
    *          The gamma and phi arguments may be passed by reference if the fugacity
    *          and activity coefficients need to be retrieved.
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public double calculateDewPointPressure(Stream stream, double[] gamma_dp, double[] phi_dp) 
    throws NumericalMethodException, FunctionException {
    
    /*
     * I. Initialize Arrays
     * -----------------------------------------------------------------------------
     */
    int componentCount = stream.getComponentCount();
    double P_dp = 0.;
    double[] x = new double[componentCount];
    double[] y = new double[componentCount];
    double[] phi = new double[componentCount];
    double[] gamma = new double[componentCount];
    double[] P_sat = new double[componentCount];
    boolean[] isCondensable = new boolean[componentCount];
    
    /*
     * II. Calculate Vapour Pressures
     * -----------------------------------------------------------------------------
     */
    for (int i = 0; i < componentCount; i++) {
      if (stream.isComponentCondensable(i)) {
        y[i] = stream.getZi(i) / stream.getCondensableFraction();
        P_sat[i] = Menu.getSpecies(stream.getSpeciesIndex(i)).evaluateVapourPressure(stream.getT(), 
                                                                                     false);
      }
      else {
        y[i] = 0.;
        P_sat[i] = 0.;
      }
      gamma[i] = 1.; // Set all gamma = 1
      phi[i] = 1.; //  Set all phi = 1
      isCondensable[i] = stream.isComponentCondensable(i);
    }
    
    /*
     * II. Initialize Arrays
     * -----------------------------------------------------------------------------
     */
    int iterationCount = 0;
    double error = 0.;
    boolean isIncreasing = true;
    do {
      // Only perform 
      if (iterationCount > 0) {
        
        // Step 1. Calculate Fugacity Coefficients at {y} 
        if (iterationCount > 1) {
          // Only perform Step 1 after the first and second iterations
          phi = this.calculateFugacityCoefficients(stream, true);
        }
        
        // Step 2. Calculate Liquid-Phase Mole Fractions, {x} 
        double sumOfX = 0.;
        for (int i = 0; i < componentCount; i++) {
          if (isCondensable[i]) {
            x[i] = y[i] * phi[i] * stream.getP() / (gamma[i] * P_sat[i]);
            sumOfX += x[i];
          }
          else {
            x[i] = 0.;
          }
        }
        
        // Step 3. Calculate Activity Coefficients
        if (iterationCount > 1) {
          
          // Only normalize {x} and check for {gamma} convergence after the 
          // first and second iterations
          
          // Step 4.a) Calculate Activity Coefficients until Convergence
          boolean gammaWithinTolerance = false;
          while (!gammaWithinTolerance) {
            for (int i = 0; i < componentCount; i++) {
              stream.setXi(x[i] / sumOfX, i); // Normalize {x}
            }
            
            double[] gamma_new = this.calculateActivityCoefficients(stream, false);
            
            // Check if error is within tolerance for each activity coefficient
            gammaWithinTolerance = true;
            for (int i = 0; i < componentCount; i++) {
              if (Math.abs(gamma_new[i] - gamma[i]) > NonIdealBehaviour.GAMMA_TOLERANCE) {
                gammaWithinTolerance = false;
              }
              gamma[i] = gamma_new[i];
            }
          }
        }
        else {
          // Perform during the second iteration 
          for (int i = 0; i < componentCount; i++) {
            stream.setXi(x[i], i); //  do not normalize {x}
          }
          gamma = this.calculateActivityCoefficients(stream, false);
        }
      }
      
      // Step 4. Calculate New Dew-Point Pressure
      double P_new = 0.;
      for (int i = 0; i < componentCount; i++) {
        if (isCondensable[i]) {
          if (P_sat[i] != 0. && stream.isComponentCondensable(i)) {
            P_new += (y[i] * phi[i]) / (gamma[i] * P_sat[i]);
          }
        }
      }
      if (P_new != 0.) {
        P_new = 1. / P_new;
      }
      
      if (P_new > P_dp) {
        isIncreasing = true;
      }
      else {
        isIncreasing = false;
      }
      
      // Step 5. Check Error 
      error = Math.abs(P_new - P_dp);
      P_dp = P_new;
      if (Double.isNaN(P_dp) || Double.isInfinite(P_dp) || P_dp < 0.) {
        break;
      }
      stream.setP(P_dp);
      
      iterationCount++;
      if (iterationCount > Behaviour.BUBBLE_DEW_POINT_MAX_EVALUATION_COUNT) {
        throw new TooManyFunctionEvaluationsException("Simple Iteration", 
                                                      "Non-Ideal Dew-Point Pressure", null, null);
      }
      
    } while (error > NonIdealBehaviour.PRESSURE_TOLERANCE || iterationCount < 3);
    
    /*
     * III. Verify Integrity of Dew-Point Pressure
     * -----------------------------------------------------------------------------
     */
    if (Double.isNaN(P_dp) || Double.isInfinite(P_dp) || P_dp < 0.) {
      if (isIncreasing || P_dp > 0.) {
        P_dp = Double.MAX_VALUE;
      }
      else {
        P_dp = 0.;
      }
    }
    
    if (gamma_dp != null && gamma_dp.length == componentCount) {
      for (int i = 0; i < componentCount; i++) {
        gamma_dp[i] = gamma[i];
      }
    }
    if (phi_dp != null && phi_dp.length == componentCount) {
      for (int i = 0; i < componentCount; i++) {
        phi_dp[i] = phi[i];
      }
    }
    
    return P_dp;
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 5) calculateBubblePointTemperature() : Calculates the bubble point temperature of a stream.
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public double calculateBubblePointTemperature(Stream stream) 
    throws NumericalMethodException, FunctionException {
    
    /*
     * I. Initialize Arrays
     * -----------------------------------------------------------------------------
     */
    int componentCount = stream.getComponentCount(); // Number of Components
    boolean[] isCondensable = stream.getIsCondensable();
    double[] x = new double[componentCount]; // Liquid-Phase Mole Fractions
    double[] y = new double[componentCount]; // Vapour-Phase Mole Fractions
    double[] gamma = new double[componentCount]; // Activity Coefficients
    double[] phi = new double[componentCount]; // Fugacity Coefficients
    int j = -1; // Index of First Condensable Component
    
    for (int i = 0; i < componentCount; i++) {
      if (stream.isComponentCondensable(i)) {
        x[i] = stream.getZi(i) / stream.getCondensableFraction(); // Store overall condensable mole fractions
        if (j < 0) {
          j = i; // Set Species i as Species j
        }
      }
      gamma[i] = 1.; // Set all gamma = 1
      phi[i] = 1.; // Set all phi = 1
    }
    // If j is still equal to -1, then no condensable components are present in the stream
    if (j == -1) {
      return 0.; // Return 0 to force vapour-phase outlet
    }
    
    /*
     * II. Calculate Saturation Temperatures
     * -----------------------------------------------------------------------------
     */
    double[] T_sat = new double[componentCount]; // Saturation Temperatures
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
          T_sat[i] = 0.0; //assume this species has no contribution to VLE
        }
      }
      else {
        T_sat[i] = 0.;
      }
    }
    
    /*
     * III. Calculate Initial Estimate of Bubble-Point Temperature
     * -----------------------------------------------------------------------------
     */
    double T_bp = 0.;
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
      
      // Step 1. Calculate Vapour Pressures
      double[] P_sat = new double[componentCount]; // Vapour Pressures
      for (int i = 0; i < componentCount; i++) {
        if (isCondensable[i]) {
          P_sat[i] = Menu.getSpecies(stream.getSpeciesIndex(i)).evaluateVapourPressure(T_bp, false);
        }
        else {
          P_sat[i] = 0.;
        }
      }
      
      // Only perform Steps 2 and 3 after the first iteration
      stream.setT(T_bp, false, false);
      if (iterationCount > 0) {
        
        // Step 2. Calculate the Vapour-Phase Mole Fractions, {y}
        for (int i = 0; i < componentCount; i++) {
          y[i] = x[i] * gamma[i] * P_sat[i] / (phi[i] * stream.getP());
          stream.setYi(y[i], i);
        }
        
        // Step 3. Calculate Fugacity Coefficients at {y}
        phi = this.calculateFugacityCoefficients(stream, false);
      }
      
      // Step 4. Calculate Activity Coefficients
      gamma = this.calculateActivityCoefficients(stream, true);
      
      // Step 5. Calculate Vapour Pressure of Species j
      double P_j = 0.;
      for (int i = 0; i < componentCount; i++) {
        if (isCondensable[i]) {
          P_j += (x[i] * gamma[i] / phi[i]) * (P_sat[i] / P_sat[j]);
        } 
      }
      if (P_j != 0) {
        P_j = stream.getP() / P_j; 
      }
      
      // Step 6. Calculate New Bubble-Point Temperature
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
      
      // Step 7. Check Error
      error = Math.abs(T_bp - T_new);
      T_bp = T_new;
      if (Double.isNaN(T_bp) || Double.isInfinite(T_bp) || T_bp < 0.) {
        break;
      }
      
      iterationCount++;
      if (iterationCount > Behaviour.BUBBLE_DEW_POINT_MAX_EVALUATION_COUNT) {
        throw new TooManyFunctionEvaluationsException("Simple Iteration", 
                                                      "Non-Ideal Bubble-Point Temperature", null, null);
      }
      
    } while (error > Behaviour.BUBBLE_DEW_POINT_TOLERANCE || iterationCount == 1);
    
    /*
     * V. Verify Integrity of Bubble-Point Temperature
     * ----------------------------------------------------------------------------
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
    * 6) calculateDewPointTemperature() : Calculates the dew point temperature of a stream.
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
    double[] x = new double[componentCount]; // Liquid-Phase Mole Fractions
    double[] y = new double[componentCount]; // Vapour-Phase Mole Fractions
    double[] gamma = new double[componentCount]; // Activity Coefficients
    double[] phi = new double[componentCount];  // Fugacity Coefficients
    int j = -1; // Index of the First Condensable Component
    
    for (int i = 0; i < componentCount; i++) {
      if (stream.isComponentCondensable(i)) {
        y[i] = stream.getZi(i) / stream.getCondensableFraction(); // Store overall condensable mole fractions
        if (j < 0) {
          j = i;
        }
      }
      gamma[i] = 1.; // Set all gamma = 1
      phi[i] = 1.; // Set all phi = 1
    }
    // If j is still equal to -1, then no condensable components are present
    if (j == -1) {
      return 0.; // Return 0 to force vapour-phase outlet
    }
    
    /*
     * II. Calculate Saturation Temperatures
     * -----------------------------------------------------------------------------
     */
    double[] T_sat = new double[componentCount]; 
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
     * III. Calculate Initial Estimate of Dew-Point Temperature
     * -----------------------------------------------------------------------------
     */
    double T_dp = 0.;
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
      
      // Step 1. Calculate Vapour Pressures
      double[] P_sat = new double[componentCount]; // Vapour Pressures
      for (int i = 0; i < componentCount; i++) {
        if (isCondensable[i]) {
          P_sat[i] = Menu.getSpecies(stream.getSpeciesIndex(i)).evaluateVapourPressure(T_dp, false);
        }
        else {
          P_sat[i] = 0.;
        }
      }
      
      // Only perform Steps 2, 3 and 4 after the first iteration 
      stream.setT(T_dp, false, false);
      if (iterationCount > 0) {
        
        // Step 2. Calculate Fugacity Coefficients 
        phi = this.calculateFugacityCoefficients(stream, true);
        
        // Step 3. Calculate Liquid-Phase Mole Fractions, {x}
        double sumOfX = 0.;
        for (int i = 0; i < componentCount; i++) {
          if (isCondensable[i]) {
            x[i] = y[i] * phi[i] * stream.getP() / (gamma[i] * P_sat[i]);
            sumOfX += x[i];
          }
          else {
            x[i] = 0.;
          }
        }
        
        // Step 4. Calculate Activity Coefficients
        if (iterationCount > 1) {
          
          // Only normalize {x} and check for {gamma} convergence after the first and second 
          // iterations
          
          // Step 4.a) Calculate Activity Coefficients until Convergence
          boolean gammaWithinTolerance = false;
          while (!gammaWithinTolerance) {
            for (int i = 0; i < componentCount; i++) {
              stream.setXi(x[i] / sumOfX, i); // Normalize {x}
            }
            
            double[] gamma_new = new double[componentCount];
            gamma_new = this.calculateActivityCoefficients(stream, false);
            
            gammaWithinTolerance = true;
            for (int i = 0; i < componentCount; i++) {
              if (Math.abs(gamma_new[i] - gamma[i]) > NonIdealBehaviour.GAMMA_TOLERANCE) {
                gammaWithinTolerance = false;
              }
              gamma[i] = gamma_new[i];
            }
          }
        }
        else {
          //Second iteration only
          
          for (int i = 0; i < componentCount; i++) {
            stream.setXi(x[i], i); // do not normalize {x}
          }
          gamma = this.calculateActivityCoefficients(stream, false);
        }
      }
      
      // Step 5. Calculate Vapour Pressure of Component j
      double P_j = 0.;
      for (int i = 0; i < componentCount; i++) {
        if (isCondensable[i]) {
          P_j += (y[i] * phi[i] / gamma[i]) * (P_sat[j] / P_sat[i]);
        }
      }
      P_j = stream.getP() * P_j;
      
      // Step 6. Calculate New Dew-Point Temperature
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
      
      // Step 7. Check Error
      error = Math.abs(T_dp - T_new);
      T_dp = T_new;
      if (Double.isNaN(T_dp) || Double.isInfinite(T_dp) || T_dp < 0.) {
        break;
      }
      
      iterationCount++;
      if (iterationCount > Behaviour.BUBBLE_DEW_POINT_MAX_EVALUATION_COUNT) {
        throw new TooManyFunctionEvaluationsException("Simple Iteration", 
                                                      "Non-Ideal Dew-Point Temperature", null, null);
      }
      
    } while (error > Behaviour.BUBBLE_DEW_POINT_TOLERANCE || iterationCount < 3);
    
    /*
     * V. Verify Integrity of Dew-Point Pressure
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
    * 7) calculateActivityCoefficients() : Returns the activity coefficients of all components in the same order 
    *           as they are in the stream.
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public double[] calculateActivityCoefficients(Stream stream, boolean bubblePoint) {
    
    /*
     * I. Initialize Arrays and Constants
     * -----------------------------------------------------------------------------
     */
    int componentCount = stream.getComponentCount(); // Number of Components
    int subGroupTypeCount = Menu.getSubGroupTypeCount(); // Number of Sub-Groups Types
    
    int v[][] = new int[componentCount][subGroupTypeCount]; // Number of a Sub-Group Type within a Species 
    double r[] = new double[componentCount];
    double q[] = new double[componentCount];
    double e[][] = new double[subGroupTypeCount][componentCount];
    double tau[][] = new double[subGroupTypeCount][subGroupTypeCount];
    double beta[][] = new double[componentCount][subGroupTypeCount];
    double theta[] = new double[subGroupTypeCount];
    double s[] = new double[subGroupTypeCount];
    double J[] = new double[componentCount];
    double L[] = new double[componentCount];
    double[] lnGamma_c = new double[componentCount];
    double[] lnGamma_r = new double[componentCount];
    double[] gamma = new double[componentCount]; // Activity Coefficients
    
    double[] x = new double[componentCount];
    if (bubblePoint) {
      x = stream.getZ();
    }
    else {
      x = stream.getX();
    }
    
    /*
     * II. Apply UNIFAC Method 
     * -----------------------------------------------------------------------------
     */
    
    /* Indices
     *    i := runs over all components 
     *    j := runs over all components
     *    k := runs over all sub-groups
     *    m := runs over all sub-groups
     */
    
    // Step 1. Calculate r[i], q[i] and v[i][k]
    for (int i = 0; i < componentCount; i++) {
      for (int k = 0; k < subGroupTypeCount; k++) {
        int subGroupIndex = Menu.getSubGroupIndex(k);
        v[i][k] = Menu.getSpecies(stream.getSpeciesIndex(i)).getSubGroupCount(subGroupIndex);
        
        r[i] += v[i][k] * Menu.getSubGroupR(subGroupIndex);
        q[i] += v[i][k] * Menu.getSubGroupQ(subGroupIndex);
      }
    }
    
    // Step 2. Calculate e[k][i] 
    for (int k = 0; k < subGroupTypeCount; k++) {
      int subGroupIndex = Menu.getSubGroupIndex(k);
      for (int i = 0; i < componentCount; i++) {
        if (q[i] != 0) {
          e[k][i] = v[i][k] * Menu.getSubGroupQ(subGroupIndex) / q[i];
        }
        else {
          e[k][i] = 0.;
        }
      }
    }
    
    // Step 3. Calculate tau[m][k]
    for (int m = 0; m < subGroupTypeCount; m++) {
      for (int k = 0; k < subGroupTypeCount; k++) {
        tau[m][k] = Math.exp(-Menu.getInteractionParameter(m, k) / stream.getT());
      }
    }
    
    // Step 4. Calculate beta[i][k]
    for (int i = 0; i < componentCount; i++) {
      for (int k = 0; k < subGroupTypeCount; k++) {
        beta[i][k] = 0.;
        for (int m = 0; m < subGroupTypeCount; m++) {
          beta[i][k] += e[m][i] * tau[m][k];
        }
      }
    }
    
    // Step 5. Calculate theta[k] */
    for (int k = 0; k < subGroupTypeCount; k++) {
      double num = 0;
      double dem = 0;
      for (int i = 0; i < componentCount; i++) {
        num += x[i] * q[i] * e[k][i];
        dem += x[i] * q[i];
      }
      if (dem != 0) {
        theta[k] = num / dem;
      }
      else {
        theta[k] = 0.;
      }
    }
    
    // Step 6. Calculate s[k]
    for (int k = 0; k < subGroupTypeCount; k++) {
      for (int m = 0; m < subGroupTypeCount; m++) {
        s[k] += theta[m] * tau[m][k];
      }
    }
    
    // Step 7. Calculate average r and q values
    double r_avg = 0;
    double q_avg = 0;
    for (int j = 0; j < componentCount; j++) {
      r_avg += r[j] * x[j];
      q_avg += q[j] * x[j];
    }
    
    // Step 8. Calculate J[i] and L[i]
    for (int i = 0; i < componentCount; i++) {
      if (r_avg != 0.) {
        J[i] = r[i] / r_avg;
      }
      else {
        J[i] = 0.;
      }
      
      if (q_avg != 0.) {
        L[i] = q[i] / q_avg;
      }
      else {
        L[i] = 0.;
      }
    }
    
    
    // Step 9. Calculate ln(gamma)_c[i], ln(gamma)[i], and gamma[i]
    for (int i = 0; i < componentCount; i++) {
      if (J[i] == 0 || L[i] == 0) {
        lnGamma_c[i] = 0.;
        lnGamma_r[i] = 0.;
      } 
      else {
        lnGamma_c[i] = 1 - J[i] + Math.log(J[i]) 
          - 5 * q[i] * (1 - (J[i] / L[i]) + Math.log(J[i] / L[i]));
        
        double lnGamma_rSum = 0;
        for (int k = 0; k < subGroupTypeCount; k++) {
          if (beta[i][k] != 0. && s[k] != 0.) {
            lnGamma_rSum += theta[k] * (beta[i][k] / s[k]) - e[k][i] * Math.log(beta[i][k] / s[k]);
          }
        }
        lnGamma_r[i] = q[i] * (1 - lnGamma_rSum);
      }
      
      gamma[i] = Math.exp(lnGamma_c[i] + lnGamma_r[i]);
    }
    
    return gamma;
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 8) calculateFugacityCoefficients() : Returns the pure species fugacity coefficients of all components in 
    *           the same order as they are in the stream.
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public double[] calculateFugacityCoefficients(Stream stream, boolean dewPoint) throws FunctionException {
    
    int componentCount = stream.getComponentCount();
    
    /*
     * I. Initialize Arrays
     * -----------------------------------------------------------------------------
     */
    
    double[] y; // Vapour-Phase Mole Fractions, {y}
    double[] P_sat = new double[componentCount]; // Vapour Pressures
    double[] phi = new double[componentCount]; // Fugacity Coefficients
    
    // Dew-Point Calculation: use overall mole fractions
    if (dewPoint) {
      y = stream.getZ();
    }
    
    //Regular Calculate: use given {y}
    else {
      y = stream.getY();
    }
    
    // Store critical mixture properties
    double[][] omega = stream.getOmega_ij();
    double[][] Tc = stream.getTc_ij(); // [K]
    double[][] Pc = stream.getPc_ij(); // [bar]
    
    double[][] Tr = new double[componentCount][componentCount]; // Reduced Temperatures
    
    // Virial Coefficients
    double[][] B0 = new double[componentCount][componentCount];
    double[][] B1 = new double[componentCount][componentCount];
    double[][] B_hat = new double[componentCount][componentCount];
    double[][] B = new double[componentCount][componentCount];
    
    double[][] delta = new double[componentCount][componentCount];
    
    /*
     * II. Calculate Virial Coefficients
     * -----------------------------------------------------------------------------
     */
    
    // Step 1. Calculate Reduced Temperatures
    for (int i = 0; i < componentCount; i++) {
      for (int j = 0; j < componentCount; j++) {
        Tr[i][j] = stream.getT() / Tc[i][j];
      }
    }
    
    // Step 2. Calculate B0[i][j]
    for (int i = 0; i < componentCount; i++) {
      for (int j = 0; j < componentCount; j++) {
        B0[i][j] = 0.083 - (0.422 / Math.pow(Tr[i][j], 1.6));
      }
    }
    
    // Step 3. Calculate B1[i][j] 
    for (int i = 0; i < componentCount; i++) {
      for (int j = 0; j < componentCount; j++) {
        B1[i][j] = 0.139 - (0.172 / Math.pow(Tr[i][j], 4.2));
      }
    }
    
    // Step 4. Calculate Bhat[i][j] 
    for (int i = 0; i < componentCount; i++) {
      for (int j = 0; j < componentCount; j++) {
        B_hat[i][j] = B0[i][j] + omega[i][j] * B1[i][j];
      }
    }
    
    // Step 5. Calculate B[i][j]
    for (int i = 0; i < componentCount; i++) {
      for (int j = 0; j < componentCount; j++) {
        if (i != j) {
          // P = [bar], T = [K], B = [cm^3/mol], R = 83.14
          B[i][j] = (B_hat[i][j] * 10.*Menu.GAS_CONSTANT * Tc[i][j]) / Pc[i][j];
        }
        else {
          B[i][j] = 0.;
        }
      }
    }
    
    // Step 6. Calculate delta[i][j]
    for (int i = 0; i < componentCount; i++) {
      for (int j = 0; j < componentCount; j++) {
        delta[i][j] = 2 * B[i][j] - B[i][i] - B[j][j];
      }
    }
    
    /*
     * III. Calculate Vapour Pressures
     * -----------------------------------------------------------------------------
     */
    for (int i = 0; i < componentCount; i++) {
      if (stream.isComponentCondensable(i)) {
        P_sat[i] = Menu.getSpecies(stream.getSpeciesIndex(i)).evaluateVapourPressure(stream.getT(), 
                                                                                     false);
      }
      else {
        P_sat[i] = 0;
      }
    }
    
    /*
     * IV. Calculate Fugacity Coefficients
     * -----------------------------------------------------------------------------
     */
    for (int i = 0; i < componentCount; i++) {
      double outerSum = 0.;
      
      for (int j = 0; j < componentCount; j++) {
        double innerSum = 0.;
        
        for (int k = 0; k < componentCount; k++) {
          innerSum += y[j] * y[k] * (2 * delta[j][i] - delta[j][k]);
        }
        
        outerSum += innerSum;
      }
      
      // P and P_sat = [bar], T = [K], delta and B = [cm^3/mol], R = 83.14
      phi[i] = Math.exp((B[i][i] * (stream.getP() - P_sat[i]) + 0.5 * stream.getP() * outerSum)
                          / (10. * Menu.GAS_CONSTANT * stream.getT()));
    }
    
    return phi;
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 9) calculatePartitionCoefficients() : Calculates the partition coefficients of the stream via 
    *           Modified Raoult's Law, where K = y/x.
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public double[] calculatePartitionCoefficients(Stream stream) throws FunctionException {
    
    int componentCount = stream.getComponentCount();
    double[] K = new double[componentCount];
    double[] gamma = stream.getGamma();
    double[] phi = stream.getPhi();
    for (int i = 0; i < componentCount; i++) {
      if (stream.isComponentCondensable(i)) {
        K[i] = (gamma[i]
                  * Menu.getSpecies(stream.getSpeciesIndex(i)).evaluateVapourPressure(stream.getT(), false))
          / (phi[i] * stream.getP());
      }
      else {
        K[i] = 0.; // Partition coefficients of non-condensable components are set to 0
      }
    }
    
    return K;
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 10) evaluateStreamEnthalpy() : Evaluates the enthalpy of the stream.
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public double evaluateStreamEnthalpy(double Tref, Stream stream, boolean derivative) throws FunctionException {
    return stream.evaluateStreamEnthalpy(Tref, derivative);
  }
  /*********************************************************************************************************************/
  
}