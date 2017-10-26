public class NewtonRaphsonRootFinder extends RootFinder {
 
/**********************************************************************************************************************
  1) rootFindingMethod() : Finds a single root of function f.
----------------------------------------------------------------------------------------------------------------------*/
  protected double rootFindingMethod(Function f, double xL, double incrementLength, double tolerance, int maxEvaluationCount) 
    throws NumericalMethodException {
   
    double oldX = xL;
    double newX = 0.;
    double error = 0.;
    int evaluationCount = 0;
    
    do { // Continue loop until convergence
      do { // Continue loop until the new value of x is a real number
        newX = oldX - (f.evaluate(oldX) / f.evaluateDerivative(oldX));
        evaluationCount += 2;
        
        if (Double.isNaN(newX)) { //Check if the new value of x is a real number
          oldX += tolerance;
        }
      } while (Double.isNaN(newX));
      
      super.checkEvaluationCount(evaluationCount, maxEvaluationCount);
      error = Math.abs(newX - oldX);
      System.out.printf("x_old = %.3f, x_new = %.3f, f = %.3f, f' = %.3f\n", oldX, newX, f.evaluate(oldX),f.evaluateDerivative(oldX));
      oldX = newX;
    } while (error > tolerance);
    
    return newX;
  }
/*********************************************************************************************************************/
  
}