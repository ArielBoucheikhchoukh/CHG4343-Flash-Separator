public abstract class RootFinder {
 
  private static final double DEFAULT_INCREMENT_FRACTION = 0.1;
  
  private double tolerance; // Absolute tolerance
  private int evaluationCount;
  
  
/**********************************************************************************************************************
  1) Constructor
----------------------------------------------------------------------------------------------------------------------*/
  public RootFinder(double tolerance) {
    this.tolerance = tolerance;
    this.evaluationCount = 0;
  }
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  2) findRoot() : Checks the integrity of the parameters and calls the root finding method. 
                  startPoint can either be taken to be the lower bound or starting point of an incremental search, or 
                  as an initial guess.
----------------------------------------------------------------------------------------------------------------------*/
  protected double findRoot(Function f, double startPoint, double incrementLength, int maxEvaluationCount) 
    throws NumericalMethodException {
    
    this.evaluationCount = 0; // Reset evaluation count
    
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
      
      if (incrementLength > Math.abs(upperBound - lowerBound) || incrementLength < 0) { // Check whether the increment length is smaller than the 
                                                                                        // domain of function f
       incrementLength = (upperBound - lowerBound) * this.DEFAULT_INCREMENT_FRACTION;
      }
    } 
    
    incrementLength = Math.abs(incrementLength);
    maxEvaluationCount = Math.abs(maxEvaluationCount);
    
    
    /* Find the Root
    -----------------------------------------------------------------------------------------------------------------*/
    if (f.evaluate(startPoint) == 0.) { // Check if startPoint is a root
      return startPoint; 
    }
    else if (isBounded) { // Check if the upper bound is a root in the case of a bounded function
      if (f.evaluate(upperBound) == 0.) { 
        return upperBound;
      }
    }
    System.out.println("start point = " + startPoint);
    //Check within the function bounds
    return this.rootFindingMethod(f, startPoint, incrementLength, maxEvaluationCount); //Go to method (3)
  }
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  3) rootFindingMethod() : Finds a single root of function f; to be overriden by children of RootFinder.
----------------------------------------------------------------------------------------------------------------------*/
  protected abstract double rootFindingMethod(Function f, double xL, double incrementLength, int maxEvaluationCount) 
    throws NumericalMethodException;
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  4) incrementalSearch() : Returns an increment of function f within which a single root exists; starts at xL and moves
                           up; to be used only by root finding methods that require bracketing.
----------------------------------------------------------------------------------------------------------------------*/
  protected double[] incrementalSearch(Function f, double xL, double incrementLength, int maxEvaluationCount) 
    throws NumericalMethodException {
    
    double[] bounds = {xL, xL + incrementLength};
    
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
      double sign = Math.signum(f.evaluate(x));
      double newSign = 0;
      this.evaluationCount++;
      
      while (x < xL + incrementLength) { //Search the increment within the error tolerance for all roots
        this.checkEvaluationCount(maxEvaluationCount);
        
        x += this.tolerance * incrementLength; // increase x by the error tolerance
        newSign = Math.signum(f.evaluate(x)); // update the sign of f(x)
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
        bounds[0] = xL;
        bounds[1] = xL + incrementLength;
      }
      else { // In the case where multiple roots exist...
        incrementLength /= 2; // ... halve the increment and restart
      }
    }
    
    return bounds;
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
  protected boolean checkForAsymptote(Function f, double x) throws NumericalMethodException {
    this.evaluationCount += 2;
    
    if (Math.abs(f.evaluate(x + 2 * this.tolerance)) >  Math.abs(f.evaluate(x))) {
      return false;
    }
    else {
      System.out.println("Asymptote at x = " + x + " --- " + Math.abs(f.evaluate(x)) + " > " 
                           + Math.abs(f.evaluate(x + 2*this.tolerance))) ;
     return true; 
    }
  }
/*********************************************************************************************************************/
  
  protected double getTolerance() {
   return this.tolerance; 
  }
  
  protected void setTolerance(double tolerance) {
   this.tolerance = tolerance; 
  }
  
  protected int getEvaluationCount() {
   return this.evaluationCount; 
  }
  
  protected void setEvaluationCount(int evaluationCount) {
   this.evaluationCount = evaluationCount; 
  }
  
  protected void increaseEvaluationCount(int increase) {
   this.evaluationCount += increase; 
  }
  
}