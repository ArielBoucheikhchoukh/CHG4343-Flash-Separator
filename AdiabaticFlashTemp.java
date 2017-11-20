//Case 2

public class AdiabaticFlashTemp extends FlashSeparator implements Function {
	
	public static final double MIN_TEMPERATURE = 0.01;
	public static final double FLASH_TEMPERATURE_INCREMENT_FRACTION = 0.2;
	public static final double FLASH_TEMPERATURE_SUB_INCREMENT_FRACTION = 1.;
	public static final double FLASH_TEMPERATURE_TOLERANCE = 1.;
	public static final int FLASH_TEMPERATURE_MAX_EVALUATION_COUNT = 100000;
	
	private double Tref;

	
	public AdiabaticFlashTemp(double feedT, double tankP, double F, double[] z, int[] speciesIndices) 
			throws StreamException {
		super("Adiabatic Flash Temperature", feedT, tankP, 
				new Stream("Feed Stream", feedT, F, z, speciesIndices));
	}
	
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
		
		double startPointTemperature;
		double endPointTemperature = super.getT();
		boolean foundFlashTemp = false;
		do {
			startPointTemperature = AdiabaticFlashTemp.MIN_TEMPERATURE;
			for (int i = 0; i < componentCount; i++) {
				if (Tc[i] < endPointTemperature && Tc[i] > startPointTemperature) {
					startPointTemperature = Tc[i];
				}
			}
			
			double[] T_bounds = super.getBehaviour().bubbleDewPointTemperatures(flashStream, 
					startPointTemperature);
			if (T_bounds[0] == T_bounds[1]) {
				T_bounds[1] = feedStream.getT();
			}
	
			super.setT(T_bounds[0]);
			this.Tref = super.selectReferenceTemperature();
	
			try {
				Menu.findRoot((Function) this, null, T_bounds[0], T_bounds[1], 
						AdiabaticFlashTemp.FLASH_TEMPERATURE_INCREMENT_FRACTION
							* Math.abs(T_bounds[1] - T_bounds[0]), 
						AdiabaticFlashTemp.FLASH_TEMPERATURE_SUB_INCREMENT_FRACTION,
						AdiabaticFlashTemp.FLASH_TEMPERATURE_TOLERANCE, 
						AdiabaticFlashTemp.FLASH_TEMPERATURE_MAX_EVALUATION_COUNT);
				foundFlashTemp = true;
			} catch (NumericalMethodException | FunctionException e) {}
			
			if (startPointTemperature <= AdiabaticFlashTemp.MIN_TEMPERATURE) {
				throw new FlashNotPossibleException();
			}
			
			endPointTemperature = startPointTemperature;
		} while (!foundFlashTemp);

		Stream[] outletStreams = super.splitPhases();

		return outletStreams;
	}
	
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
			T_flash = Menu.findRoot(enthalpyBalance, null, feedStream.getT(), false,
					FlashSeparator.ENTHALPY_BALANCE_INCREMENT_LENGTH, 
					FlashSeparator.ENTHALPY_BALANCE_SUB_INCREMENT_FRACTION,
					FlashSeparator.ENTHALPY_BALANCE_TOLERANCE + 0.01,
					FlashSeparator.ENTHALPY_BALANCE_MAX_EVALUATION_COUNT, false);
		} catch(NumericalMethodException | FunctionException e) {
			throw new UndefinedDependentVariableException(enthalpyBalance.getID(), super.getT());
		}
		
		return T_flash;
	}
	
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