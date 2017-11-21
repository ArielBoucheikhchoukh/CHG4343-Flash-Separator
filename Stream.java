import java.text.DecimalFormat;

public class Stream {

	private String name;
	private double T; // [K]
	private double P; // [bar]
	private double F; // [mol/h]
	private double condensableFraction; // mole fraction
	private double vapourFraction; // mole fraction; pertains only to the condensable fraction
	private double[] x;
	private double[] y;
	private double[] z;
	private boolean[] isCondensable;
	private int[] speciesIndices;

	
/**********************************************************************************************************************
* 1.1) Constructor A: Sets all instance variables to default values.
* ---------------------------------------------------------------------------------------------------------------------
*/
	public Stream(int componentCount) {
		this.name = "Stream";
		this.T = 273.15;
		this.P = 10.;
		this.F = 0.;
		this.condensableFraction = 1.;
		this.vapourFraction = 0.;
		this.x = new double[componentCount];
		this.y = new double[componentCount];
		this.z = new double[componentCount];
		this.isCondensable = new boolean[componentCount];
		this.speciesIndices = new int[componentCount];
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 1.2) Constructor B: Used by FlashSeparator children to construct their feed stream objects. 
* 						The feed is set to a liquid-phase stream by default.
* ---------------------------------------------------------------------------------------------------------------------
*/
	public Stream(String name, double T, double F, double[] z, int[] speciesIndices) 
			throws StreamException {
		this.name = name;
		this.T = T;
		this.P = 10.;
		this.F = F;

		this.condensableFraction = 1.;
		this.vapourFraction = 0.;

		int componentCount = speciesIndices.length;
		if (z.length != componentCount) {
			throw new StreamCompositionException(this.clone());
		}
		
		double moleFractionSum = 0.;
		for (int i = 0; i < componentCount; i++) {
			moleFractionSum += z[i];
		}
		if (Math.abs(moleFractionSum - 1.0) > 0.001) {
			if (this.F > 0.001 || this.F < -0.001) {
				throw new StreamCompositionException(this.clone());
			}
		}
		
		this.x = new double[componentCount];
		this.y = new double[componentCount];
		this.z = z.clone();
		this.isCondensable = new boolean[componentCount];
		this.speciesIndices = speciesIndices.clone();

		this.updateCondensableState(false);

		for (int i = 0; i < componentCount; i++) {
			if (this.isCondensable[i]) {
				this.x[i] = this.z[i] / this.condensableFraction;
			}
		}
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 1.3) Constructor C: Used by FlashSeparator to construct outlet streams.
* 						Define the number and type of phases in the stream via the phaseIndex
* 						variable: 
* 							Liquid only: phaseIndex = 0 
* 							Vapour/Gas only: phaseIndex = 1 
* 							Liquid, Vapour and Gas: phaseIndex = 2
* ---------------------------------------------------------------------------------------------------------------------
*/
	public Stream(String name, double T, double P, double F, int phaseIndex, double[] x, double[] y, 
			double[] z, int[] speciesIndices) throws StreamException {

		this.name = name;
		this.T = T;
		this.P = P;
		this.F = F;

		this.vapourFraction = 0.;

		int componentCount = speciesIndices.length;
		if (z.length != componentCount) {
			throw new StreamCompositionException(this.clone());
		} else if (x != null) {
			if (x.length != componentCount) {
				throw new StreamCompositionException(this.clone());
			}
		} else if (y != null) {
			if (y.length != componentCount) {
				throw new StreamCompositionException(this.clone());
			}
		}
		
		double moleFractionSum = 0.;
		for (int i = 0; i < componentCount; i++) {
			moleFractionSum += z[i];
		}
		if (Math.abs(moleFractionSum - 1.0) > 0.001) {
			if (this.F > 0.001 || this.F < -0.001) {
				throw new StreamCompositionException(this.clone());
			}
		}
		
		switch (phaseIndex) {

		case 0:
			this.x = x.clone();
			this.y = new double[componentCount];
			this.z = z.clone();
			break;

		case 1:
			this.x = new double[componentCount];
			this.y = y.clone();
			this.z = z.clone();
			break;

		case 2:
			this.x = x.clone();
			this.y = y.clone();
			this.z = z.clone();
			break;

		default:
			break;
		}

		this.isCondensable = new boolean[componentCount];
		this.speciesIndices = speciesIndices.clone();

		this.updateCondensableState(false);
		this.calculateVapourFraction();
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 2) Copy Constructor
* ---------------------------------------------------------------------------------------------------------------------
*/
	public Stream(Stream source) {
		this.name = source.name;
		this.T = source.T;
		this.P = source.P;
		this.F = source.F;
		this.condensableFraction = source.condensableFraction;
		this.vapourFraction = source.vapourFraction;
		if (source.x != null) {
			this.x = source.x.clone();
		}
		if (source.y != null) {
			this.y = source.y.clone();
		}
		if (source.z != null) {
			this.z = source.z.clone();
		}
		if (source.isCondensable != null) {
			this.isCondensable = source.isCondensable.clone();
		}
		if (source.speciesIndices != null) {
			this.speciesIndices = source.speciesIndices.clone();
		}
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 3) clone()
* ---------------------------------------------------------------------------------------------------------------------
*/
	public Stream clone() {
		return new Stream(this);
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 4) toString()
* ---------------------------------------------------------------------------------------------------------------------
*/
	public String toString() {

		String message = new String();
		DecimalFormat formatter = new DecimalFormat("###,###,##0.00");

		message = this.name + ": \r\n" + "   T = " + formatter.format(this.T) + " K \r\n" + "   P = "
				+ formatter.format(this.P) + " bar \r\n" + "   F = " + formatter.format(this.F) + " mol/h \r\n"
				+ "   Fraction of condensable moles = " + formatter.format(this.condensableFraction * 100.) + " % \r\n"
				+ "   Fraction of condensable moles in the vapour phase = "
				+ formatter.format(this.vapourFraction * 100.) + " % \r\n" + "   Components: \r\n";

		for (int i = 0; i < this.getComponentCount(); i++) {

			message += "      " + (i + 1) + ". " + Menu.getSpecies(this.getSpeciesIndex(i)).getName() + ": \r\n"
					+ "         x = " + formatter.format(this.x[i] * 100) + "% \r\n" + "         y = "
					+ formatter.format(this.y[i] * 100) + "% \r\n" + "         z = " + formatter.format(this.z[i] * 100)
					+ "% \r\n";

		}

		return message;
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 5) evaluateStreamEnthalpy()
* ---------------------------------------------------------------------------------------------------------------------
*/
	public double evaluateStreamEnthalpy(double Tref, boolean derivative) throws FunctionException {
		double H = 0.;
		for (int i = 0; i < this.speciesIndices.length; i++) {

			double hL_i = 0.;
			double Hv_i = 0.;
			Species species_i = Menu.getSpecies(this.speciesIndices[i]);
			//System.out.println("Test - Stream Class - evaluateStreamEnthalpy: Species = " + species_i.getName());
			if (this.isCondensable[i]) {

				if (this.x[i] > 0) {
					hL_i = species_i.evaluateEnthalpyLiquid(this.T, Tref, derivative);
					//System.out.println("Test - Stream Class - evaluateStreamEnthalpy: hL = " +hL_i);
				}
				if (this.y[i] > 0) {
					Hv_i = species_i.evaluateEnthalpyVapour(this.T, Tref, derivative);
					//System.out.println("Test - Stream Class - evaluateStreamEnthalpy: Hv = " +Hv_i);
				}

				H += this.condensableFraction * this.F
						* (this.x[i] * (1 - this.vapourFraction) * hL_i + this.y[i] * this.vapourFraction * Hv_i);
			} else {
				Hv_i = species_i.evaluateEnthalpyVapour(this.T, Tref, derivative);
				//System.out.println("Test - Stream Class - evaluateStreamEnthalpy: Hv = " + Hv_i);
				H += this.z[i] * this.F * Hv_i;
			}
		}

		/*
		 * System.out.
		 * println("Test - Stream Class - evaluateStreamEnthalpy: cdFraction = " +
		 * this.condensableFraction); System.out.
		 * println("Test - Stream Class - evaluateStreamEnthalpy: vapourFraction = " +
		 * this.vapourFraction);
		 * System.out.println("Test - Stream Class - evaluateStreamEnthalpy: F = " +
		 * this.F);
		 * System.out.println("Test - Stream Class - evaluateStreamEnthalpy: H = " + H);
		 */
		return H;
	}
/*********************************************************************************************************************/

	
	public int getComponentCount() {
		return this.speciesIndices.length;
	}

