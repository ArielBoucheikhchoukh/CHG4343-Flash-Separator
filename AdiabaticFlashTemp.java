//Case 2

public class AdiabaticFlashTemp extends FlashSeparator implements Function {
	
	public static final double MIN_TEMPERATURE = 0.01;
	public static final double FLASH_TEMPERATURE_INCREMENT_FRACTION = 0.2;
	public static final double FLASH_TEMPERATURE_SUB_INCREMENT_FRACTION = 1.;
	public static final double FLASH_TEMPERATURE_TOLERANCE = 1.;
	public static final int FLASH_TEMPERATURE_MAX_EVALUATION_COUNT = 100000;
	
	private double Tref;

/**********************************************************************************************************************
* 1) Constructor
* ---------------------------------------------------------------------------------------------------------------------
*/
	public AdiabaticFlashTemp(double feedT, double tankP, double F, double[] z, int[] speciesIndices) 
			throws StreamException {
		super("Adiabatic Flash Temperature", feedT, tankP, 
				new Stream("Feed Stream", feedT, F, z, speciesIndices));
	}
/*********************************************************************************************************************/


/**********************************************************************************************************************
* 2) Copy Constructor
* ---------------------------------------------------------------------------------------------------------------------
*/
	public AdiabaticFlashTemp(AdiabaticFlashTemp source) throws StreamException {
		super("Adiabatic Flash Temperature", source.getT(), source.getP(), 
				new Stream("Feed Stream", source.getT(), source.getFeedStream().getF(), 
						source.getFeedStream().getZ().clone(), 
						source.getFeedStream().getSpeciesIndices()));
		super.setFeedStream(source.getFeedStream());
		super.setFlashStream(source.getFlashStream());
		super.setOutletStreams(source.getOutletStreams());
	}
/*********************************************************************************************************************/
	

/**********************************************************************************************************************
* 3) clone()
* ---------------------------------------------------------------------------------------------------------------------
*/
	public AdiabaticFlashTemp clone() {
		try {
			return new AdiabaticFlashTemp(this);
		} catch (StreamException e) {
			return null;
		}
	}
/*********************************************************************************************************************/
	

/**********************************************************************************************************************
* 4) flashCalculation() : .
* ---------------------------------------------------------------------------------------------------------------------
*/
	public Stream[] flashCalculation() 
			throws FlashCalculationException, NumericalMethodException, 
				FunctionException, StreamException {

		Stream feedStream = super.getFeedStream();
		Stream flashStream = super.getFlashStream();

		int componentCount = flashStream.getComponentCount();
		double[] Tc = new double[componentCount];
		for (int i = 0; i < componentCount; i++) {
			Tc[i] = Menu.getSpecies(flashStream.getSpeciesIndex(i)).getTc();
		}
		
		boolean foundFlashTemp = false;
		double T_L = AdiabaticFlashTemp.MIN_TEMPERATURE;
		double T_U = super.getFeedStream().getT();
		do {
			T_L = AdiabaticFlashTemp.MIN_TEMPERATURE;
			for (int j = 0; j < componentCount; j++) {
				if (Tc[j] < T_U && Tc[j] > T_L) {
					T_L = Tc[j];
				}
			}
					
			flashStream.setT(0.5*(T_L + T_U), false);
			double T_bp = super.getBehaviour().calculateBubblePointTemperature(flashStream);
			double T_dp = super.getBehaviour().calculateDewPointTemperature(flashStream);
					
			super.setT(T_bp);
			this.Tref = super.selectReferenceTemperature();
			
			try {
				Menu.findRoot((Function) this, null, T_bp, T_dp, 
						AdiabaticFlashTemp.FLASH_TEMPERATURE_INCREMENT_FRACTION
							* Math.abs(T_bp - T_dp), 
						AdiabaticFlashTemp.FLASH_TEMPERATURE_SUB_INCREMENT_FRACTION,
						AdiabaticFlashTemp.FLASH_TEMPERATURE_TOLERANCE, 
						AdiabaticFlashTemp.FLASH_TEMPERATURE_MAX_EVALUATION_COUNT);
				foundFlashTemp = true;
			} catch (NumericalMethodException | FunctionException e) {
				throw new FlashNotPossibleException();
			}
					
			T_U = T_L;
					
		} while (T_L > AdiabaticFlashTemp.MIN_TEMPERATURE);
		
		if (T_L <= AdiabaticFlashTemp.MIN_TEMPERATURE && !foundFlashTemp) {
			throw new FlashNotPossibleException();
		}
		
		Stream[] outletStreams = super.splitPhases();

		return outletStreams;
	}
/*********************************************************************************************************************/


/**********************************************************************************************************************
* 5) calculateFlashTemperature()
* ---------------------------------------------------------------------------------------------------------------------
*/
	public double calculateFlashTemperature(double T) 
			throws FunctionException {
		
		super.setT(T);
		
		Stream feedStream = super.getFeedStream();
		Stream flashStream;
		
		try {
			flashStream = super.performFlash();
		} catch (FlashCalculationException | NumericalMethodException e) {
			throw new UndefinedDependentVariableException("Flash Calculation", T);
		}
		
		EnthalpyBalance enthalpyBalance = new EnthalpyBalance(Tref,
				new Stream[] { new Stream(flashStream) }, 
				new Stream[] { new Stream(feedStream) },
				null, super.getBehaviour(), false, false);

		double T_flash = 1.;
		try {
			T_flash = Menu.findRoot(enthalpyBalance, null, feedStream.getT() 
						- FlashSeparator.ENTHALPY_BALANCE_TOLERANCE, 
					AdiabaticFlashTemp.MIN_TEMPERATURE,
					FlashSeparator.ENTHALPY_BALANCE_INCREMENT_LENGTH, 
					FlashSeparator.ENTHALPY_BALANCE_SUB_INCREMENT_FRACTION,
					FlashSeparator.ENTHALPY_BALANCE_TOLERANCE + 0.01,
					FlashSeparator.ENTHALPY_BALANCE_MAX_EVALUATION_COUNT);
		} catch(NumericalMethodException | FunctionException e) {
			throw new UndefinedDependentVariableException(enthalpyBalance.getID(), super.getT());
		}
		
		return T_flash;
	}
/*********************************************************************************************************************/

	
	public double evaluate(double x, double[] constants) throws FunctionException  {
		
		double newFlashTemp = this.calculateFlashTemperature(x);
		return super.getT() - newFlashTemp;
	}
	
	public double evaluateDerivative(double x, double[] constants) throws FunctionException {
		throw new DerivativeNotDefinedException("FlashSeparator: AdiabaticFlashTemp");
	}
	
	public double getTref() {
		return Tref;
	}

	public void setTref(double tref) {
		Tref = tref;
	}

}