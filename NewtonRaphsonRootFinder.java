public class NewtonRaphsonRootFinder extends RootFinder {

/**********************************************************************************************************************
* 1) Constructor
* ----------------------------------------------------------------------------------------------------------------------
*/
	public NewtonRaphsonRootFinder(double maxEvaluationCount) {
		super(maxEvaluationCount);
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 2) rootFindingMethod() : Finds a single root of function f.
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
				catch (UndefinedDependentVariableException e) {
					oldX =+ tolerance;
				}
			}

			super.checkEvaluationCount();
			error = Math.abs(newX - oldX);
			oldX = newX;
		} while (error > tolerance);

		return newX;
	}
/*********************************************************************************************************************/

}