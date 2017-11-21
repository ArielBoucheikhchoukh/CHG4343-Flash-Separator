public class NonIdealBehaviour extends Behaviour {
	
	//Instance variables below apply only to the last stream that was passed to performFlash().
	private double[] phi;
	private double[] gamma;
	
	
/**********************************************************************************************************************
* 1) Constructor
* ---------------------------------------------------------------------------------------------------------------------
*/
	public NonIdealBehaviour() {
		super();
		this.phi = null;
		this.gamma = null;
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 2) Copy Constructor
* ---------------------------------------------------------------------------------------------------------------------
*/
	public NonIdealBehaviour(NonIdealBehaviour source) {
		super.setCondensableSpeciesIndices(source.getCondensableSpeciesIndices());
		super.setK(source.getK());
		this.phi = source.phi.clone();
		this.gamma = source.gamma.clone();
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 3) clone()
* ---------------------------------------------------------------------------------------------------------------------
*/
	public NonIdealBehaviour clone() {
		return new NonIdealBehaviour(this);
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 4) performFlash()
* ---------------------------------------------------------------------------------------------------------------------
*/
	public Stream performFlash(Stream flashStream)
			throws FlashCalculationException, NumericalMethodException, FunctionException {
		
		//Iterate: guess v, {gamma}_i and {phi}_i
		
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
* 9) calculateActivityCoefficients() : Returns the activity coefficients of all condensable components
* 										in the same order as they are in the stream. 
* 		If isVLE = true, then use the actual x-values of the stream;
* 							ignore the value of isSaturatedVapour in this case.
*   	If isVLE = false AND isSaturatedVapour = false, then use the z-values in place of the x-values.
*   	If isVLE = false AND isSaturatedVapour = true, then return 1 for all gamma values.
* ---------------------------------------------------------------------------------------------------------------------
*/
	public double[] calculateActivityCoefficients(Stream stream, 
			boolean isVLE, boolean isSaturatedVapour) {
		
		/*The bubble point class will be calling this method, but all of the z-values need to be equal
		* to the x-values of the stream. 
		*
		*/
		
		int condensableCount = 0;
		for (int i = 0; i < stream.getComponentCount(); i++) {
			condensableCount++;
		}
		
		double[] gamma = new double[condensableCount];
		
		return gamma;
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 10) calculateFugacityCoefficients() : Returns the pure species fugacity coefficients of all condensable
* 										components in the same order as they are in the stream. 
* 		If isVLE = true, then use the actual y-values of the stream;
* 							ignore the value of isSaturatedVapour in this case.
*   	If isVLE = false AND isSaturatedVapour = false, then return 1 for all phi values.
*   	If isVLE = false AND isSaturatedVapour = true, then use the z-values in place of the y-values.
* ---------------------------------------------------------------------------------------------------------------------
*/
	public double[] calculateFugacityCoefficients(Stream stream, 
			boolean isVLE, boolean isSaturatedVapour) {
		
		int condensableCount = 0;
		for (int i = 0; i < stream.getComponentCount(); i++) {
			condensableCount++;
		}
		
		double[] phi = new double[condensableCount];
		
		return phi;
	}
/*********************************************************************************************************************/


/**********************************************************************************************************************
* 11) calculatePartitionCoefficients() : .
* ---------------------------------------------------------------------------------------------------------------------
*/
	protected void calculatePartitionCoefficients(Stream stream) throws FunctionException {

		super.setK(new double[super.getCondensableSpeciesIndices().length]);
		for (int i = 0; i < this.getCondensableSpeciesIndices().length; i++) {
			double K_i = (this.gamma[i] * Menu.getSpecies(this.getCondensableSpeciesIndex(i))
					.evaluateVapourPressure(stream.getT(), false))
					/ (this.phi[i] * stream.getP());
			this.setKi(K_i, i);
		}
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 12) evaluateStreamEnthalpy() : Evaluates the enthalpy of the stream. Takes into account the residual
* 								enthalpy of each component;
* ---------------------------------------------------------------------------------------------------------------------
*/
	public double evaluateStreamEnthalpy(double Tref, Stream stream, boolean derivative) 
	throws FunctionException {
		return stream.evaluateStreamEnthalpy(Tref, derivative);
	}
/*********************************************************************************************************************/


	public double[] getPhi() {
		return this.phi;
	}
	
	
	public void setPhi(double[] phi) {
		this.phi = phi;
	}
	
	
	public double getPhi_i(int cdComponentIndex) {
		return this.phi[cdComponentIndex];
	}
	
	
	public void setPhi_i(double phi, int cdComponentIndex) {
		this.phi[cdComponentIndex] = phi;
	}
	
	
	public double[] getGamma() {
		return this.gamma;
	}
	
	
	public void setGamma(double[] gamma) {
		this.gamma = gamma;
	}

	
	public double getGamma_i(int cdComponentIndex) {
		return this.gamma[cdComponentIndex];
	}
	
	
	public void setGamma_i(double gamma, int cdComponentIndex) {
		this.gamma[cdComponentIndex] = gamma;
	}
	
}