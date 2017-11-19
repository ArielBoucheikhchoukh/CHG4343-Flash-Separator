public class Behaviour implements Cloneable {

	public static final double BUBBLE_DEW_POINT_INCREMENT_LENGTH = 10.;
	public static final double BUBBLE_DEW_POINT_TOLERANCE = 0.01;
	public static final int BUBBLE_DEW_POINT_MAX_EVALUATION_COUNT = 100000;
	public static final double RACHFORD_RICE_INCREMENT_LENGTH = 0.1;
	public static final double RACHFORD_RICE_TOLERANCE = 0.0001;
	public static final int RACHFORD_RICE_MAX_EVALUATION_COUNT = 100000;

	
/**********************************************************************************************************************
* 1) clone()
* ---------------------------------------------------------------------------------------------------------------------
*/
	public Behaviour clone() {
		try {
			return (Behaviour) super.clone(); // might be subject to change
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 2) performFlash()
* ---------------------------------------------------------------------------------------------------------------------
*/
	public Stream performFlash(Stream flashStream)
			throws FlashCalculationException, NumericalMethodException, FunctionException {

		int componentCount = flashStream.getComponentCount();
		int condensableCount = 0;
		Species[] components = new Species[componentCount];
		for (int i = 0; i < componentCount; i++) {
			components[i] = Menu.getSpecies(flashStream.getSpeciesIndex(i));
			if (flashStream.isComponentCondensable(i)) {
				condensableCount++;
			}
		}

		int j = 0;
		double[] z_cd = new double[condensableCount];
		for (int i = 0; i < componentCount; i++) {
			if (flashStream.isComponentCondensable(i)) {
				z_cd[j] = flashStream.getZi(i) / flashStream.getCondensableFraction();
				j++;
			}
		}

		double[] K = this.calculatePartitionCoefficients(flashStream, components, condensableCount);
		// System.out.println("First component: z = " + z_cd[0] + ", and K = " + K[0]);
		// System.out.println("Second component: z = " + z_cd[1] + ", and K = " + K[1]);
		Function rachfordRice = new RachfordRice(z_cd, K);
		double vapourFraction = 0.;
		try {
			vapourFraction = Menu.findRoot(rachfordRice, null, 0., 1., 
					Behaviour.RACHFORD_RICE_INCREMENT_LENGTH, Behaviour.RACHFORD_RICE_TOLERANCE, 
					Behaviour.RACHFORD_RICE_MAX_EVALUATION_COUNT);
		} catch (NumericalMethodException e) {

			throw new FlashNotPossibleException();
		}

		flashStream.setVapourFraction(vapourFraction);

		j = 0;
		for (int i = 0; i < componentCount; i++) {
			if (flashStream.isComponentCondensable(i)) {
				double x = z_cd[j] / (1 + (K[j] - 1) * vapourFraction);
				double y = x * K[j];

				flashStream.setXi(x, i);
				flashStream.setYi(y, i);

				j++;
			} else {
				flashStream.setXi(0, i);
				flashStream.setYi(0, i);
			}
		}

		return flashStream.clone();
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 3) bubbleDewPointPressures() : Calculates and returns the bubble point and
* 									dew point pressures of a stream.
* ---------------------------------------------------------------------------------------------------------------------
*/
	public double[] bubbleDewPointPressures(Stream stream) 
			throws NumericalMethodException, FunctionException {

		BubblePoint bubblePoint = new BubblePoint(stream);
		DewPoint dewPoint = new DewPoint(stream);

		double P_bp = bubblePoint.calculateBubblePointPressure(stream.getT(), false);
		double P_dp = dewPoint.calculateDewPointPressure(stream.getT(), false);

		return new double[] { P_bp, P_dp };
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 4) bubbleDewPointTemperatures() : Calculates and returns the bubble point and
* 									dew point temperatures of a stream.
* ---------------------------------------------------------------------------------------------------------------------
*/
	public double[] bubbleDewPointTemperatures(Stream stream) 
			throws NumericalMethodException, FunctionException {

		BoundedFunction bubblePoint = new BubblePoint(stream);
		BoundedFunction dewPoint = new DewPoint(stream);

		double T_bp = Menu.findRoot(bubblePoint, null, bubblePoint.getMinX(), true, 
				Behaviour.BUBBLE_DEW_POINT_INCREMENT_LENGTH, Behaviour.BUBBLE_DEW_POINT_TOLERANCE, 
				Behaviour.BUBBLE_DEW_POINT_MAX_EVALUATION_COUNT, false);
		double T_dp = Menu.findRoot(dewPoint, null, dewPoint.getMinX(), true,
				Behaviour.BUBBLE_DEW_POINT_INCREMENT_LENGTH, Behaviour.BUBBLE_DEW_POINT_TOLERANCE, 
				Behaviour.BUBBLE_DEW_POINT_MAX_EVALUATION_COUNT, false);

		return new double[] { T_bp, T_dp };
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 5) calculatePartitionCoefficients() : .
* ---------------------------------------------------------------------------------------------------------------------
*/
	protected double[] calculatePartitionCoefficients(Stream stream, Species[] components, 
			int condensableCount) throws FunctionException {

		int j = 0;
		double[] K = new double[condensableCount];
		for (int i = 0; i < components.length; i++) {
			if (stream.isComponentCondensable(i)) {
				K[j] = components[i].evaluateVapourPressure(stream.getT(), false) / stream.getP();
				// System.out.println("K value of " +components[i].getName() + ": " + K[j]);
				j++;
			}
		}

		return K;
	}
/*********************************************************************************************************************/

	
/**********************************************************************************************************************
* 6) evaluateStreamEnthalpy() : 
* ---------------------------------------------------------------------------------------------------------------------
*/
	public double evaluateStreamEnthalpy(double Tref, Stream stream, boolean derivative) 
	throws FunctionException {
		return stream.evaluateStreamEnthalpy(Tref, derivative);
	}
/*********************************************************************************************************************/

}