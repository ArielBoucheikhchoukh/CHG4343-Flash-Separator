public class RiddersMethodRootFinder extends BracketingRootFinder {
  
  /**********************************************************************************************************************
    * 1.1) Constructor A
    * ----------------------------------------------------------------------------------------------------------------------
    */
  public RiddersMethodRootFinder(double endPoint, double incrementLength, double subIncrementFraction, 
                                 double maxEvaluationCount) {
    super("Ridders Method Root Finder", endPoint, incrementLength, subIncrementFraction, maxEvaluationCount);
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 1.2) Constructor B
    * ----------------------------------------------------------------------------------------------------------------------
    */
  public RiddersMethodRootFinder(double incrementLength, double subIncrementFraction, 
                                 boolean positiveDirection, double maxEvaluationCount, boolean useFunctionBounds) {
    super("Ridders Method Root Finder", incrementLength, subIncrementFraction, positiveDirection, maxEvaluationCount, 
          useFunctionBounds);
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 2) Copy Constructor
    * ----------------------------------------------------------------------------------------------------------------------
    */
  public RiddersMethodRootFinder(RiddersMethodRootFinder source) {
    super(source);
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 3) clone()
    * ----------------------------------------------------------------------------------------------------------------------
    */
  public RiddersMethodRootFinder clone() {
    return new RiddersMethodRootFinder(this);
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 4) rootFindingMethod() : Finds and returns a root of function f.
    * ----------------------------------------------------------------------------------------------------------------------
    */
  protected double rootFindingMethod(Function f, double[] constants, double startPoint,
                                     double tolerance) throws NumericalMethodException, FunctionException {
    
    double xR = 0.; // Root
    double endBound = startPoint;
    boolean foundRoot = false; // true if a critical point has been found
    
    /*
     * Loop 1: Search the current search increment for the unique critical point
     * ----------------------------------------------------------------------------
     */
    do {
      
      // Calculate the bounds of the search increment. 
      double[] bounds = super.incrementalSearch(f, constants, endBound, tolerance); 
      
      double xL = bounds[0]; // Lower Bound
      double xU = bounds[1]; // Upper Bound
      double xR_old = 0.;
      
      // Set the endBound of the search increment to the endBound of the returned search increment 
      if (super.getDirection() == 1) {
        endBound = xU;
      } else {
        endBound = xL;
      }
      
      try {
        double f_xL = f.evaluate(xL, constants);
        double f_xU = f.evaluate(xU, constants);
        double f_xR = 0;
        super.setEvaluationCount(super.getEvaluationCount() + 2);
        
        double error = 0.;
        int iterationCount = 0;
        
        /*
         * Loop 2: Calculate the Root (xR) of the Current Increment
         * ----------------------------------------------------------------------------
         */
        do {
          super.checkEvaluationCount(f); // Check number of times f has been evaluated
          
          // Step 1. Mid-Point
          double xM = 0.5 * (xL + xU); // Mid-point x-value
          double f_xM = f.evaluate(xM, constants); // f(xM)
          
          // Step 2. Root
          // Calculate xR via the Ridders method-specific equation
          xR = xM + (xM - xL) * Math.signum(f_xL - f_xU) * f_xM 
            / Math.sqrt(Math.pow(f_xM, 2) - f_xL * f_xU);
          f_xR = f.evaluate(xR, constants); // f(xR)
          
          // Step 3. Check Error
          error = Math.abs(xR - xR_old);
          xR_old = xR;
          super.setEvaluationCount(super.getEvaluationCount() + 2);
          
          // Step 4. Adjust Upper and Lower Bounds to
          // only perfom this step if the error is not within tolerance
          if (error > tolerance || iterationCount == 0) {
            // Case A : The root is smaller than the mid-point
            if (xR < xM) {
              // Case A.1 : The root is between xL and xR 
              if (Math.signum(f_xL) != Math.signum(f_xR)) {
                xU = xR;
                f_xU = f.evaluate(xU, constants);
              } 
              // Case A.2 : The root is between xR and xM 
              else if (Math.signum(f_xR) != Math.signum(f_xM)) {
                xL = xR;
                f_xL = f.evaluate(xL, constants);
                
                xU = xM;
                f_xU = f.evaluate(xU, constants);
                
                super.setEvaluationCount(super.getEvaluationCount() + 1); // increment evaluation count
              } 
              // Case A.3 : The root is between xM and xU 
              else if (Math.signum(f_xM) != Math.signum(f_xU)) {
                xL = xM;
                f_xL = f.evaluate(xL, constants);
              }
            } 
            // Case B : The root is greater than the mid-point
            else {
              
              // Case B.1 : The root is between xL and xM 
              if (Math.signum(f_xL) != Math.signum(f_xM)) {
                xU = xM;
                f_xU = f.evaluate(xU, constants);
              } 
              // Case B.2 : The root is between xM and xR 
              else if (Math.signum(f_xM) != Math.signum(f_xR)) {
                xL = xM;
                f_xL = f.evaluate(xL, constants);
                
                xU = xR;
                f_xU = f.evaluate(xU, constants);
                
                super.setEvaluationCount(super.getEvaluationCount() + 1); // increment evaluation count
              } 
              // Case B.1 : The root is between xR and xU 
              else if (Math.signum(f_xR) != Math.signum(f_xU)) {
                xL = xR;
                f_xL = f.evaluate(xL, constants);
              }
            }
            
            super.setEvaluationCount(super.getEvaluationCount() + 1); // increment evaluation count
          }
          
          iterationCount++;
          
        } while (error > tolerance || iterationCount == 1); // End of Loop 2
        
        foundRoot = !super.checkForAsymptote(f, constants, xR, tolerance); // Check if the root is an asymptote
      }
      // If f(x) returns an undefind value at any point, then a discontinuity has been found, and Loop 1 must be restarted
      catch (FunctionException e) {
        if (e instanceof UndefinedFunctionException) {
          foundRoot = false;
        } else {
          throw e;
        }
      }
      
    } while (!foundRoot); // End of Loop 1: exit only if a unique root has been found
    
    return xR;
  }
  /*********************************************************************************************************************/
  
}