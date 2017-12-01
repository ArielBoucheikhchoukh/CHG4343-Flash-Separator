
public class UndefinedFunctionException extends FunctionException {
	
	private double x;
	
	public UndefinedFunctionException(String functionID, Function f, double x) {
		super("UndefinedFunctionException: Result of the function was undefined." 
				+ "\nIndependent Variable: " + x, functionID, f);
		this.x = x;
	}
	
	public double getX() {
		return this.x;
	}
	
	public void setX(double x) {
		this.x = x;
	}
	
}
