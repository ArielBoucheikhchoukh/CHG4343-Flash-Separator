//Case 2

public class AdiabaticFlashTemp extends FlashSeparator {

	public AdiabaticFlashTemp(double feedT, double tankP, double F, double[] z, int[] speciesIndices) 
			throws StreamException {
		super("Adiabatic Flash Temperature", tankP, new Stream("Feed Stream", feedT, F, z, speciesIndices));
	}

	public Stream[] flashCalculation() 
			throws FlashCalculationException, NumericalMethodException, 
				FunctionException, StreamException {

		Stream feedStream = super.getFeedStream();
		Stream flashStream = super.getFlashStream();

		int componentCount = flashStream.getComponentCount();
		Species[] components = new Species[componentCount];
		for (int i = 0; i < componentCount; i++) {
			components[i] = Menu.getSpecies(flashStream.getSpeciesIndex(i));
		}

		double[] T_bounds = super.getBehaviour().bubbleDewPointTemperatures(flashStream);
		if (T_bounds[0] == T_bounds[1]) {
			T_bounds[1] = feedStream.getT();
		}

		super.setT(T_bounds[0]);
		double Tref = super.selectReferenceTemperature();
		double error = 0.;

		do {
			try {
				if (super.getT() > T_bounds[1]) {
					throw new FlashNotPossibleException();
				}

				flashStream = super.performFlash();

				EnthalpyBalance enthalpyBalance = new EnthalpyBalance(Tref,
						new Stream[] { new Stream(super.getFlashStream()) }, 
						new Stream[] { new Stream(feedStream) },
						null, super.getBehaviour(), false);

				double T_flash = 0.;
				T_flash = Menu.findRoot(enthalpyBalance, null, super.getT(), false,
						FlashSeparator.ENTHALPY_BALANCE_INCREMENT_LENGTH, 
						FlashSeparator.ENTHALPY_BALANCE_TOLERANCE,
						FlashSeparator.ENTHALPY_BALANCE_MAX_EVALUATION_COUNT, false);

				error = Math.abs(T_flash - super.getT());

				super.setT(super.getT() + FlashSeparator.FLASH_TEMPERATURE_INCREMENT);
				
			} catch (FlashCalculationException | NumericalMethodException | FunctionException e) {
				super.setT(super.getT() + FlashSeparator.FLASH_TEMPERATURE_INCREMENT);
			}
		} while (error > FlashSeparator.FLASH_TEMPERATURE_TOLERANCE);

		Stream[] outletStreams = super.splitPhases();

		return outletStreams;
	}

}