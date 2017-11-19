/* Validation of the RootFinder class.
 * Intended to be used with the following files:
    - BisectionRootFinder
    - BoundedFunction
    - Function
    - NewtonRaphsonRootFinder
    - NoRootWithinFunctionBoundsException
    - NumericalMethodException
    - RootFinder
    - TooManyFunctionEvaluationsException
    - ValidationFunction */

public class ValidateRootFinder {
	public static void main(String[] args) {

		// RootFinder rootFinder = new BisectionRootFinder(20., true, 20000, true);
		RootFinder rootFinder = new NewtonRaphsonRootFinder(20000);

		double answer = 0.;

		try {
			answer = rootFinder.findRoot(new ValidationFunction(-50., 100.), null, 0.02, 0.001);
			System.out.println("\nThe root is: " + answer);
		} catch (Exception e) {
			System.out.println("\nError. " + e.getMessage());
		}
	}
}