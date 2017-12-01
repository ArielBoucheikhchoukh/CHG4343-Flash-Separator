public class RachfordRice extends BoundedFunction {

	private double[] z;
	private double[] K;
	private boolean[] isCondensable;


/**********************************************************************************************************************
* 1) Constructor
* ---------------------------------------------------------------------------------------------------------------------
*/
	public RachfordRice(double[] z, double[] K, boolean[] isCondensable) {
		super("Rachford-Rice", 0., 1.);
		this.z = z.clone();
		this.K = K.clone();
		this.isCondensable = isCondensable.clone();
	}
/*********************************************************************************************************************/


/**********************************************************************************************************************
* 2) Copy Constructor
* ---------------------------------------------------------------------------------------------------------------------
*/
	public RachfordRice(RachfordRice source) {
		super(source);
		this.z = source.z.clone();
		this.K = source.K.clone();
		this.isCondensable = source.isCondensable.clone();
	}
/*********************************************************************************************************************/


/**********************************************************************************************************************
* 3) clone()
* ---------------------------------------------------------------------------------------------------------------------
*/
	public RachfordRice clone() {
		return new RachfordRice(this);
	}
/*********************************************************************************************************************/


/**********************************************************************************************************************
* 4) evaluateWithinBounds()
* ---------------------------------------------------------------------------------------------------------------------
*/
	public double evaluateWithinBounds(double x, double[] constants) {

		double f = 0.;

		for (int i = 0; i < this.z.length; i++) {
			if (this.isCondensable[i]) {
				f += (this.z[i] * (this.K[i] - 1)) / (1 + (this.K[i] - 1) * x);
			}
		}

		return f;
	}
/*********************************************************************************************************************/


/**********************************************************************************************************************
* 5) evaluateDerivativeWithinBounds()
* ---------------------------------------------------------------------------------------------------------------------
*/
	public double evaluateDerivativeWithinBounds(double x, double[] constants) {

		double df = 0.;

		for (int i = 0; i < this.z.length; i++) {
			if (this.isCondensable[i]) {
				df -= this.z[i] * Math.pow((this.K[i] - 1) / (1 + (this.K[i] - 1) * x), 2);
			}
		}

		return df;
	}
/*********************************************************************************************************************/

	
	public double[] getZ() {
		return z.clone();
	}

	public void setZ(double[] z) {
		this.z = z.clone();
	}

	public double[] getK() {
		return this.K.clone();
	}

	public void setK(double[] K) {
		this.K = K.clone();
	}

	public boolean[] getIsCondensable() {
		return this.isCondensable;
	}

	public void setIsCondensable(boolean[] isCondensable) {
		this.isCondensable = isCondensable.clone();
	}

}