	public void updateCondensableState(boolean updatePhaseFractions) {
		double oldCdFraction = this.condensableFraction;
		this.condensableFraction = 0.;
		for (int i = 0; i < this.getComponentCount(); i++) {
			if (this.T < Menu.getSpecies(this.speciesIndices[i]).getTc()) {
				this.condensableFraction += this.z[i];
				this.isCondensable[i] = true;
			} else {
				this.isCondensable[i] = false;
			}
		}
		
		if (updatePhaseFractions) {
			double oldVpFraction = this.vapourFraction;
			this.vapourFraction = 0.;
			if (this.condensableFraction > 0.) {
				for (int i = 0; i < this.getComponentCount(); i++) {
					if (this.isCondensable[i]) {
						this.vapourFraction += this.y[i] * oldVpFraction * oldCdFraction;
					}
				}
				this.vapourFraction = this.vapourFraction / this.condensableFraction;
			}
			
			for (int i = 0; i < this.getComponentCount(); i++) {
				if (this.isCondensable[i]) {
					if (this.vapourFraction < 1.) {
						this.x[i] = this.x[i] * (1. - oldVpFraction) * oldCdFraction 
								/ ((1. - this.vapourFraction) * this.condensableFraction);
					} else if (this.vapourFraction > 0.) {
					this.y[i] = this.y[i] * (oldVpFraction) * oldCdFraction 
							/ ((this.vapourFraction) * this.condensableFraction);
					}
				} else {
					this.isCondensable[i] = false;
					this.x[i] = 0.;
					this.y[i] = 0.;
				}
			}
		}
	}

