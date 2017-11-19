
public class UndefinedDependentVariableException extends FunctionException {
	
	private double x;
	
	public UndefinedDependentVariableException(String functionID, double x) {
		super("UndefinedDependentVariableException: Result of the function was undefined." 
				+ "\nIndependent Variable: " + x, 
				functionID);
		this.x = x;
	}
	
	public double getX() {
		return this.x;
	}
	
	public void setX(double x) {
		this.x = x;
	}
	
}
