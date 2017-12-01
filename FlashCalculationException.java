public class FlashCalculationException extends Exception {
	
	
	private double T;
	private double P;
	private double P_bp;
	private double P_dp;
	private double T_bp;
	private double T_dp;
	
	public FlashCalculationException(double T, double P, double P_bp, double P_dp, double T_bp, double T_dp) {
		super();
		this.T = T;
		this.P = P;
		this.P_bp = P_bp;
		this.P_dp = P_dp;
		this.T_bp = T_bp;
		this.T_dp = T_dp;
	}
	
	public String toString() {
		return " T = " + this.T + " K \r\n"
				+ " P = " + this.P + " bar \r\n"
				+ " bubble-point P = " + this.P_bp + " bar \r\n"
				+ " dew-point P = " + this.P_dp + " bar \r\n"
				+ " bubble-point T = " + this.T_bp + " K \r\n"
				+ " dew-point T = " + this.T_dp + " K \r\n";
	}
	
	public String getMessage() {
		return "FlashCalculationException: Flash not possible. \r\n" + this.toString();
	}
	
	public double getT() {
		return this.T;
	}

	public void setT(double T) {
		this.T = T;
	}

	public double getP() {
		return this.P;
	}

	public void setP(double P) {
		this.P = P;
	}

	public double getP_bp() {
		return this.P_bp;
	}

	public void setP_bp(double P_bp) {
		this.P_bp = P_bp;
	}

	public double getP_dp() {
		return this.P_dp;
	}

	public void setP_dp(double P_dp) {
		this.P_dp = P_dp;
	}

	public double getT_bp() {
		return this.T_bp;
	}

	public void setT_bp(double T_bp) {
		this.T_bp = T_bp;
	}

	public double getT_dp() {
		return this.T_dp;
	}

	public void setT_dp(double T_dp) {
		this.T_dp = T_dp;
	}
	
}