
public abstract class BracketingRootFinder extends RootFinder{
  
  public static final double DEFAULT_INCREMENT_FACTOR = 100.;
  public static final double DEFAULT_SUB_INCREMENT_FRACTION = 0.1;
  
  private double endPoint; // the x-value at the end of the search range
  private double incrementLength; // the length of an increment within the search range that is searched by incrementalSearch() 
  private double subIncrementFraction; // fraction of the increment
  private int direction; // direction the root finder will search in: 1 for positive direction, -1 for negative direction
  private boolean useFunctionBounds; // true if the root finder will use the function bounds of the function
  
  
  /**********************************************************************************************************************
    * 1.1) Constructor A
    * ----------------------------------------------------------------------------------------------------------------------
    */
  public BracketingRootFinder(String name, double endPoint, double incrementLength, 
                              double subIncrementFraction, double maxEvaluationCount) {
    super(name, maxEvaluationCount);
    this.endPoint = endPoint;
    this.incrementLength = Math.abs(incrementLength);
    this.subIncrementFraction = Math.abs(subIncrementFraction);
    this.direction = 1;
    this.useFunctionBounds = false;
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 1.2) Constructor B
    * ----------------------------------------------------------------------------------------------------------------------
    */
  public BracketingRootFinder(String name, double incrementLength, double subIncrementFraction, 
                              boolean positiveDirection, double maxEvaluationCount, boolean useFunctionBounds) {
    super(name, maxEvaluationCount);
    this.incrementLength = Math.abs(incrementLength);
    this.subIncrementFraction = Math.abs(subIncrementFraction);
    
    if (positiveDirection) {
      this.direction = 1;
      this.endPoint = Double.MAX_VALUE;
    } else {
      this.direction = -1;
      this.endPoint = Double.MIN_VALUE;
    }
    
    this.useFunctionBounds = useFunctionBounds;
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 2) Copy Constructor
    * ----------------------------------------------------------------------------------------------------------------------
    */
  public BracketingRootFinder(BracketingRootFinder source) {
    super(source);
    this.endPoint = source.endPoint;
    this.incrementLength = source.incrementLength;
    this.subIncrementFraction = source.subIncrementFraction;
    this.direction = source.direction;
    this.useFunctionBounds = source.useFunctionBounds;
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 3) clone()
    * ----------------------------------------------------------------------------------------------------------------------
    */
  public abstract BracketingRootFinder clone();
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 4) findRoot() : Checks the integrity of the parameters and calls the root
    *      finding method. startPoint can either be taken to be the lower bound or
    *      starting point of an incremental search, or as an initial guess.
    * ----------------------------------------------------------------------------------------------------------------------
    */
  protected double findRoot(Function f, double[] constants, double startPoint,
                            double tolerance) throws NumericalMethodException, FunctionException {
    
    // f : function object
    // constants: to be used with f
    // startPoint : x-value at the beginning of the search range
    // tolerance: error must be less than tolerance
    
    /*
     * Check Bounds and Direction
     * -----------------------------------------------------------------------------
     */
    // If the bounds have already been set, determine the search direction
    if (!this.useFunctionBounds) {
      if (this.endPoint > startPoint) {
        this.direction = 1; // search in positive direction
      }
      else {
        this.direction = -1; // search in negative direction
      }
    } 
    // If the bounds have not been set, use the function bounds if possible, or use MIN_VALUE and MAX_VALUE
    else {
      if (f instanceof BoundedFunction) {
        BoundedFunction boundedF = (BoundedFunction) f;
        if (this.direction == 1) {
          this.endPoint = boundedF.getMaxX();
        }
        else {
          this.endPoint = boundedF.getMinX();
        }
      } else {
        if (this.direction == 1) {
          this.endPoint = Double.MAX_VALUE;
        }
        else {
          this.endPoint = Double.MIN_VALUE;
        }
      }
    }
    
    /*
     * Check Integrity of Root Finding Parameters
     * -----------------------------------------------------------------------------
     */
    tolerance = Math.abs(tolerance); // tolerance must be positive
    
    if (this.subIncrementFraction > 1.) {
      this.subIncrementFraction = BracketingRootFinder.DEFAULT_SUB_INCREMENT_FRACTION; // subIncrementFraction must be positive
    }
    
    /*
     * Find the Root
     * -----------------------------------------------------------------------------
     */
    // check if the startPoint is a root
    try {
      if (f.evaluate(startPoint, constants) == 0.) { // Check if startPoint is a root
        return startPoint;
      } 
    } catch (FunctionException e) {}
    
    // solve for root
    double root = this.rootFindingMethod(f, constants, startPoint, 
                                         tolerance); // Go to method (3)
    
    return root;
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 5) rootFindingMethod() : Finds a single root of function f; to be overridden
    *        by children of RootFinder.
    * ----------------------------------------------------------------------------------------------------------------------
    */
  protected abstract double rootFindingMethod(Function f, double[] constants, double startPoint,
                                              double tolerance) throws NumericalMethodException, FunctionException;
  
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 6) incrementalSearch() : Returns an increment of function f within which a
    *        single root exists, as well as the current evaluation count; starts at xL and
    *        moves up; to be used only by root finding methods that require bracketing.
    *        bounds[0] = Lower Bound (xL)
    *        bounds[1] = Upper Bound (xU)
    * ----------------------------------------------------------------------------------------------------------------------
    */
  protected double[] incrementalSearch(Function f, double[] constants, double startBound,
                                       double tolerance) throws NumericalMethodException, FunctionException {
    
    // startBound: starting x-value of the searching increment
    // endBound: ending x-valye of the searching increment
    // length: length of the searching increment
    
    double endBound = startBound + (double) this.direction * this.incrementLength; // set the endBound based off the startBound
    double[] bounds = new double[2]; // Bounds of the search increment to be returned
    double length = this.incrementLength;
    
    boolean uniqueRoot = false; // Flag for a single root within the given increment; true if only 1 root exists
    
    /*
     * Loop 1: Continue until a unique root is found within the searching increment 
     * ----------------------------------------------------------------------------
     */
    while (!uniqueRoot) { 
      
      // If the startBound has been adjusted to beyond the endPoint, then no root exists within the startPoint and endPoint 
      if ((this.direction == 1 && startBound >= this.endPoint) || (this.direction == -1 && startBound <= this.endPoint)) {
        String functionName = "Function";
        if (f instanceof BoundedFunction) {
          BoundedFunction boundedF = (BoundedFunction) f;
          functionName = boundedF.getID();
        }
        throw new NoRootFoundException(super.getName(), functionName, this, f); // Throw exception
      }
      
      // If the endBound has been adjusted to be beyond the endPoint, then set it to to be equal to the endPoint.
      // The endBound should not be allowed to go beyond the endPoint.
      if ((this.direction == 1 && endBound > this.endPoint) || (this.direction == -1 && endBound < this.endPoint)) {
        endBound = this.endPoint;
      }
      
      double sign = 0; // Denotes the sign of f(x); equal to 1 if f(x) returns as positive, and -1 if f(x) returns as negative 
      double x = startBound; // set initial value of x to be the startBound
      double newSign = 0; // Denotes the new sign of f(x)
      int rootCount = 0; // Number of roots found within the searching increment
      boolean endOfBound = false; // true if the endBound of the searching increment has been reached
      
      /*
       * Loop 2: Search the current search increment for all roots
       * ----------------------------------------------------------------------------
       */
      do { 
        // Search the increment within the error tolerance for all roots
        this.checkEvaluationCount(f); // Check number of function evaluations
        super.setEvaluationCount(super.getEvaluationCount() + 1); // increase evaluation count
        boolean evaluated = false;
        
        /*
         * Loop 3: Attempt to evaluate f(x)
         * ----------------------------------------------------------------------------
         */
        do {
          try {
            newSign = Math.signum(f.evaluate(x, constants)); // update the sign of f(x)
            evaluated = true; // true if f(x) was successfully evaluated
          } catch (FunctionException e) {
            
            // If f(x) returns an undefined value, then a discontinuity has been found
            if (e instanceof UndefinedFunctionException) {
              
              // If x is not at the startBound, then increment rootCount and skip to next x
              if (x != startBound) {
                rootCount++;
                evaluated = true;
              } 
              // If x is still at the startBound, then skip this discontinuity, adjust the startBound and recalculate f(x) at the new x
              else {
                startBound += (double) this.direction * Math.min(this.subIncrementFraction * length, tolerance); // Move startBound beyond the discontinuity
                x = startBound; // Set x to the startBound
                
                //If the startBound has gone beyond the endBound, then the searching increment has been completely searched
                if ((this.direction == 1 && startBound > endBound) || (this.direction == -1 && startBound < endBound)) {
                  endOfBound = true;
                  break; // End the Inner Loop
                }
              }
            } else {
              throw e;
            }
          }
        } while (!evaluated); // End of Loop 3
        
        // If still at the startBound, set the initial value of sign
        if (x == startBound) {
          sign = newSign;
        }
        
        // If the new sign differs from the old sign, then a root has been found
        if (newSign != sign && !endOfBound) { // Check whether the signs of f(x_i) and f(x_i-1) differ
          rootCount++; // If so, increase the root count
          sign = newSign;
        }
        
        // Increment x by the sub-increment fraction of the increment length
        if ((this.direction == 1 && x < endBound) || (this.direction == -1 && x > endBound)) {
          x += (double) this.direction * this.subIncrementFraction * length; // increase x
          if ((this.direction == 1 && x > endBound) || (this.direction == -1 && x < endBound)) {
            x = endBound;
          } 
        } 
        // endBound has been reached
        else {
          endOfBound = true;
        }
        
      } while (!endOfBound); // End of Loop 2
      
      // Case A: No root was found
      if (rootCount == 0) { 
        startBound += this.direction * length; // Move on to the next increment
        endBound = startBound + this.direction * this.incrementLength;
        length = this.incrementLength;
        // Restart Loop 1
      } 
      // Case B: Only a single root was found
      else if (rootCount == 1) {
        uniqueRoot = true; // exit Loop 1
        
        // Set search bounds that will be returned
        if (this.direction == 1) {
          bounds[0] = startBound;
          bounds[1] = endBound;
        } else {
          bounds[0] = endBound;
          bounds[1] = startBound;
        }
      } 
      // Case C: More than 1 root was found
      else {
        length /= 2; // Halve the increment and restart
        endBound = startBound + (double) this.direction * length;
        // Restart Loop 1
      }
    } // End of Loop 1
    
    return bounds;
  }
  /*********************************************************************************************************************/
  
  public double getEndPoint() {
    return this.endPoint;
  }
  
  
  public void setEndPoint(double endPoint) {
    this.endPoint = endPoint;
  }
  
  
  public double getIncrementLength() {
    return this.incrementLength;
  }
  
  
  public void setIncrementLength(double incrementLength) {
    this.incrementLength = incrementLength;
  }
  
  
  public double getSubIncrementFraction() {
    return this.subIncrementFraction;
  }
  
  
  public void setSubIncrementFraction(double subIncrementFraction) {
    this.subIncrementFraction = subIncrementFraction;
  }
  
  
  public int getDirection() {
    return this.direction;
  }
  
  
  public void setDirection(int direction) {
    this.direction = direction;
  }
  
  
  public boolean isUseFunctionBounds() {
    return this.useFunctionBounds;
  }
  
  
  public void setUseFunctionBounds(boolean useFunctionBounds) {
    this.useFunctionBounds = useFunctionBounds;
  }
  
}
