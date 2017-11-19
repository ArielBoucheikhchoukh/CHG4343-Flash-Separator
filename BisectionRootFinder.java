public class BisectionRootFinder extends BracketingRootFinder {

/**********************************************************************************************************************
* 1.1) Constructor A
* ----------------------------------------------------------------------------------------------------------------------
*/
	public BisectionRootFinder(double endPoint, double incrementLength, double maxEvaluationCount) {
		super(endPoint, incrementLength, maxEvaluationCount);
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 1.2) Constructor B
* ----------------------------------------------------------------------------------------------------------------------
*/
	public BisectionRootFinder(double incrementLength, boolean positiveDirection, 
			double maxEvaluationCount, boolean useFunctionBounds) {
		super(incrementLength, positiveDirection, maxEvaluationCount, useFunctionBounds);
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 2) rootFindingMethod() : Finds and returns a root of function f.
* ----------------------------------------------------------------------------------------------------------------------
*/
	protected double rootFindingMethod(Function f, double[] constants, double startPoint, double tolerance)
			throws NumericalMethodException, FunctionException {

		double xR = 0.;
		double endBound = startPoint;
		boolean foundRoot = false;

		do {
			double[] bounds = super.incrementalSearch(f, constants, endBound, tolerance);

			double xL = bounds[0];
			double xU = bounds[1];
			double xR_old = 0.;

			if (super.getDirection() == 1) {
				endBound = xU;
			} else {
				endBound = xL;
			}
			
			try {
				double f_xL = f.evaluate(xL, constants);
				double f_xU = f.evaluate(xU, constants);
				double f_xR = 0;
				super.setEvaluationCount(super.getEvaluationCount() + 2);
				
				double error = 0.;
				int iterationCount = 0;
				do {
					super.checkEvaluationCount();
	
					xR = 0.5 * (xL + xU);
					f_xR = f.evaluate(xR, constants);
					
					error = Math.abs(xR - xR_old);
					xR_old = xR;
					super.setEvaluationCount(super.getEvaluationCount() + 1);
					
					if (error > tolerance || iterationCount == 0) {
						if (Math.signum(f_xR) != Math.signum(f_xL)) {
							xU = xR;
							f_xU = f.evaluate(xU, constants);
						} else if (Math.signum(f_xR) != Math.signum(f_xU)) {
							xL = xR;
							f_xL = f.evaluate(xL, constants);
						}
						
						super.setEvaluationCount(super.getEvaluationCount() + 1);
					}
					
					iterationCount++;
	
				} while (error > tolerance || iterationCount == 1);
	
				foundRoot = !super.checkForAsymptote(f, constants, xR, tolerance);
			} 
			catch (UndefinedDependentVariableException e) {
				foundRoot = false;
			}
			

		} while (!foundRoot);

		return xR;
	}
/*********************************************************************************************************************/

}