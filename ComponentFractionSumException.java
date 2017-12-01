
public class ComponentFractionSumException extends StreamException {
	
	private double[] fractions;
	
	public ComponentFractionSumException(Stream stream, double[] fractions, String fractionType) {
		super("The " + fractionType + " fractions do not sum to unity.", stream);
		this.fractions = fractions.clone();
	}
	
	public double[] getFractions() {
		return this.fractions.clone();
	}
	
	public void setFractions(double[] fractions) {
		this.fractions = fractions.clone();
	}
}
