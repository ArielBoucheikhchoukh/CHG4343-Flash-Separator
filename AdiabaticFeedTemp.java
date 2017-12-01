//Case 3

public class AdiabaticFeedTemp extends FlashSeparator {

	
/**********************************************************************************************************************
* 1) Constructor
* ---------------------------------------------------------------------------------------------------------------------
*/
	public AdiabaticFeedTemp(double tankT, double tankP, double F, double[] z, int[] speciesIndices, 
			Behaviour behaviour) throws StreamException {
		super("Adiabatic Feed Temperature", tankT, tankP, 
				new Stream("Feed Stream", 273.15, F, z, speciesIndices), behaviour);
	}
/*********************************************************************************************************************/
	
	
/**********************************************************************************************************************
* 2) Copy Constructor
* ---------------------------------------------------------------------------------------------------------------------
*/
	public AdiabaticFeedTemp(AdiabaticFeedTemp source) throws StreamException {
		super(source);
	}
/*********************************************************************************************************************/
	

/**********************************************************************************************************************
* 3) clone()
* ---------------------------------------------------------------------------------------------------------------------
*/
	public AdiabaticFeedTemp clone() {
		try {
			return new AdiabaticFeedTemp(this);
		}
		catch (StreamException e) {
			return null;
		}
	}
/*********************************************************************************************************************/


/**********************************************************************************************************************
* 4) flashCalculation() : Performs flash calculation and return outlet liquid-phase and vapour-phase 
* 							outlet streams; computes the temperature of the feed under adiabatic conditions.
* ---------------------------------------------------------------------------------------------------------------------
*/
	public Stream[] flashCalculation() 
			throws FlashCalculationException, NumericalMethodException, 
				FunctionException, StreamException {

		Stream feedStream = super.getFeedStream();
		
		// Step 1. Attempt Flash Separation
		Stream flashStream = super.performFlash();
		
		// Step 2. Select Reference Temperature 
		double Tref = super.selectReferenceTemperature(); // Returns lowest pure-species normal boiling point
		
		// Step 3. Calculate Feed Temperature
		
		// The feed stream is passed to the enthalpy balance as an inlet stream of unknown temperature, 
		// whereas the flash stream is passed as an outlet stream of known temperature
		EnthalpyBalance enthalpyBalance = new EnthalpyBalance(Tref, new Stream[] { new Stream(feedStream) }, 
				null, new Stream[] { new Stream(flashStream) }, super.getBehaviour(), true, true);
		
		// Start at the flash temperature and search in the positive direction for the feed temperature
		try {
			double T_feed = Menu.findRoot(enthalpyBalance, null, super.getT(), true,
					FlashSeparator.ENTHALPY_BALANCE_INCREMENT_LENGTH, 
					FlashSeparator.ENTHALPY_BALANCE_SUB_INCREMENT_FRACTION,
					FlashSeparator.ENTHALPY_BALANCE_TOLERANCE, 
					FlashSeparator.ENTHALPY_BALANCE_MAX_EVALUATION_COUNT, false);
			super.setFeedStreamTemperature(T_feed, true);
		} catch (NumericalMethodException | FunctionException e) {
			System.out.println(e.getMessage());
			Menu.appendToMessages("Error: Unable to compute the feed temperature. \n" + e.getMessage());
			super.setStatus(super.getStatus() + "Unable to compute the temperature of the feed. \r\n");
		}
		
		// Step 5. Split Flash Stream into Liquid and Vapour Phases
		Stream[] outletStreams = super.splitPhases();

		return outletStreams;
	}
/*********************************************************************************************************************/



}