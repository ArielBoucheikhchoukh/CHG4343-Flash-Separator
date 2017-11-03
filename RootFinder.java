public abstract class RootFinder {
 
  private static final double DEFAULT_INCREMENT_FRACTION = 0.1;
  
/**********************************************************************************************************************
  1) findRoot() : Checks the integrity of the parameters and calls the root finding method. 
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
      
      if (incrementLength > Math.abs(upperBound - lowerBound) || incrementLength < 0) { // Check whether the increment length is smaller than the 
                                                                                        // domain of function f
       incrementLength = (upperBound - lowerBound) * this.DEFAULT_INCREMENT_FRACTION;
      }
    } 
    
    incrementLength = Math.abs(incrementLength);
    tolerance = Math.abs(tolerance);
    maxEvaluationCount = Math.abs(maxEvaluationCount);
    
    
    /* Find the Root
    -----------------------------------------------------------------------------------------------------------------*/
    if (f.evaluate(startPoint, constants) == 0.) { // Check if startPoint is a root
      return startPoint; 
    }
    else if (isBounded) { // Check if the upper bound is a root in the case of a bounded function
      if (f.evaluate(upperBound, constants) == 0.) { 
        return upperBound;
      }
    }
    System.out.println("start point = " + startPoint);
    //Check within the function bounds
    return this.rootFindingMethod(f, constants, startPoint, incrementLength, tolerance, maxEvaluationCount); //Go to method (2)
  }
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  2) rootFindingMethod() : Finds a single root of function f; to be overriden by children of RootFinder.
----------------------------------------------------------------------------------------------------------------------*/
  protected abstract double rootFindingMethod(Function f, double[] constants, double xL, double incrementLength, 
                                              double tolerance, int maxEvaluationCount) 
    throws NumericalMethodException, FunctionException;
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  3) incrementalSearch() : Returns an increment of function f within which a single root exists, as well as the current 
                           evaluation count; starts at xL and moves up; to be used only by root finding methods that 
                           require bracketing.
                           returnParameters[0] = xL
                           returnParameters[1] = xU
                           returnParameters[2] = evaluationCount
----------------------------------------------------------------------------------------------------------------------*/
  protected double[] incrementalSearch(Function f, double[] constants, double xL, double incrementLength, double tolerance, int evaluationCount, int maxEvaluationCount) 
    throws NumericalMethodException, FunctionException {
    
    double[] returnParameters = new double[3];
    
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
      evaluationCount++;
      
      while (x < xL + incrementLength) { //Search the increment within the error tolerance for all roots
        this.checkEvaluationCount(evaluationCount, maxEvaluationCount);
        
        x += tolerance * incrementLength; // increase x by the error tolerance
        newSign = Math.signum(f.evaluate(x, constants)); // update the sign of f(x)
        evaluationCount++;
        
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
    
    returnParameters[2] = evaluationCount;
    return returnParameters;
  }
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  4) checkEvaluationCount() : Safeguard that checks whether the root finding method has exceeded the provided maximum 
                              number of function evaluations; to be called periodically from rootFindingMethod().
----------------------------------------------------------------------------------------------------------------------*/
  protected void checkEvaluationCount(int evaluationCount, int maxEvaluationCount) throws TooManyFunctionEvaluationsException {
    if (evaluationCount > maxEvaluationCount) {
     throw new TooManyFunctionEvaluationsException(); 
    }
  }
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  5) checkForAsymptote() : Checks whether the critical point x is a root (false) or an asymptote (true). 
----------------------------------------------------------------------------------------------------------------------*/
  protected boolean checkForAsymptote(Function f, double[] constants, double x, double tolerance) 
    throws FunctionException, NumericalMethodException {
    
    if (Math.abs(f.evaluate(x + 2 * tolerance, constants)) >  Math.abs(f.evaluate(x, constants))) {
      return false;
    }
    else {
      System.out.println("Asymptote at x = " + x);
     return true; 
    }
  }
/*********************************************************************************************************************/
  
}