public abstract class RootFinder {
  
  public static final double DEFAULT_START_POINT = 0.;
  public static final double DEFAULT_TOLERANCE = 0.01;
  public static final int DEFAULT_MAX_EVALUATION_COUNT = 100000;
  
  private String name; // Name of RootFinder Object; used in exception handling
  private double evaluationCount; // Number of times the function f has been evaluated
  private double maxEvaluationCount; // The maximum number of times function f is allowed to be evaluated before an exception is thrown
  
  /**********************************************************************************************************************
    * 1) Constructor
    * ----------------------------------------------------------------------------------------------------------------------
    */
  public RootFinder(String name, double maxEvaluationCount) {
    this.name = name;
    this.evaluationCount = 0.;
    this.maxEvaluationCount = Math.abs(maxEvaluationCount);
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 2) Copy Constructor
    * ----------------------------------------------------------------------------------------------------------------------
    */
  public RootFinder(RootFinder source) {
    this.name = source.name;
    this.evaluationCount = source.evaluationCount;
    this.maxEvaluationCount = source.maxEvaluationCount;
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 3) clone()
    * ----------------------------------------------------------------------------------------------------------------------
    */
  public abstract RootFinder clone();
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 4.1) findRoot() : Checks the integrity of the parameters and calls the root
    *      finding method. startPoint can either be taken to be the lower bound or
    *      starting point of an incremental search, or as an initial guess.
    * ----------------------------------------------------------------------------------------------------------------------
    */
  protected double findRoot(Function f, double[] constants, double tolerance) 
    throws NumericalMethodException, FunctionException {
    
    return this.findRoot(f, constants, RootFinder.DEFAULT_START_POINT, tolerance);
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 4.2) findRoot() : Checks the integrity of the parameters and calls the root
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
    
    tolerance = Math.abs(tolerance);
    
    try {
      if (f.evaluate(startPoint, constants) == 0.) { // Check if startPoint is a root
        return startPoint;
      } 
    } catch (FunctionException e) {}
    
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
                                              double tolerance) 
    throws NumericalMethodException, FunctionException;
  
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 6) checkEvaluationCount() : Safeguard that checks whether the root finding
    *         method has exceeded the provided maximum number of function evaluations; 
    *         to be called periodically from rootFindingMethod().
    * ----------------------------------------------------------------------------------------------------------------------
    */
  protected void checkEvaluationCount(Function f) 
    throws TooManyFunctionEvaluationsException {
    if (this.evaluationCount > this.maxEvaluationCount) {
      String functionName = "Function";
      if (f instanceof BoundedFunction) {
        BoundedFunction boundedF = (BoundedFunction) f;
        functionName = boundedF.getID();
      }
      
      throw new TooManyFunctionEvaluationsException(this.name, functionName, this, f);
    }
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 7) checkForAsymptote() : Checks whether the critical point x is a root
    *        (false) or an asymptote (true).
    * ----------------------------------------------------------------------------------------------------------------------
    */
  protected boolean checkForAsymptote(Function f, double[] constants, double x, double tolerance)
    throws NumericalMethodException, FunctionException {
    
    boolean evaluated = false;
    int distanceFactor = 2;
    double f_x = f.evaluate(x, constants); 
    this.evaluationCount += 1;
    while (!evaluated) {
      try {
        double f_above = f.evaluate(x + distanceFactor * tolerance * 0.01, constants);
        double f_below = f.evaluate(x - distanceFactor * tolerance * 0.01, constants);
        if ((Math.abs(f_above) < Math.abs(f_x))
              && (Math.abs(f_below) < Math.abs(f_x))) {
          this.evaluationCount += 2;
          return true;
        } else {
          return false;
        }
      }
      catch (UndefinedFunctionException e) {
        distanceFactor++;
      }
    }
    return true;
  }
  /*********************************************************************************************************************/
  
  public String getName() {
    return this.name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public double getEvaluationCount() {
    return this.evaluationCount;
  }
  
  public void setEvaluationCount(double evaluationCount) {
    this.evaluationCount = evaluationCount;
  }
  
  public double getMaxEvaluationCount() {
    return this.maxEvaluationCount;
  }
  
  public void setMaxEvaluationCount(double maxEvaluationCount) {
    this.maxEvaluationCount = maxEvaluationCount;
  }
  
}