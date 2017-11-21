public class Behaviour implements Cloneable {

	public static final double BUBBLE_DEW_POINT_INCREMENT_LENGTH = 25.;
	public static final double BUBBLE_DEW_POINT_SUB_INCREMENT_FRACTION = 1.;
	public static final double BUBBLE_DEW_POINT_TOLERANCE = 0.01;
	public static final int BUBBLE_DEW_POINT_MAX_EVALUATION_COUNT = 100000;
	public static final double RACHFORD_RICE_INCREMENT_LENGTH = 0.1;
	public static final double RACHFORD_RICE_TOLERANCE = 0.0001;
	public static final int RACHFORD_RICE_MAX_EVALUATION_COUNT = 100000;
	
	//Instance variables below apply only to the last stream that was passed to performFlash().
	private int[] condensableSpeciesIndices;
	private double[] K;


/**********************************************************************************************************************
* 1) Constructor
* ---------------------------------------------------------------------------------------------------------------------
*/
	public Behaviour() {
		this.condensableSpeciesIndices = null;
		this.K = null;
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 2) Copy Constructor
* ---------------------------------------------------------------------------------------------------------------------
*/
	public Behaviour(Behaviour source) {
		if (source.condensableSpeciesIndices != null ) {
			this.condensableSpeciesIndices = source.condensableSpeciesIndices.clone();
		} else {
			this.condensableSpeciesIndices = null;
		}
		
		if (source.K != null ) {
			this.K = source.K.clone();
		} else {
			this.K = null;
		}
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 3) clone()
* ---------------------------------------------------------------------------------------------------------------------
*/
	public Behaviour clone() {
		return new Behaviour(this);
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 4) performFlash()
* ---------------------------------------------------------------------------------------------------------------------
*/
	public Stream performFlash(Stream flashStream)
			throws FlashCalculationException, NumericalMethodException, FunctionException {
		
		int componentCount = flashStream.getComponentCount();
		int condensableCount = 0;
		for (int i = 0; i < componentCount; i++) {
			if (flashStream.isComponentCondensable(i)) {
				condensableCount++;
			}
		}

		int j = 0;
		this.condensableSpeciesIndices = new int[condensableCount];
		double[] z_cd = new double[condensableCount];
		for (int i = 0; i < componentCount; i++) {
			if (flashStream.isComponentCondensable(i)) {
				this.condensableSpeciesIndices[j] = flashStream.getSpeciesIndex(i);
				z_cd[j] = flashStream.getZi(i) / flashStream.getCondensableFraction();
				j++;
			}
		}

		this.calculatePartitionCoefficients(flashStream);
		Function rachfordRice = new RachfordRice(z_cd, this.K);
		double vapourFraction = 0.;
		try {
			vapourFraction = Menu.findRoot(rachfordRice, null, 0., 1., 
					Behaviour.RACHFORD_RICE_INCREMENT_LENGTH, 
					BracketingRootFinder.DEFAULT_SUB_INCREMENT_FRACTION, 
					Behaviour.RACHFORD_RICE_TOLERANCE, 
					Behaviour.RACHFORD_RICE_MAX_EVALUATION_COUNT);
		} catch (NumericalMethodException e) {
			throw new FlashNotPossibleException();
		}

		flashStream.setVapourFraction(vapourFraction);

		j = 0;
		for (int i = 0; i < componentCount; i++) {
			if (flashStream.isComponentCondensable(i)) {
				double x = z_cd[j] / (1 + (K[j] - 1) * vapourFraction);
				double y = x * K[j];

				flashStream.setXi(x, i);
				flashStream.setYi(y, i);

				j++;
			} else {
				flashStream.setXi(0, i);
				flashStream.setYi(0, i);
			}
		}

		return flashStream.clone();
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 5) calculateBubblePointPressure() : Calculates the bubble point pressure of a stream.
* ---------------------------------------------------------------------------------------------------------------------
*/
	public double calculateBubblePointPressure(Stream stream) throws FunctionException {
		double P_bp = 0.;
		
		for (int i = 0; i < stream.getComponentCount(); i++) {
			Species component = Menu.getSpecies(stream.getSpeciesIndex(i));
			if (stream.getT() < component.getTc()) {
				P_bp += (stream.getZi(i) / stream.getCondensableFraction())
						* component.evaluateVapourPressure(stream.getT(), false);
			}
		}
		
		return P_bp;
	}
/*********************************************************************************************************************/
	
	
/**********************************************************************************************************************
* 6) calculateDewPointPressure() : Calculates the dew point pressure of a stream.
* ---------------------------------------------------------------------------------------------------------------------
*/
	public double calculateDewPointPressure(Stream stream) throws FunctionException {
		double P_dp = 0.;
		for (int i = 0; i < stream.getComponentCount(); i++) {
			Species component = Menu.getSpecies(stream.getSpeciesIndex(i));
			if (stream.getT() < component.getTc()) {
				P_dp += (stream.getZi(i) / stream.getCondensableFraction())
						/ component.evaluateVapourPressure(stream.getT(), false);
			}
		}
		if (P_dp != 0.) {
			P_dp = 1. / P_dp;
		}

		return P_dp;
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 7) calculateBubblePointTemperature() : Calculates the bubble point temperature of a stream.
* ---------------------------------------------------------------------------------------------------------------------
*/
	public double calculateBubblePointTemperature(Stream stream) 
			throws FlashCalculationException, FunctionException {
		
		int condensableCount = 0;
		for (int i = 0; i < stream.getComponentCount(); i++) {
			if (stream.isComponentCondensable(i)) {
				condensableCount++;
			}
		}
		
		if (condensableCount == 0) {
			throw new FlashNotPossibleException();
		}
		
		int j = 0;
		int[] cdSpeciesIndices = new int[condensableCount];
		double[] x = new double[condensableCount];
		for (int i = 0; i < stream.getComponentCount(); i++) {
			if (stream.isComponentCondensable(i)) {
				cdSpeciesIndices[j] = stream.getSpeciesIndex(i);
				x[j] = stream.getZi(i);
				j++;
			}
		}
		
		double[] T_sat = new double[condensableCount]; 
		for (int i = 0; i < condensableCount; i++) {
			try {
			T_sat[i] = Menu.findRoot(Menu.getSpecies(cdSpeciesIndices[i]).getCorrelation(Species.VAPOUR_PRESSURE), 
					new double[] {stream.getP()}, 1., true, 
					Behaviour.BUBBLE_DEW_POINT_INCREMENT_LENGTH, 
					Behaviour.BUBBLE_DEW_POINT_SUB_INCREMENT_FRACTION, 
					Behaviour.BUBBLE_DEW_POINT_TOLERANCE, 
					Behaviour.RACHFORD_RICE_MAX_EVALUATION_COUNT, false);
			} catch (NumericalMethodException e) {
				T_sat[i] = 0.00001; //assume this species has no contribution to VLE
			}
		}
		double T_bp = 0.;
		for (int i = 0; i < condensableCount; i++) {
			T_bp += x[i] * T_sat[i];
		}
		
		double error = 0.;
		do {
			
			
			double[] P_sat = new double[condensableCount];
			for (int i = 0; i < condensableCount; i++) {
				P_sat[i] = Menu.getSpecies(cdSpeciesIndices[i]).evaluateVapourPressure(T_bp, false);
			}
			
			double P_j = 0.;
			for (int i = 0; i < condensableCount; i++) {
				P_j += x[i] * P_sat[i] / P_sat[0];
			}
			P_j = stream.getP() / P_j;
			
			double T_new = 0.;
			try {
				T_new = Menu.findRoot(Menu.getSpecies(cdSpeciesIndices[0]).getCorrelation(Species.VAPOUR_PRESSURE), 
					new double[] {P_j}, 1., true, 
					Behaviour.BUBBLE_DEW_POINT_INCREMENT_LENGTH, 
					Behaviour.BUBBLE_DEW_POINT_SUB_INCREMENT_FRACTION, 
					Behaviour.BUBBLE_DEW_POINT_TOLERANCE, 
					Behaviour.RACHFORD_RICE_MAX_EVALUATION_COUNT, false);
			} catch (NumericalMethodException e) {
				throw new FlashNotPossibleException();
			}
			
			error = Math.abs(T_bp - T_new);
			T_bp = T_new;
			
		} while (error > Behaviour.BUBBLE_DEW_POINT_TOLERANCE);
		
		return T_bp;
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 8) calculateDewPointTemperature() : Calculates the dew point temperature of a stream.
* ---------------------------------------------------------------------------------------------------------------------
*/
	public double calculateDewPointTemperature(Stream stream) 
			throws FlashCalculationException, FunctionException {
		
		int condensableCount = 0;
		for (int i = 0; i < stream.getComponentCount(); i++) {
			if (stream.isComponentCondensable(i)) {
				condensableCount++;
			}
		}
		
		if (condensableCount == 0) {
			throw new FlashNotPossibleException();
		}
		
		int j = 0;
		int[] cdSpeciesIndices = new int[condensableCount];
		double[] y = new double[condensableCount];
		for (int i = 0; i < stream.getComponentCount(); i++) {
			if (stream.isComponentCondensable(i)) {
				cdSpeciesIndices[j] = stream.getSpeciesIndex(i);
				y[j] = stream.getZi(i);
				j++;
			}
		}
		
		double[] T_sat = new double[condensableCount]; 
		for (int i = 0; i < condensableCount; i++) {
			try {
			T_sat[i] = Menu.findRoot(Menu.getSpecies(cdSpeciesIndices[i]).getCorrelation(Species.VAPOUR_PRESSURE), 
					new double[] {stream.getP()}, 1., true, 
					Behaviour.BUBBLE_DEW_POINT_INCREMENT_LENGTH, 
					Behaviour.BUBBLE_DEW_POINT_SUB_INCREMENT_FRACTION, 
					Behaviour.BUBBLE_DEW_POINT_TOLERANCE, 
					Behaviour.RACHFORD_RICE_MAX_EVALUATION_COUNT, false);
			} catch (NumericalMethodException e) {
				T_sat[i] = 0.00001; //assume this species has no contribution to VLE
			}
		}
		
		double T_dp = 0.;
		for (int i = 0; i < condensableCount; i++) {
			T_dp += y[i] * T_sat[i];
		}
		
		double error = 0.;
		do {
			double[] P_sat = new double[condensableCount];
			for (int i = 0; i < condensableCount; i++) {
				P_sat[i] = Menu.getSpecies(cdSpeciesIndices[i]).evaluateVapourPressure(T_dp, false);
			}
			
			double P_j = 0.;
			for (int i = 0; i < condensableCount; i++) {
				P_j += y[i] * P_sat[0] / P_sat[i];
			}
			P_j = stream.getP() * P_j;
			
			double T_new = 0.;
			try {
				T_new = Menu.findRoot(Menu.getSpecies(cdSpeciesIndices[0]).getCorrelation(Species.VAPOUR_PRESSURE), 
					new double[] {P_j}, 1., true, 
					Behaviour.BUBBLE_DEW_POINT_INCREMENT_LENGTH, 
					Behaviour.BUBBLE_DEW_POINT_SUB_INCREMENT_FRACTION, 
					Behaviour.BUBBLE_DEW_POINT_TOLERANCE, 
					Behaviour.RACHFORD_RICE_MAX_EVALUATION_COUNT, false);
			} catch (NumericalMethodException e) {
				throw new FlashNotPossibleException();
			}
			
			error = Math.abs(T_dp - T_new);
			T_dp = T_new;
			
		} while (error > Behaviour.BUBBLE_DEW_POINT_TOLERANCE);
		
		return T_dp;
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 9) calculatePartitionCoefficients() : .
* ---------------------------------------------------------------------------------------------------------------------
*/
	protected void calculatePartitionCoefficients(Stream stream) throws FunctionException {

		this.K = new double[this.condensableSpeciesIndices.length];
		for (int i = 0; i < this.condensableSpeciesIndices.length; i++) {
			this.K[i] = Menu.getSpecies(this.condensableSpeciesIndices[i])
					.evaluateVapourPressure(stream.getT(), false) / stream.getP();
		}
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 10) evaluateStreamEnthalpy() : 
* ---------------------------------------------------------------------------------------------------------------------
*/
	public double evaluateStreamEnthalpy(double Tref, Stream stream, boolean derivative) 
	throws FunctionException {
		return stream.evaluateStreamEnthalpy(Tref, derivative);
	}
/*********************************************************************************************************************/

	
	public int[] getCondensableSpeciesIndices() {
		return this.condensableSpeciesIndices.clone();
	}


	public void setCondensableSpeciesIndices(int[] condensableSpeciesIndices) {
		this.condensableSpeciesIndices = condensableSpeciesIndices.clone();
	}
	
	
	public int getCondensableSpeciesIndex(int cdComponentIndex) {
		return this.condensableSpeciesIndices[cdComponentIndex];
	}
	
	
	public void setCondensableSpeciesIndex(int cdSpeciesIndex,int cdComponentIndex) {
		this.condensableSpeciesIndices[cdComponentIndex] = cdSpeciesIndex;
	}
	
	
	public double[] getK() {
		return this.K.clone();
	}


	public void setK(double[] K) {
		this.K = K.clone();
	}
	
	public double getKi(int cdComponentIndex) {
		return this.K[cdComponentIndex];
	}


	public void setKi(double Ki,int cdComponentIndex) {
		this.K[cdComponentIndex] = Ki;
	}
}