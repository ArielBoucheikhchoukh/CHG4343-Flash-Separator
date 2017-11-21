import java.util.Scanner;

public abstract class BoundedFunction implements Function {

	private String id;
	private double minX;
	private double maxX;

	public BoundedFunction(String id, double minX, double maxX) {
		this.id = id;
		this.minX = minX;
		this.maxX = maxX;
	}

	public double evaluate(double x, double[] constants) throws FunctionException {

		if (Double.isNaN(x)) {
			throw new UndefinedIndependentVariableException(this.id);
		}

		double y = this.evaluateWithinBounds(x, constants);

		if (Double.isNaN(y) || Double.isInfinite(y)) {
			throw new UndefinedDependentVariableException(this.id, x);
		}

		if (x < this.minX || x > this.maxX) {
			Menu.appendToMessages("\r\nWarning: " + this.id 
					+ " was evaluated outside of the function bounds.");
		}

		return y;
	}

	public double evaluateDerivative(double x, double[] constants) throws FunctionException {

		if (Double.isNaN(x)) {
			throw new UndefinedIndependentVariableException(this.id);
		}

		double y = this.evaluateDerivativeWithinBounds(x, constants);

		if (Double.isNaN(y)) {
			throw new UndefinedDependentVariableException(this.id, x);
		}

		if (x < this.minX || x > this.maxX) {
			Menu.appendToMessages("\r\nWarning: " + this.id 
					+ " was evaluated outside of the function bounds.");
		}

		return y;
	}

	protected abstract double evaluateWithinBounds(double x, double[] constants) 
			throws FunctionException;

	protected abstract double evaluateDerivativeWithinBounds(double x, double[] constants) 
			throws FunctionException;
	
	public String getID() {
		return id;
	}

	public void setID(String id) {
		this.id = id;
	}
	
	protected double getMinX() {
		return this.minX;
	}

	protected void setMinX(double minX) {
		this.minX = minX;
	}

	protected double getMaxX() {
		return this.maxX;
	}

	protected void setMaxX(double maxX) {
		this.maxX = maxX;
	}

}