public class Species {

	public static final int VAPOUR_PRESSURE = 0;
	public static final int ENTHALPY_LIQUID = 1;
	public static final int ENTHALPY_VAPOUR = 2;

	public static final int PHYSICAL_PROPERTY_COUNT = 8;
	public static final int CORRELATION_COUNT = 3;

	private String name;
	private int index;
	private double molarMass; // [g/mol]
	private double Tb;
	private double latentHeat;
	private double accentricFactor;
	private double Tc;
	private double Pc;
	private double Vc;
	private double Zc;
	private Correlation[] correlations;
	private int[][] subGroups;

	
/**********************************************************************************************************************
* 1) Constructor
* ----------------------------------------------------------------------------------------------------------------------
*/
	public Species(String name, int index, double[] properties, double[] correlationParameters, 
			int[][] subGroups) {
		
		//Store Identifiers
		this.name = name;
		this.index = index;
		
		// Store Physical Properties
		this.molarMass = properties[0];
		this.Tb = properties[1];
		this.latentHeat = properties[2];
		this.accentricFactor = properties[3];
		this.Tc = properties[4];
		this.Pc = properties[5];
		this.Vc = properties[6];
		this.Zc = properties[7];

		// Initialize Correlations
		this.correlations = new Correlation[Species.CORRELATION_COUNT];

		this.correlations[VAPOUR_PRESSURE] = new VapourPressure("Vapour Pressure of " + this.name);
		this.correlations[ENTHALPY_LIQUID] = new EnthalpyLiquid("Liquid-Phase Enthalpy of " + this.name);
		this.correlations[ENTHALPY_VAPOUR] = new EnthalpyVapour("Vapour-Phase Enthalpy of " + this.name);

		int j = 0;
		double[] C;
		for (int i = 0; i < Species.CORRELATION_COUNT; i++) {
			int constantCount = this.correlations[i].getConstantCount();

			C = new double[constantCount];
			System.arraycopy(correlationParameters, j, C, 0, constantCount);

			j += constantCount;
			this.correlations[i].setParameters(C, correlationParameters[j], 
					correlationParameters[j + 1],
					(int) correlationParameters[j + 2]);

			j += 3;
		}
		
		this.setSubGroups(subGroups);
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 2) Copy Constructor
* ----------------------------------------------------------------------------------------------------------------------
*/
	public Species(Species source) {

		// Store Physical Properties
		this.name = source.name;
		this.index = source.index;
		this.molarMass = source.molarMass;
		this.Tb = source.Tb;
		this.latentHeat = source.latentHeat;
		this.accentricFactor = source.accentricFactor;
		this.Tc = source.Tc;
		this.Pc = source.Pc;
		this.Vc = source.Vc;
		this.Zc = source.Zc;

		// Initialize Correlations
		this.correlations = new Correlation[Species.CORRELATION_COUNT];
		for (int i = 0; i < Species.CORRELATION_COUNT; i++) {
			this.correlations[i] = source.correlations[i].clone();
		}
		
		//Store Sub-Group Counts
		this.setSubGroups(source.subGroups);
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 3) clone()
* ----------------------------------------------------------------------------------------------------------------------
*/
	public Species clone() {
		return new Species(this);
	}
/*********************************************************************************************************************/


	public static int getCorrelationParameterCount() {
		
		return  VapourPressure.CONSTANT_COUNT + 3
				+ EnthalpyLiquid.CONSTANT_COUNT + 3
				+ EnthalpyVapour.CONSTANT_COUNT + 3;
	}
	
	
	public double[] getProperties() {
		return new double[] { this.molarMass, this.Tb, this.latentHeat, this.accentricFactor, this.Tc, 
				this.Pc, this.Vc, this.Zc };
	}
	
	public int getNumberOfSubGroupTypes() {
		return this.subGroups[0].length;
	}

	public double evaluateVapourPressure(double T, boolean derivative) throws FunctionException {
		if (derivative) {
			return this.correlations[Species.VAPOUR_PRESSURE].evaluateDerivative(T, null);
		} else {
			return this.correlations[Species.VAPOUR_PRESSURE].evaluate(T, null);
		}
	}

