public class EnthalpyBalance extends BoundedFunction {

	private double Tref;
	private boolean isInlet; // true if the unknown temperature streams are inlet streams, 
								// false if they are outlet streams
	private Stream[] unknownTempStreams; // Inlet or outlet streams, all evaluated at the same temperature
	private Stream[] inletStreams; // Inlet streams with constant temperatures
	private Stream[] outletStreams; // Outlet streams with constant temperatures
	private Behaviour behaviour;

	
/**********************************************************************************************************************
* 1) Constructor
* ---------------------------------------------------------------------------------------------------------------------
*/
	public EnthalpyBalance(double Tref, Stream[] unknownTempStreams, Stream[] inletStreams, Stream[] outletStreams,
			Behaviour behaviour, boolean isInlet) {

		super("Enthalpy Balance", 0., 1.);
		this.Tref = Tref;
		this.isInlet = isInlet;
		this.behaviour = behaviour;

		double[] bounds = { Double.MIN_VALUE, Double.MAX_VALUE };

		if (unknownTempStreams != null) {
			this.unknownTempStreams = new Stream[unknownTempStreams.length];
			for (int i = 0; i < unknownTempStreams.length; i++) {
				this.unknownTempStreams[i] = new Stream(unknownTempStreams[i]);
				bounds = this.calculateBounds(bounds[0], bounds[1], this.unknownTempStreams[i]);
			}
		}

		if (inletStreams != null) {
			this.inletStreams = new Stream[inletStreams.length];
			for (int i = 0; i < inletStreams.length; i++) {
				this.inletStreams[i] = new Stream(inletStreams[i]);
				bounds = this.calculateBounds(bounds[0], bounds[1], this.inletStreams[i]);
			}
		}

		if (outletStreams != null) {
			this.outletStreams = new Stream[outletStreams.length];
			for (int i = 0; i < outletStreams.length; i++) {
				this.outletStreams[i] = new Stream(outletStreams[i]);
				bounds = this.calculateBounds(bounds[0], bounds[1], this.outletStreams[i]);
			}
		}

		super.setMinX(bounds[0]);
		super.setMaxX(bounds[1]);
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 2) evaluateWithinBounds() : Returns the energy that must be added to the system.
* ---------------------------------------------------------------------------------------------------------------------
*/
	protected double evaluateWithinBounds(double x, double[] constants) throws FunctionException {
		// System.out.println("Test - EnthalpyBalance Class - evaluateWithinBounds
		// Method: x = " + x);
		return this.evaluateHeat(x, false);
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 3) evaluateDerivativeWithinBounds() : Returns the derivative of the energy that must be added to 
* 										the system with respect to the unknown temperature.
* ---------------------------------------------------------------------------------------------------------------------
*/
	protected double evaluateDerivativeWithinBounds(double x, double[] constants) 
			throws FunctionException {
		// System.out.println("Test - EnthalpyBalance Class -
		// evaluateDerivativeWithinBounds Method: x = " + x);
		return this.evaluateHeat(x, true);
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 4) evaluateHeat() : Returns the heat of the flash separation, Q. 
* 						If Q is positive, then energy must be added to the system. 
* 						If Q is negative, then energy must be removed from the system.
* ---------------------------------------------------------------------------------------------------------------------
*/
	private double evaluateHeat(double T, boolean derivative) throws FunctionException {

		double Q = 0;
		
		if (this.unknownTempStreams != null) {
			for (int i = 0; i < unknownTempStreams.length; i++) {

				unknownTempStreams[i].setT(T);

				if (this.isInlet) {
					Q -= this.behaviour.evaluateStreamEnthalpy(this.Tref, unknownTempStreams[i], 
							derivative);
				} else {
					Q += this.behaviour.evaluateStreamEnthalpy(this.Tref, unknownTempStreams[i], 
							derivative);
				}
			}
		}

		if (this.inletStreams != null) {
			for (int i = 0; i < inletStreams.length; i++) {
				Q -= this.behaviour.evaluateStreamEnthalpy(this.Tref, inletStreams[i], derivative);
			}
		}

		if (this.outletStreams != null) {
			for (int i = 0; i < outletStreams.length; i++) {
				Q += this.behaviour.evaluateStreamEnthalpy(this.Tref, outletStreams[i], derivative);
			}
		}
		
		return Q;
	}
/*********************************************************************************************************************/

	
	private double[] calculateBounds(double minX, double maxX, Stream stream) {

		for (int i = 0; i < stream.getComponentCount(); i++) {
			minX = Math.max(minX,
					Menu.getSpecies(stream.getSpeciesIndex(i)).getCorrelation(Species.ENTHALPY_LIQUID).getMinX());
			minX = Math.max(minX,
					Menu.getSpecies(stream.getSpeciesIndex(i)).getCorrelation(Species.ENTHALPY_VAPOUR).getMinX());

			maxX = Math.min(maxX,
					Menu.getSpecies(stream.getSpeciesIndex(i)).getCorrelation(Species.ENTHALPY_LIQUID).getMaxX());
			maxX = Math.min(maxX,
					Menu.getSpecies(stream.getSpeciesIndex(i)).getCorrelation(Species.ENTHALPY_VAPOUR).getMaxX());
		}
		
		return new double[] { minX, maxX };
	}

	public Stream[] getInletStreams() {

		Stream[] arrayCopy = new Stream[this.inletStreams.length];

		for (int i = 0; i < this.inletStreams.length; i++) {
			arrayCopy[i] = this.inletStreams[i].clone();
		}

		return arrayCopy;
	}

	public void setInletStreams(Stream[] inletStreams) {

		this.inletStreams = new Stream[inletStreams.length];

		for (int i = 0; i < this.inletStreams.length; i++) {
			this.inletStreams[i] = inletStreams[i].clone();
		}
	}
	
	public Stream[] getOutletStreams() {

		Stream[] arrayCopy = new Stream[this.outletStreams.length];

		for (int i = 0; i < this.outletStreams.length; i++) {
			arrayCopy[i] = this.outletStreams[i].clone();
		}

		return arrayCopy;
	}

	public void setOutletStreams(Stream[] outletStreams) {

		this.outletStreams = new Stream[outletStreams.length];

		for (int i = 0; i < this.outletStreams.length; i++) {
			this.outletStreams[i] = outletStreams[i].clone();
		}
	}

}