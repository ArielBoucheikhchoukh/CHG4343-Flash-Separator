public class DewPoint extends BoundedFunction {

	private Stream stream;

	public DewPoint(Stream stream) {
		super("Dew Point of " + stream.getName(), 0., 1.);
		this.stream = stream.clone();

		double minX = Double.MIN_VALUE;
		double maxX = Double.MAX_VALUE;
		int[] speciesIndices = stream.getSpeciesIndices();
		for (int i = 0; i < speciesIndices.length; i++) {
			minX = Math.max(minX, Menu.getSpecies(speciesIndices[i]).getCorrelation(Species.VAPOUR_PRESSURE).getMinX());
			maxX = Math.min(maxX, Menu.getSpecies(speciesIndices[i]).getCorrelation(Species.VAPOUR_PRESSURE).getMaxX());
		}

		super.setMinX(minX);
		super.setMaxX(maxX);
	}

	public double evaluateWithinBounds(double x, double[] constants) throws FunctionException {
		return stream.getP() - this.calculateDewPointPressure(x, false);
	}

	public double evaluateDerivativeWithinBounds(double x, double[] constants) throws FunctionException {

		double num = 0.;
		for (int i = 0; i < this.stream.getComponentCount(); i++) {
			Species component = Menu.getSpecies(this.stream.getSpeciesIndex(i));
			if (x < component.getTc()) {
				num += (stream.getZi(i) / stream.getCondensableFraction()) * component.evaluateVapourPressure(x, true)
						* Math.pow(component.evaluateVapourPressure(x, false), 2);
			}
		}

		double dem = this.calculateDewPointPressure(x, false);

		return stream.getP() - num / dem;
	}

	public double calculateDewPointPressure(double T, boolean derivative) throws FunctionException {

		double P_dp = 0.;
		for (int i = 0; i < this.stream.getComponentCount(); i++) {
			Species component = Menu.getSpecies(this.stream.getSpeciesIndex(i));
			if (T < component.getTc()) {
				P_dp += (stream.getZi(i) / stream.getCondensableFraction())
						/ component.evaluateVapourPressure(T, false);
			}
		}
		P_dp = 1. / P_dp;

		return P_dp;
	}

}