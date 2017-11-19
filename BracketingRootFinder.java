
public abstract class BracketingRootFinder extends RootFinder{
	
	public static final double DEFAULT_INCREMENT_FACTOR = 100.;
	
	private double endPoint;
	private double incrementLength;
	private int direction;
	private boolean useFunctionBounds;
	
	
/**********************************************************************************************************************
* 1.1) Constructor A
* ----------------------------------------------------------------------------------------------------------------------
*/
	public BracketingRootFinder(double endPoint, double incrementLength, double maxEvaluationCount) {
		super(maxEvaluationCount);
		this.endPoint = endPoint;
		this.incrementLength = Math.abs(incrementLength);
		this.direction = 1;
		this.useFunctionBounds = false;
	}
/*********************************************************************************************************************/


/**********************************************************************************************************************
* 1.2) Constructor B
* ----------------------------------------------------------------------------------------------------------------------
*/
	public BracketingRootFinder(double incrementLength, boolean positiveDirection, 
			double maxEvaluationCount, boolean useFunctionBounds) {
		super(maxEvaluationCount);
		this.endPoint = 0.;
		this.incrementLength = Math.abs(incrementLength);
		
		if (positiveDirection) {
			this.direction = 1;
		} else {
			this.direction = -1;
		}
		
		this.useFunctionBounds = useFunctionBounds;
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 2) findRoot() : Checks the integrity of the parameters and calls the root
* 					finding method. startPoint can either be taken to be the lower bound or
* 					starting point of an incremental search, or as an initial guess.
* ----------------------------------------------------------------------------------------------------------------------
*/
		protected double findRoot(Function f, double[] constants, double startPoint,
				double tolerance) throws NumericalMethodException, FunctionException {
			
			/*
			 * Check Bounds and Direction
			 * -----------------------------------------------------------------------------
			 */
			if (!this.useFunctionBounds) {
				if (this.endPoint > startPoint) {
					this.direction = 1;
				}
				else {
					this.direction = -1;
				}
			} else {
				if (f instanceof BoundedFunction) {
					BoundedFunction boundedF = (BoundedFunction) f;
					if (this.direction == 1) {
						this.endPoint = boundedF.getMaxX();
					}
					else {
						this.endPoint = boundedF.getMinX();
					}
				} else {
					if (this.direction == 1) {
						this.endPoint = Double.MAX_VALUE;
					}
					else {
						this.endPoint = Double.MIN_VALUE;
					}
				}
			}
			
			/*
			 * Check Integrity of Root Finding Parameters
			 * -----------------------------------------------------------------------------
			 */
			
			if (incrementLength < tolerance) {
				incrementLength = tolerance * BracketingRootFinder.DEFAULT_INCREMENT_FACTOR;
			}
			
			tolerance = Math.abs(tolerance);

			/*
			 * Find the Root
			 * -----------------------------------------------------------------------------
			 */
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
			double tolerance) throws NumericalMethodException, FunctionException;

/*********************************************************************************************************************/

		
/**********************************************************************************************************************
* 4) incrementalSearch() : Returns an increment of function f within which a
* 							single root exists, as well as the current evaluation count; starts at xL and
* 							moves up; to be used only by root finding methods that require bracketing.
* 							returnParameters[0] = xL 
* 							returnParameters[1] = xU
* ----------------------------------------------------------------------------------------------------------------------
*/
	protected double[] incrementalSearch(Function f, double[] constants, double startBound,
			double tolerance) throws NumericalMethodException, FunctionException {
		
		double endBound = startBound + (double) this.direction * this.incrementLength;
		double[] bounds = new double[2];
		
		boolean uniqueRoot = false; // Flag for a single root within the given increment
		while (!uniqueRoot) { // Continue this loop until only a single root exists
			
			if ((this.direction == 1 && startBound >= this.endPoint) 
					&& (this.direction == -1 && startBound <= this.endPoint)) {
				throw new NoRootFoundException();
			}
			
			if ((this.direction == 1 && endBound > this.endPoint) 
					&& (this.direction == -1 && endBound < this.endPoint)) {
				endBound = this.endPoint;
			}
			
			int rootCount = 0;
			double x = startBound;

			double sign = 0;
			double newSign = 0;
			boolean evaluated = false;
			while (!evaluated) {
				try {
					super.setEvaluationCount(super.getEvaluationCount() + 1);
					sign = Math.signum(f.evaluate(x, constants));
					evaluated = true;
				} catch (UndefinedDependentVariableException e) {
					x = x + (double) this.direction * tolerance;
				}
			}

			while ((this.direction == 1 && x < endBound) || (this.direction == -1 && x > endBound)) { 
				// Search the increment within the error tolerance for all roots
				this.checkEvaluationCount();

				x = x + (double) this.direction * tolerance; // increase x by the error tolerance
				try {
					super.setEvaluationCount(super.getEvaluationCount() + 1);
					newSign = Math.signum(f.evaluate(x, constants)); // update the sign of f(x)

					if (newSign != sign) { // Check whether the signs of f(x_i) and f(x_i-1) differ
						rootCount++; // If so, increase the root count
						sign = newSign;
						// System.out.println("Approximate root at x = " + x);
					}
				} catch (UndefinedDependentVariableException e) {
				}
			}

			// System.out.println("Root count: " + rootCount);
			if (rootCount == 0) { // In the case where no roots exist...
				startBound += incrementLength; // ... move on to the next increment
				endBound += incrementLength;
				// System.out.println("New xU = " + xU);
			} else if (rootCount == 1) { // In the case where only a single root exists...
				uniqueRoot = true; // ... exit the loop
				
				if (this.direction == 1) {
					bounds[0] = startBound;
					bounds[1] = endBound;
				} else {
					bounds[0] = endBound;
					bounds[1] = startBound;
				}
			} else { // In the case where multiple roots exist...
				incrementLength /= 2; // ... halve the increment and restart
			}
		}

		return bounds;
	}
/*********************************************************************************************************************/

	
	public double getEndPoint() {
		return endPoint;
	}


	public void setEndPoint(double endPoint) {
		this.endPoint = endPoint;
	}


	public double getIncrementLength() {
		return incrementLength;
	}


	public void setIncrementLength(double incrementLength) {
		this.incrementLength = incrementLength;
	}


	public int getDirection() {
		return direction;
	}


	public void setDirection(int direction) {
		this.direction = direction;
	}


	public boolean isUseFunctionBounds() {
		return useFunctionBounds;
	}


	public void setUseFunctionBounds(boolean useFunctionBounds) {
		this.useFunctionBounds = useFunctionBounds;
	}
	
}