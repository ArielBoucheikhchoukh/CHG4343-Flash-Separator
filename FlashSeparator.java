import java.text.DecimalFormat;

public abstract class FlashSeparator {

	public static final double ENTHALPY_BALANCE_MIN_X = 0.;
	public static final double ENTHALPY_BALANCE_MAX_X = 100000.;
	public static final double ENTHALPY_BALANCE_INCREMENT_LENGTH = 10.;
	public static final double ENTHALPY_BALANCE_TOLERANCE = 0.01;
	public static final int ENTHALPY_BALANCE_MAX_EVALUATION_COUNT = 500000;
	public static final double ENTHALPY_BALANCE_SUB_INCREMENT_FRACTION = 1.;

	private String type;
	private String status;
	private double T;
	private double P;
	private double Q;
	private Stream feedStream;
	private Stream flashStream;
	private Stream[] outletStreams;
	private Behaviour behaviour;
	
	
/**********************************************************************************************************************
* 1) Constructor
* ---------------------------------------------------------------------------------------------------------------------
*/
	public FlashSeparator(String type, double T, double P, Stream feedStream) {
		this.type = type;
		this.status = "";
		
		this.T = T;
		this.P = P;
		this.Q = 0.;

		this.feedStream = feedStream.clone();

		this.flashStream = feedStream.clone();
		this.flashStream.setName("Flash Stream");
		this.flashStream.setT(this.T, false);
		this.flashStream.setP(this.P);

		this.outletStreams = new Stream[2];
		this.outletStreams[0] = this.flashStream.clone();
		this.outletStreams[1] = this.flashStream.clone();
	}
/*********************************************************************************************************************/


/**********************************************************************************************************************
* 2) toString() : Returns the state of the FlashSeparator object in the form of a String.
* ---------------------------------------------------------------------------------------------------------------------
*/
	public String toString() {

		String message = new String();
		DecimalFormat formatter = new DecimalFormat("###,###,##0.00");

		String behaviourCase = new String();
		if (this.behaviour instanceof NonIdealBehaviour) {
			behaviourCase = "Non-Ideal Behaviour";
		} else {
			behaviourCase = "Ideal Behaviour";
		}

		message = "Flash Separator: " + this.type + " - " + behaviourCase + " \r\n" 
				+ "   Status: " + this.status + " \r\n"
				+ "   T = " + formatter.format(this.T) + " K \r\n" 
				+ "   P = " + formatter.format(this.P) + " bar \r\n" 
				+ "   Q = " + formatter.format(this.Q) + " J/h \r\n\r\n" 
				+ this.feedStream.toString() + "\r\n\r\n"
				+ this.flashStream.toString() + "\r\n\r\n" 
				+ "Outlet Streams: \r\n" 
				+ this.outletStreams[0].toString() + "\r\n" 
				+ this.outletStreams[1].toString() + "\r\n";

		return message;
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 3) flashCalculation() : .
* ---------------------------------------------------------------------------------------------------------------------
*/
	public abstract Stream[] flashCalculation() 
			throws FlashCalculationException, NumericalMethodException, 
				FunctionException, StreamException;
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 4) performFlash() : .
* ---------------------------------------------------------------------------------------------------------------------
*/
	protected Stream performFlash() 
			throws FlashCalculationException, NumericalMethodException, 
			FunctionException {

		double[] P_bounds = this.behaviour.bubbleDewPointPressures(this.flashStream.clone());
		/*System.out.println("Flash Pressure: " + flashStream.getP());
		System.out.println("Bubble Point Pressure: " + P_bounds[0]);
		System.out.println("Dew Point Pressure: " + P_bounds[1]);*/

		if (this.P < P_bounds[0] && this.P > P_bounds[1]) {
			this.flashStream = this.behaviour.performFlash(this.flashStream.clone());
			this.status = "Feed stream was flashed into liquid and vapour phase streams.";
		} else if (this.P <= P_bounds[1]) {
			this.flashStream.setVapourFraction(1.);
			for (int i = 0; i < this.flashStream.getComponentCount(); i++) {

				this.flashStream.setXi(0., i);

				if (this.flashStream.isComponentCondensable(i)) {
					this.flashStream.setYi(this.flashStream.getZi(i) 
							/ this.flashStream.getCondensableFraction(), i);
				} else {
					this.flashStream.setYi(this.flashStream.getZi(i), i);
				}
				
				DecimalFormat formatter = new DecimalFormat("###,###,##0.00");
				this.status = "Feed stream was completely boiled into a vapour phase stream. \r\n" 
						+ "Bubble point pressure: " + formatter.format(P_bounds[0]) + " bar \r\n" 
						+ "Dew point pressure: " + formatter.format(P_bounds[1]) + " bar \r\n";
			}
		} else if (this.P >= P_bounds[0]) {
			this.flashStream.setVapourFraction(0.);
			for (int i = 0; i < this.flashStream.getComponentCount(); i++) {
				if (this.flashStream.isComponentCondensable(i)) {
					this.flashStream.setXi(this.flashStream.getZi(i) / this.flashStream.getCondensableFraction(), i);
					this.flashStream.setYi(0., i);
				} else {
					this.flashStream.setXi(0., i);
					this.flashStream.setYi(this.flashStream.getZi(i), i);
				}
			}
			
			DecimalFormat formatter = new DecimalFormat("###,###,##0.00");
			this.status = "Feed stream remained in the liquid phase. \r\n" 
					+ "Bubble point pressure: " + formatter.format(P_bounds[0]) + " bar \r\n" 
					+ "Dew point pressure: " + formatter.format(P_bounds[1]) + " bar \r\n";
		}

		return this.flashStream.clone();
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 5) splitPhases() : Splits a stream into liquid (i = 0) and vapour/gas (i = 1) streams.
* ---------------------------------------------------------------------------------------------------------------------
*/
	protected Stream[] splitPhases() throws StreamException {

		int componentCountTotal = this.flashStream.getComponentCount();
		int componentCountLiquid = 0;
		int componentCountGas = 0;
		double F_liquid = 0.;
		double F_gas = 0.;

		for (int i = 0; i < componentCountTotal; i++) {
			if (this.flashStream.isComponentCondensable(i)) {
				if (this.flashStream.getXi(i) > 0) {
					componentCountLiquid++;
					F_liquid += this.flashStream.getCondensableFraction() * (1 - this.flashStream.getVapourFraction())
							* this.flashStream.getXi(i) * this.flashStream.getF();
				}
				if (this.flashStream.getYi(i) > 0) {
					componentCountGas++;
					F_gas += this.flashStream.getCondensableFraction() * this.flashStream.getVapourFraction()
							* this.flashStream.getYi(i) * this.flashStream.getF();
				}
			} else {
				F_gas += this.flashStream.getZi(i) * this.flashStream.getF();
				componentCountGas++;
			}
		}

		int liquidIndex = 0;
		int gasIndex = 0;
		double[] x = new double[componentCountLiquid];
		double[] y = new double[componentCountGas];
		int[][] speciesIndices = new int[2][];
		speciesIndices[0] = new int[componentCountLiquid];
		speciesIndices[1] = new int[componentCountGas];
		for (int i = 0; i < componentCountTotal; i++) {
			if (this.flashStream.isComponentCondensable(i)) {
				if (this.flashStream.getXi(i) > 0) {
					x[liquidIndex] = this.flashStream.getXi(i) * (1 - this.flashStream.getVapourFraction())
							* this.flashStream.getCondensableFraction() * this.flashStream.getF() / F_liquid;
					speciesIndices[0][liquidIndex] = this.flashStream.getSpeciesIndex(i);
					liquidIndex++;
				}
				if (this.flashStream.getYi(i) > 0) {
					y[gasIndex] = this.flashStream.getYi(i) * this.flashStream.getVapourFraction()
							* this.flashStream.getCondensableFraction() * this.flashStream.getF() / F_gas;
					speciesIndices[1][gasIndex] = this.flashStream.getSpeciesIndex(i);
					gasIndex++;
				}
			} else {
				this.flashStream.setXi(0., i);
				this.flashStream.setYi(0., i);
				y[gasIndex] = this.flashStream.getZi(i) * this.flashStream.getF() / F_gas;
				speciesIndices[1][gasIndex] = this.flashStream.getSpeciesIndex(i);
				gasIndex++;
			}
		}

		this.outletStreams[0] = new Stream("Liquid Phase", this.T, this.P, F_liquid, 0, x, null, x, 
				speciesIndices[0]);
		this.outletStreams[1] = new Stream("Vapour/Gas Phase", this.T, this.P, F_gas, 1, null, y, y, 
				speciesIndices[1]);

		return this.getOutletStreams();
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 6) selectReferenceTemperature() : Selects the lowest normal boiling point among all components in 
* 									the stream as the reference temperature for the enthalpy balance.
* ---------------------------------------------------------------------------------------------------------------------
*/
	public double selectReferenceTemperature() {

		double Tref = Double.MAX_VALUE;

		for (int i = 0; i < this.feedStream.getComponentCount(); i++) {
			Tref = Math.min(Tref, Menu.getSpecies(this.feedStream.getSpeciesIndex(i)).getTb());
		}

		return Tref;
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 7) setFeedStreamTemperature() : Sets the temperature of the feed stream.
* ---------------------------------------------------------------------------------------------------------------------
*/
	public void setFeedStreamTemperature(double T, boolean updatePhaseFractions) {
		this.feedStream.setT(T, updatePhaseFractions);
	}
/*********************************************************************************************************************/

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public double getT() {
		return this.T;
	}

	public void setT(double T) {
		this.T = T;
		this.flashStream.setT(T, false);
		this.outletStreams[0].setT(T, false);
		this.outletStreams[1].setT(T, false);
	}

	public double getP() {
		return this.P;
	}

	public void setP(double P) {
		this.P = P;
		this.flashStream.setP(P);
		this.outletStreams[0].setP(P);
		this.outletStreams[1].setP(P);
	}

	public double getQ() {
		return this.Q;
	}

	public void setQ(double Q) {
		this.Q = Q;
	}

	public Stream getFeedStream() {
		return this.feedStream.clone();
	}

	public void setFeedStream(Stream feedStream) {
		this.feedStream = feedStream.clone();
	}

	public Stream getFlashStream() {
		return this.flashStream.clone();
	}

	public void setFlashStream(Stream flashStream) {
		this.flashStream = flashStream.clone();
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

	protected Behaviour getBehaviour() {
		return this.behaviour.clone();
	}

	protected void setBehaviour(boolean nonIdealBehaviour) {
		if (!nonIdealBehaviour) {
			this.behaviour = new Behaviour();
		} else {
			this.behaviour = new NonIdealBehaviour();
		}
	}

}