public abstract class RootFinder {
 
  private static final double DEFAULT_INCREMENT_FRACTION = 0.1;
  private static final double DEFAULT_INCREMENT_FACTOR = 100.;
  private static final int DEFAULT_MAX_EVALUATION_COUNT = 100000;
  private double evaluationCount;
  
  
/**********************************************************************************************************************
  1) Constructor
----------------------------------------------------------------------------------------------------------------------*/
  public RootFinder() {
    this.evaluationCount = 0.;
  }
/*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
  2.1) findRoot() : Checks the integrity of the parameters and calls the root finding method. 
                  startPoint can either be taken to be the lower bound or starting point of an incremental search, or 
                  as an initial guess.
----------------------------------------------------------------------------------------------------------------------*/
  protected double findRoot(Function f, double[] constants, double tolerance) 
    throws NumericalMethodException, FunctionException {
    
    return this.findRoot(f, constants, 0., tolerance * this.DEFAULT_INCREMENT_FACTOR, tolerance, this.DEFAULT_MAX_EVALUATION_COUNT);
  }
 
/**********************************************************************************************************************
  2.2) findRoot() : Checks the integrity of the parameters and calls the root finding method. 
                  startPoint can either be taken to be the lower bound or starting point of an incremental search, or 
                  as an initial guess.
----------------------------------------------------------------------------------------------------------------------*/
  protected double findRoot(Function f, double[] constants, double startPoint, double incrementLength, double tolerance, int maxEvaluationCount) 
    throws NumericalMethodException, FunctionException {
    
    /* Check Integrity of Root Finding Parameters
    -----------------------------------------------------------------------------------------------------------------*/
    boolean isBounded = false;
    double upperBound = 0.;
    
    if (f instanceof BoundedFunction) { // Check whether the given parameters respect the bounds of function f
      isBounded = true;
      BoundedFunction boundedF = (BoundedFunction) f;
      double lowerBound = boundedF.getMinX();
      upperBound = boundedF.getMaxX();
      
      if (startPoint < lowerBound || startPoint > upperBound) { // Check whether the start point is within the function bounds
        startPoint = lowerBound;
      }
      
      if (Math.abs(incrementLength) > Math.abs(upperBound - lowerBound) || incrementLength < 0) { // Check whether the increment length is smaller 
                                                                                                  // than the domain of function f
       incrementLength = (upperBound - lowerBound) * this.DEFAULT_INCREMENT_FRACTION;
      }
    } 
    
    incrementLength = Math.abs(incrementLength);
    if (incrementLength < tolerance) {
      incrementLength = tolerance * this.DEFAULT_INCREMENT_FACTOR;
    }
    
    tolerance = Math.abs(tolerance);
    maxEvaluationCount = Math.abs(maxEvaluationCount);
    
    
    /* Find the Root
    -----------------------------------------------------------------------------------------------------------------*/
    try {
      if (f.evaluate(startPoint, constants) == 0.) { // Check if startPoint is a root
        return startPoint; 
      }
      else if (isBounded) { // Check if the upper bound is a root in the case of a bounded function
        if (f.evaluate(upperBound, constants) == 0.) { 
          return upperBound;
        }
      }
    }
    catch (Exception e) {
      
    }
    
    System.out.println("start point = " + startPoint);
    //Check within the function bounds
    return this.rootFindingMethod(f, constants, startPoint, incrementLength, tolerance, maxEvaluationCount); //Go to method (2)
  }
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  3) rootFindingMethod() : Finds a single root of function f; to be overriden by children of RootFinder.
----------------------------------------------------------------------------------------------------------------------*/
  protected abstract double rootFindingMethod(Function f, double[] constants, double xL, double incrementLength, 
                                              double tolerance, int maxEvaluationCount) 
    throws NumericalMethodException, FunctionException;
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  4) incrementalSearch() : Returns an increment of function f within which a single root exists, as well as the current 
                           evaluation count; starts at xL and moves up; to be used only by root finding methods that 
                           require bracketing.
                           returnParameters[0] = xL
                           returnParameters[1] = xU
                           returnParameters[2] = evaluationCount
----------------------------------------------------------------------------------------------------------------------*/
  protected double[] incrementalSearch(Function f, double[] constants, double xL, double incrementLength, double tolerance, int maxEvaluationCount) 
    throws NumericalMethodException, FunctionException {
    
    double[] returnParameters = new double[2];
    
    if (f instanceof BoundedFunction) { // Check whether the bounds of the function have been exhausted
      BoundedFunction boundedF = (BoundedFunction) f;
      if (xL >= boundedF.getMaxX()) { //If the provided xL is greater or equal to xU
        throw new NoRootWithinFunctionBoundsException();
      }
    }
      
    boolean uniqueRoot = false; // Flag for a single root within the given increment
    while (!uniqueRoot) { // Continue this loop until only a single root exists
      System.out.println("xL = " + xL + ", Increment Length = " + incrementLength);
      int rootCount = 0;
      double x = xL;
      double sign = Math.signum(f.evaluate(x, constants));
      double newSign = 0;
      this.evaluationCount++;
      
      while (x < xL + incrementLength) { //Search the increment within the error tolerance for all roots
        this.checkEvaluationCount(maxEvaluationCount);
        
        x += tolerance; // increase x by the error tolerance
        newSign = Math.signum(f.evaluate(x, constants)); // update the sign of f(x)
        this.evaluationCount++;
        
        if (newSign != sign) { // Check whether the signs of f(x_i) and f(x_i-1) differ 
          rootCount++; // If so, increase the root count
          sign = newSign;
          System.out.println("Approximate root at x = " + x);
        }
        
      }
      
      System.out.println("Root count: " + rootCount);
      if (rootCount == 0) { // In the case where no roots exist...
        xL += incrementLength; // ... move on to the next increment
      }
      else if (rootCount == 1) { // In the case where only a single root exists...
        uniqueRoot = true; // ... exit the loop
        returnParameters[0] = xL;
        returnParameters[1] = xL + incrementLength;
      }
      else { // In the case where multiple roots exist...
        incrementLength /= 2; // ... halve the increment and restart
      }
    }
    
    return returnParameters;
  }
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  5) checkEvaluationCount() : Safeguard that checks whether the root finding method has exceeded the provided maximum 
                              number of function evaluations; to be called periodically from rootFindingMethod().
----------------------------------------------------------------------------------------------------------------------*/
  protected void checkEvaluationCount(int maxEvaluationCount) throws TooManyFunctionEvaluationsException {
    if (this.evaluationCount > maxEvaluationCount) {
     throw new TooManyFunctionEvaluationsException(); 
    }
  }
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  6) checkForAsymptote() : Checks whether the critical point x is a root (false) or an asymptote (true). 
----------------------------------------------------------------------------------------------------------------------*/
  protected boolean checkForAsymptote(Function f, double[] constants, double x, double tolerance) 
    throws FunctionException, NumericalMethodException {
    
    if (Math.abs(f.evaluate(x + 2 * tolerance, constants)) >  Math.abs(f.evaluate(x, constants))) {
      this.evaluationCount += 2;
      return false;
    }
    else {
      System.out.println("Asymptote at x = " + x);
     return true; 
    }
  }
/*********************************************************************************************************************/
  
  
  public double getEvaluationCount() {
    return this.evaluationCount;
  }
  
  public void setEvaluationCount(double evaluationCount) {
    this.evaluationCount = evaluationCount;
  }
  
}