public class BisectionRootFinder extends RootFinder {
  
/**********************************************************************************************************************
  1) Constructor
----------------------------------------------------------------------------------------------------------------------*/
  public BisectionRootFinder() {
    super();
  }
/*********************************************************************************************************************/
  
  protected double rootFindingMethod(Function f, double[] constants, double xL, double incrementLength, double tolerance, int maxEvaluationCount) 
    throws NumericalMethodException, FunctionException {
   
    double xR = 0.;
    double lowerBound = xL;
    boolean foundRoot = false;
    int evaluationCount = 0;
    
    do {
      double f_xR = 0;
      double error = 0.;
      
      System.out.println("Previous Lower Bound : " + lowerBound);
      
      double[] bounds = super.incrementalSearch(f, constants, lowerBound, incrementLength, tolerance, maxEvaluationCount);
      
      xL = bounds[0];
      double xU = bounds[1];
      lowerBound = xU;
      
      double f_xL = f.evaluate(xL, constants);
      double f_xU = f.evaluate(xU, constants);
      super.setEvaluationCount(super.getEvaluationCount() + 2);
      
      System.out.println("xL = " + xL + " and xU = " + xU);
      
      do {
        super.checkEvaluationCount(maxEvaluationCount);
        
        xR = 0.5*(xL + xU);
        f_xR = f.evaluate(xR, constants);
        
        System.out.printf("Error = %.5f, xL = %.5f, f_xL = %.5f --- xR = %.5f, f_xR = %.5f --- xU = %.5f, f_xU = %.5f \n", 
                          error, xL, f_xL, xR, f_xR, xU, f_xU);
        
        if (Math.signum(f_xR) != Math.signum(f_xL)) {
          xU = xR;
          f_xU = f.evaluate(xU, constants);
        }
        else if (Math.signum(f_xR) != Math.signum(f_xU)) {
          xL = xR;
          f_xL = f.evaluate(xL, constants);
        }
        
        error = (xU - xL)/2;
        super.setEvaluationCount(super.getEvaluationCount() + 3);
        
      } while (error > tolerance);
      
      xR = 0.5*(xL + xU);
      foundRoot = !super.checkForAsymptote(f, constants, xR, tolerance);
      super.setEvaluationCount(super.getEvaluationCount() + 2);
      
    } while (!foundRoot);
    
    return xR;
  }
  
}