	public void calculateVapourFraction() {

		double F_vapour = 0.;

		for (int i = 0; i < this.getComponentCount(); i++) {
			F_vapour += this.y[i] * this.condensableFraction * this.F;
		}

		this.vapourFraction = F_vapour / (this.condensableFraction * this.F);
		if (Double.isNaN(this.vapourFraction)) {
			this.vapourFraction = 0.;
		}
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getT() {
		return this.T;
	}

	public void setT(double T, boolean updatePhaseFractions) {
		this.T = T;
		this.updateCondensableState(updatePhaseFractions);
	}

	public double getP() {
		return this.P;
	}

	public void setP(double P) {
		this.P = P;
	}

	public double getF() {
		return this.F;
	}

	public void setF(double F) {
		this.F = F;
	}

	public double getCondensableFraction() {
		return this.condensableFraction;
	}

	public void setCondensableFraction(double condensableFraction) {
		this.condensableFraction = condensableFraction;
	}

	public double getVapourFraction() {
		return this.vapourFraction;
	}

	public void setVapourFraction(double vapourFraction) {
		this.vapourFraction = vapourFraction;
	}

	public double[] getX() {
		return this.x.clone();
	}

	public void setX(double[] x) throws StreamCompositionException {
		this.x = x.clone();
		
		if (x.length != this.speciesIndices.length) {
			throw new StreamCompositionException(this.clone());
		}
		
		double moleFractionSum = 0.;
		for (int i = 0; i < this.speciesIndices.length; i++) {
			moleFractionSum += x[i];
		}
		if (Math.abs(moleFractionSum - 1.0) > 0.001) {
			if (this.F > 0.001 || this.F < -0.001) {
				throw new StreamCompositionException(this.clone());
			}
		}
	}

	public double getXi(int componentIndex) {
		return this.x[componentIndex];
	}

	public void setXi(double x_i, int componentIndex) {
		this.x[componentIndex] = x_i;
	}

	public double[] getY() {
		return this.x.clone();
	}

	public void setY(double[] y) throws StreamCompositionException {
		this.y = y.clone();
		
		if (y.length != this.speciesIndices.length) {
			throw new StreamCompositionException(this.clone());
		}
		
		double moleFractionSum = 0.;
		for (int i = 0; i < this.speciesIndices.length; i++) {
			moleFractionSum += y[i];
		}
		if (Math.abs(moleFractionSum - 1.0) > 0.001) {
			if (this.F > 0.001 || this.F < -0.001) {
				throw new StreamCompositionException(this.clone());
			}
		}
	}

	public double getYi(int componentIndex) {
		return this.y[componentIndex];
	}

	public void setYi(double y_i, int componentIndex) {
		this.y[componentIndex] = y_i;
	}

	public double[] getZ() {
		return this.z.clone();
	}

	public void setZ(double[] z) throws StreamCompositionException {
		this.z = z.clone();
		
		if (z.length != this.speciesIndices.length) {
			throw new StreamCompositionException(this.clone());
		}
		
		double moleFractionSum = 0.;
		for (int i = 0; i < this.speciesIndices.length; i++) {
			moleFractionSum += z[i];
		}
		if (Math.abs(moleFractionSum - 1.0) > 0.001) {
			if (this.F > 0.001 || this.F < -0.001) {
				throw new StreamCompositionException(this.clone());
			}
		}
	}

	public double getZi(int componentIndex) {
		return this.z[componentIndex];
	}

	public void setZi(double z_i, int componentIndex) {
		this.z[componentIndex] = z_i;
	}

	public int[] getSpeciesIndices() {
		return this.speciesIndices.clone();
	}

	public void setSpeciesIndices(int[] speciesIndices) {
		this.speciesIndices = speciesIndices.clone();
	}

	public int getSpeciesIndex(int componentIndex) {
		return this.speciesIndices[componentIndex];
	}

	public void setSpeciesIndex(int speciesIndex, int componentIndex) {
		this.speciesIndices[componentIndex] = speciesIndex;
	}

	public boolean[] getIsCondensable() {
		return this.isCondensable.clone();
	}

	public void setIsCondensable(boolean[] isCondensable) throws StreamException {
		this.isCondensable = isCondensable.clone();
		if (this.isCondensable.length != this.getComponentCount()) {
			throw new StreamCompositionException(this.clone());
		}
	}

	public boolean isComponentCondensable(int componentIndex) {
		return this.isCondensable[componentIndex];
	}

	public void setComponentCondensableState(boolean isCondensable, int componentIndex) {
		this.isCondensable[componentIndex] = isCondensable;
	}

}
