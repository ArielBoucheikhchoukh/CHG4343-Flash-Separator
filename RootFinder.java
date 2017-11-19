public abstract class RootFinder {

	public static final double DEFAULT_START_POINT = 0.;
	public static final double DEFAULT_TOLERANCE = 0.01;
	public static final int DEFAULT_MAX_EVALUATION_COUNT = 100000;
	
	private double evaluationCount;
	private double maxEvaluationCount;

/**********************************************************************************************************************
* 1) Constructor
* ----------------------------------------------------------------------------------------------------------------------
*/
	public RootFinder(double maxEvaluationCount) {
		this.evaluationCount = 0.;
		this.maxEvaluationCount = Math.abs(maxEvaluationCount);
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 2.1) findRoot() : Checks the integrity of the parameters and calls the root
* 					finding method. startPoint can either be taken to be the lower bound or
* 					starting point of an incremental search, or as an initial guess.
* ----------------------------------------------------------------------------------------------------------------------
*/
	protected double findRoot(Function f, double[] constants, double tolerance) 
			throws NumericalMethodException, FunctionException {

		return this.findRoot(f, constants, RootFinder.DEFAULT_START_POINT, tolerance);
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 2.2) findRoot() : Checks the integrity of the parameters and calls the root
* 					finding method. startPoint can either be taken to be the lower bound or
* 					starting point of an incremental search, or as an initial guess.
* ----------------------------------------------------------------------------------------------------------------------
*/
	protected double findRoot(Function f, double[] constants, double startPoint,
			double tolerance) throws NumericalMethodException, FunctionException {
		
		tolerance = Math.abs(tolerance);

		try {
			if (f.evaluate(startPoint, constants) == 0.) { // Check if startPoint is a root
				return startPoint;
			} 
		} catch (FunctionException e) {}
		
		double root = this.rootFindingMethod(f, constants, startPoint, 
				tolerance); // Go to method (3)
		
		return root;
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 3) rootFindingMethod() : Finds a single root of function f; to be overridden
* 							by children of RootFinder.
* ----------------------------------------------------------------------------------------------------------------------
*/
	protected abstract double rootFindingMethod(Function f, double[] constants, double startPoint, 
			double tolerance) 
					throws NumericalMethodException, FunctionException;

/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 4) checkEvaluationCount() : Safeguard that checks whether the root finding
* 								method has exceeded the provided maximum number of function evaluations; 
* 								to be called periodically from rootFindingMethod().
* ----------------------------------------------------------------------------------------------------------------------
*/
	protected void checkEvaluationCount() 
			throws TooManyFunctionEvaluationsException {
		if (this.evaluationCount > this.maxEvaluationCount) {
			throw new TooManyFunctionEvaluationsException();
		}
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 5) checkForAsymptote() : Checks whether the critical point x is a root
* 							(false) or an asymptote (true).
* ----------------------------------------------------------------------------------------------------------------------
*/
	protected boolean checkForAsymptote(Function f, double[] constants, double x, double tolerance)
			throws NumericalMethodException, FunctionException {
		
		boolean evaluated = false;
		int distanceFactor = 2;
		while (!evaluated) {
			try {
				if (Math.abs(f.evaluate(x + distanceFactor * tolerance, constants)) 
						> Math.abs(f.evaluate(x, constants))) {
					this.evaluationCount += 2;
					return false;
				} else {
					return true;
				}
			}
			catch (UndefinedDependentVariableException e) {
				distanceFactor++;
			}
		}
		return true;
	}
/*********************************************************************************************************************/

	public double getEvaluationCount() {
		return this.evaluationCount;
	}

	public void setEvaluationCount(double evaluationCount) {
		this.evaluationCount = evaluationCount;
	}
	
	public double getMaxEvaluationCount() {
		return this.maxEvaluationCount;
	}

	public void setMaxEvaluationCount(double maxEvaluationCount) {
		this.maxEvaluationCount = maxEvaluationCount;
	}

}