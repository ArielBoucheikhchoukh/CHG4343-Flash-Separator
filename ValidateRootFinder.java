/* Validation of the RootFinder class.
 * Intended to be used with the following files:
    - BisectionRootFinder
    - BoundedFunction
    - Function
    - NewtonRaphsonRootFinder
    - NoRootWithinFunctionBoundsException
    - NumericalMethodException
    - OutOfFunctionBoundsException
    - RootFinder
    - TooManyFunctionEvaluationsException
    - ValidationFunction */

public class ValidateRootFinder {
  public static void main(String[] args) {
   
    //RootFinder rootFinder = new BisectionRootFinder();
    RootFinder rootFinder = new NewtonRaphsonRootFinder();
    
    double answer = 0.;
    
    try {
      answer = rootFinder.findRoot(new ValidationFunction(-50., 100.), 0.02, 20., 0.001, 20000);
      System.out.println("\nThe root is: " + answer);
    }
    catch (NumericalMethodException e) {
      System.out.println("\nError. " + e.getMessage()); 
    }
  }
}