/*Case 1*/

public class IsothermalHeat extends FlashSeparator {

/**********************************************************************************************************************
* 1) Constructor
* ---------------------------------------------------------------------------------------------------------------------
*/
	public IsothermalHeat(double T, double tankP, double F, double[] z, int[] speciesIndices) 
		throws StreamException {
		super("Isothermal Heat", T, tankP, new Stream("Feed Stream", T, F, z, speciesIndices));
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 2) falshCalculation()
* ---------------------------------------------------------------------------------------------------------------------
*/
	public Stream[] flashCalculation() 
			throws FlashCalculationException, NumericalMethodException, 
			FunctionException, StreamException {

		Stream feedStream = super.getFeedStream();

		Stream flashStream = super.performFlash();

		double Tref = super.selectReferenceTemperature();
		EnthalpyBalance enthalpyBalance = new EnthalpyBalance(Tref, null, new Stream[] { new Stream(feedStream) },
				new Stream[] { new Stream(flashStream) }, super.getBehaviour(), true);

		try {
			super.setQ(enthalpyBalance.evaluate(enthalpyBalance.getMinX(), new double[] { Tref }));
		} catch (FunctionException e) {
			Menu.appendToMessages("Error: Unable to compute heat of flash. \n" + e.getMessage());
		}

		Stream[] outletStreams = super.splitPhases();

		return outletStreams;
	}
/*********************************************************************************************************************/

}