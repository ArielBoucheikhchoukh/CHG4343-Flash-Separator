/* Evaluates the enthalpy of a species in the vapour phase.  
 * T = [K]
 * H = [J/mol*K]
 */

public class EnthalpyVapour extends Correlation {

	public static final int CONSTANT_COUNT = 4;
	public static final double R = 8.314;

	public EnthalpyVapour(String id) {
		super(id);
	}

	public EnthalpyVapour(String id, double[] C, double minX, double maxX, int form) {
		super(id, C, minX, maxX, form);
	}
	
	public EnthalpyVapour(EnthalpyVapour source) {
		super(source);
	}
	
	public EnthalpyVapour clone() {
		return new EnthalpyVapour(this);
	}
	
	protected double evaluateWithinBounds(double x, double[] constants) {

		double T = x;
		double Tb = constants[0];
		double hL = constants[1];
		double lambda = constants[2];
		double[] C = super.getC();

		double Hv = EnthalpyVapour.R 
				* ((C[0] * (T - Tb)) 
						+ (0.5 * C[1] * (Math.pow(T, 2) - Math.pow(Tb, 2)))
						+ ((1. / 3.) * C[2] * (Math.pow(T, 3) - Math.pow(Tb, 3)))
						+ (-1. * C[3] * (Math.pow(T, -1) - Math.pow(Tb, -1))));

		return hL + lambda + Hv;
	}

	protected double evaluateDerivativeWithinBounds(double x, double[] constants) {
		double T = x;
		double dhLdT = constants[1];
		double[] C = super.getC();

		double dHvdT = EnthalpyVapour.R 
				* (C[0] 
						+ C[1] * T 
						+ C[2] * Math.pow(T, 2) 
						+ C[3] * Math.pow(T, -2));

		return dhLdT + dHvdT;
	}

	public int getConstantCount() {
		return EnthalpyVapour.CONSTANT_COUNT;
	}

}