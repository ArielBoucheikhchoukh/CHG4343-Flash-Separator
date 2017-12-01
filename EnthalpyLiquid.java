/* Evaluates the enthalpy of a species in the liquid phase.  
 * T = [K]
 * h = [J/mol*K]
 */

public class EnthalpyLiquid extends Correlation {

	public static final int CONSTANT_COUNT = 5;

	public EnthalpyLiquid(String id) {
		super(id);
	}

	public EnthalpyLiquid(String id, double[] C, double minX, double maxX, int form) {
		super(id, C, minX, maxX, form);
	}
	
	public EnthalpyLiquid(EnthalpyLiquid source) {
		super(source);
	}
	
	public EnthalpyLiquid clone() {
		return new EnthalpyLiquid(this);
	}
	
	protected double evaluateWithinBounds(double x, double[] constants) {
		double T = x;
		double Tref = constants[0];
		double Tc = constants[1];
		double[] C = super.getC();

		if (super.getForm() == 0) {
			return 0.001 * (C[0] * (T - Tref) + 0.5 * C[1] * (Math.pow(T, 2) - Math.pow(Tref, 2))
					+ (1. / 3.) * C[2] * (Math.pow(T, 3) - Math.pow(Tref, 3))
					+ (1. / 4.) * C[3] * (Math.pow(T, 4) - Math.pow(Tref, 4))
					+ (1. / 5.) * C[4] * (Math.pow(T, 5) - Math.pow(Tref, 5)));
		} else {
			double t = 1 - T / Tc;
			double t0 = 1 - Tref / Tc;

			return 0.001 * -Tc
					* (Math.pow(C[0], 2) * Math.log(t / t0) + C[1] * (t - t0)
							- C[0] * C[2] * (Math.pow(t, 2) - Math.pow(t0, 2))
							- (1. / 3.) * C[0] * C[3] * (Math.pow(t, 3) - Math.pow(t0, 3))
							- (1. / 12.) * Math.pow(C[2], 2) * (Math.pow(t, 4) - Math.pow(t0, 4))
							- (1. / 10.) * C[2] * C[3] * (Math.pow(t, 5) - Math.pow(t0, 5))
							- (1. / 30.) * Math.pow(C[3], 2) * (Math.pow(t, 6) - Math.pow(t0, 6)));
		}
	}

	protected double evaluateDerivativeWithinBounds(double x, double[] constants) {
		double T = x;
		double Tc = constants[1];
		double[] C = super.getC();

		if (super.getForm() == 0) {
			return 0.001 
					* (C[0] 
					+ C[1] * T 
					+ C[2] * Math.pow(T, 2) 
					+ C[3] * Math.pow(T, 3) 
					+ C[4] * Math.pow(T, 4));
		} else {
			double t = 1 - T / Tc;

			return 0.001 * -Tc
					* (Math.pow(C[0], 2) / t 
							+ C[1] 
							- 2. * C[0] * C[2] * t 
							- C[0] * C[3] * Math.pow(t, 2)
							- (1. / 3.) * Math.pow(C[2], 2) * Math.pow(t, 3) 
							- (1. / 2.) * C[2] * C[3] * Math.pow(t, 4)
							- (1. / 5.) * Math.pow(C[3], 2) * Math.pow(t, 5));
		}
	}

	public int getConstantCount() {
		return EnthalpyLiquid.CONSTANT_COUNT;
	}

}