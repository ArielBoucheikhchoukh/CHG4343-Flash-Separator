public class NewtonRaphsonRootFinder extends RootFinder {
 
/**********************************************************************************************************************
  1) Constructor
----------------------------------------------------------------------------------------------------------------------*/
  public NewtonRaphsonRootFinder() {
    super();
  }
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  2) rootFindingMethod() : Finds a single root of function f.
----------------------------------------------------------------------------------------------------------------------*/
  protected double rootFindingMethod(Function f, double[] constants, double xL, double incrementLength, double tolerance, int maxEvaluationCount) 
    throws NumericalMethodException, FunctionException {
   
    double oldX = xL;
    double newX = 0.;
    double error = 0.;
    int evaluationCount = 0;
    
    do { // Continue loop until convergence
      do { // Continue loop until the new value of x is a real number
        //System.out.println("Test - NewtonRaphsonRootFinder Class: About to evaluate f.");
        double num = f.evaluate(oldX, constants);
        //System.out.println("Test - NewtonRaphsonRootFinder Class: About to evaluate df/dx.");
        double dem = f.evaluateDerivative(oldX, constants);
        newX = oldX - num/dem;
        super.setEvaluationCount(super.getEvaluationCount() + 2);
        
        if (Double.isNaN(newX)) { //Check if the new value of x is a real number
          oldX += tolerance;
        }
      } while (Double.isNaN(newX));
      
      super.checkEvaluationCount(maxEvaluationCount);
      error = Math.abs(newX - oldX);
      System.out.printf("x_old = %.3f, x_new = %.3f, f = %.3f, f' = %.3f\n", oldX, newX, f.evaluate(oldX, constants), 
                        f.evaluateDerivative(oldX, constants));
      oldX = newX;
    } while (error > tolerance);
    
    return newX;
  }
/*********************************************************************************************************************/
  
}