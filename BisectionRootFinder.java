public class BisectionRootFinder extends RootFinder {
  
  public BisectionRootFinder(double tolerance) {
    super(tolerance);
  }
  
  protected double rootFindingMethod(Function f, double xL, double incrementLength, int maxEvaluationCount) 
    throws NumericalMethodException {
   
    double xR = 0.;
    
    double lowerBound = xL;
    boolean foundRoot = false;
    
    do {
      double f_xR = 0;
      double error = 0.;
      
      System.out.println("Previous Lower Bound : " + lowerBound);
      
      double[] bounds = super.incrementalSearch(f, lowerBound, incrementLength, maxEvaluationCount);
      
      xL = bounds[0];
      double xU = bounds[1];
      lowerBound = xU;
      
      double f_xL = f.evaluate(xL);
      double f_xU = f.evaluate(xU);
      super.increaseEvaluationCount(2);
      
      System.out.println("xL = " + xL + " and xU = " + xU);
      
      do {
        super.checkEvaluationCount(maxEvaluationCount);
        
        xR = 0.5*(xL + xU);
        f_xR = f.evaluate(xR);
        
        System.out.printf("Error = %.5f, xL = %.5f, f_xL = %.5f --- xR = %.5f, f_xR = %.5f --- xU = %.5f, f_xU = %.5f \n", 
                          error, xL, f_xL, xR, f_xR, xU, f_xU);
        
        if (Math.signum(f_xR) != Math.signum(f_xL)) {
          xU = xR;
          f_xU = f.evaluate(xU);
        }
        else if (Math.signum(f_xR) != Math.signum(f_xU)) {
          xL = xR;
          f_xL = f.evaluate(xL);
        }
        
        error = (xU - xL)/2;
        super.increaseEvaluationCount(3);
        
      } while (error > super.getTolerance());
      
      xR = 0.5*(xL + xU);
      foundRoot = !super.checkForAsymptote(f, xR);
      
    } while (!foundRoot);
    
    return xR;
  }
  
}