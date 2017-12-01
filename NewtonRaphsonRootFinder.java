public class NewtonRaphsonRootFinder extends RootFinder {

	
/**********************************************************************************************************************
* 1) Constructor
* ----------------------------------------------------------------------------------------------------------------------
*/
	public NewtonRaphsonRootFinder(double maxEvaluationCount) {
		super("Newton-Raphson Root Finder", maxEvaluationCount);
	}
/*********************************************************************************************************************/


/**********************************************************************************************************************
* 2) Copy Constructor
* ----------------------------------------------------------------------------------------------------------------------
*/
	public NewtonRaphsonRootFinder(NewtonRaphsonRootFinder source) {
		super(source);
	}
/*********************************************************************************************************************/
	

/**********************************************************************************************************************
* 3) clone()
* ----------------------------------------------------------------------------------------------------------------------
*/
	public NewtonRaphsonRootFinder clone() {
		return new NewtonRaphsonRootFinder(this);
	}
/*********************************************************************************************************************/
	
	
/**********************************************************************************************************************
* 4) rootFindingMethod() : Finds a single root of function f.
* ----------------------------------------------------------------------------------------------------------------------
*/
	protected double rootFindingMethod(Function f, double[] constants, double startPoint, double tolerance)
			throws NumericalMethodException, FunctionException {

		double x = startPoint;
		double oldX = x;
		double newX = 0.;
		double error = 0.;

		do { // Continue loop until convergence
			boolean evaluated = false;
			while (!evaluated) {
				try {
					super.setEvaluationCount(super.getEvaluationCount() + 2);
					newX = oldX - f.evaluate(oldX, constants) / f.evaluateDerivative(oldX, constants);
					evaluated = true;
				}
				catch (UndefinedFunctionException e) {
					oldX =+ tolerance;
				}
			}

			super.checkEvaluationCount(f);
			error = Math.abs(newX - oldX);
			oldX = newX;
		} while (error > tolerance);

		return newX;
	}
/*********************************************************************************************************************/

}