	public double evaluateEnthalpyLiquid(double T, double Tref, boolean derivative) 
			throws FunctionException {
		if (derivative) {
			return this.correlations[Species.ENTHALPY_LIQUID].evaluateDerivative(T, 
					new double[] { Tref, this.Tc });
		} else {
			return this.correlations[Species.ENTHALPY_LIQUID].evaluate(T, 
					new double[] { Tref, this.Tc });
		}
	}

	public double evaluateEnthalpyVapour(double T, double Tref, boolean derivative) throws FunctionException {
		if (derivative) {
			double dhdL = this.correlations[Species.ENTHALPY_LIQUID].evaluateDerivative(this.Tb,
					new double[] { Tref, this.Tc });
			return this.correlations[Species.ENTHALPY_VAPOUR].evaluateDerivative(T,
					new double[] { this.Tb, dhdL, 1000 * this.latentHeat });
		} else {
			double hL = this.correlations[Species.ENTHALPY_LIQUID].evaluate(this.Tb, 
					new double[] { Tref, this.Tc });
			return this.correlations[Species.ENTHALPY_VAPOUR].evaluate(T,
					new double[] { this.Tb, hL, 1000 * this.latentHeat });
		}
	}
	
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIndex() {
		return this.index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	public double getMolarMass() {
		return this.molarMass;
	}

	public void setMolarMass(double molarMass) {
		this.molarMass = molarMass;
	}

	public double getTb() {
		return this.Tb;
	}

	public void setTb(double Tb) {
		this.Tb = Tb;
	}

	public double getLatentHeat() {
		return this.latentHeat;
	}

	public void setLatentHeat(double latentHeat) {
		this.latentHeat = latentHeat;
	}

	public double getAccentricFactor() {
		return this.accentricFactor;
	}

	public void setAccentricFactor(double accentricFactor) {
		this.accentricFactor = accentricFactor;
	}

	public double getTc() {
		return this.Tc;
	}

	public void setTc(double Tc) {
		this.Tc = Tc;
	}

	public double getPc() {
		return this.Pc;
	}

	public void setPc(double Pc) {
		this.Pc = Pc;
	}

	public double getVc() {
		return this.Vc;
	}

	public void setVc(double Vc) {
		this.Vc = Vc;
	}

	public double getZc() {
		return this.Zc;
	}

	public void setZc(double Zc) {
		this.Zc = Zc;
	}
	
	public Correlation[] getCorrelations() {
		
		Correlation[] correlationsCopy = new Correlation[this.correlations.length];
		
		for (int i = 0; i < this.correlations.length; i++) {
			correlationsCopy[i] = this.correlations[i].clone();
		}
		
		return correlationsCopy;
	}
	
	public Correlation getCorrelation(int correlationIndex) {
		return (Correlation) this.correlations[correlationIndex].clone();
	}
	
	public void setCorrelations(Correlation[] correlations) {
		
		this.correlations = new Correlation[correlations.length];
		
		for (int i = 0; i < correlations.length; i++) {
			this.correlations[i] = correlations[i].clone();
		}
	}
	
	public int[][] getSubGroups() {
		
		int[][] subGroupsCopy = new int[2][this.subGroups[0].length];
		
		for (int i = 0; i < this.subGroups[0].length; i++) {
			subGroupsCopy[0][i] = this.subGroups[0][i];
			subGroupsCopy[1][i] = this.subGroups[1][i];
		}
		
		return subGroupsCopy;
	}
	
	public int getNumberOfSubGroup(int subGroupIndex) {
		for (int i = 0; i < this.subGroups[0].length; i++) {
			if (this.subGroups[0][i] == subGroupIndex) {
				return this.subGroups[1][i];
			}
		}
		return 0;
	}
	
	public void setSubGroups(int[][] subGroups) {
		this.subGroups = new int[2][subGroups[0].length];
		
		for (int i = 0; i < this.subGroups[0].length; i++) {
			this.subGroups[0][i] = subGroups[0][i];
			this.subGroups[1][i] = subGroups[1][i];
		}
	}

}