//Case 3

public class AdiabaticFeedTemp extends FlashSeparator {

	public AdiabaticFeedTemp(double tankT, double tankP, double F, double[] z, int[] speciesIndices) 
			throws StreamException {
		super("Adiabatic Feed Temperature", tankT, tankP, 
				new Stream("Feed Stream", 273.15, F, z, speciesIndices));
	}

	public Stream[] flashCalculation() 
			throws FlashCalculationException, NumericalMethodException, 
				FunctionException, StreamException {

		Stream feedStream = super.getFeedStream();
		Stream flashStream = super.performFlash();

		double Tref = super.selectReferenceTemperature();
		EnthalpyBalance enthalpyBalance = new EnthalpyBalance(Tref, new Stream[] { new Stream(feedStream) }, 
				null, new Stream[] { new Stream(flashStream) }, super.getBehaviour(), true, true);
		try {
			double T_feed = Menu.findRoot(enthalpyBalance, null, super.getT(), true,
					FlashSeparator.ENTHALPY_BALANCE_INCREMENT_LENGTH, 
					FlashSeparator.ENTHALPY_BALANCE_SUB_INCREMENT_FRACTION,
					FlashSeparator.ENTHALPY_BALANCE_TOLERANCE, 
					FlashSeparator.ENTHALPY_BALANCE_MAX_EVALUATION_COUNT, false);
			super.setFeedStreamTemperature(T_feed, true);
		} catch (NumericalMethodException | FunctionException e) {
			System.out.println(e.getMessage());
			Menu.appendToMessages("Error: Unable to compute feed temperature. \n" + e.getMessage());
			super.setStatus(super.getStatus() + "Unable to compute the temperature of the feed. \r\n");
		}

		Stream[] outletStreams = super.splitPhases();

		return outletStreams;
	}

}