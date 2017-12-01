import java.text.DecimalFormat;

public class Stream {
  
  private String name;
  private double T; // [K]
  private double P; // [bar]
  private double F; // [mol/h]
  private int[] speciesIndices;
  private double condensableFraction; // mole fraction
  private double vapourFraction; // mole fraction; pertains only to the condensable fraction
  private double P_bp;
  private double P_dp;
  private double[] x;
  private double[] y;
  private double[] z;
  private double[] K;
  private double[] gamma;
  private double[] phi;
  private double[][] omega_ij;
  private double[][] Tc_ij;
  private double[][] Pc_ij;
  private double[][] Zc_ij;
  private double[][] Vc_ij;
  private boolean[] isCondensable;
  
  
  /**********************************************************************************************************************
    * 1.1) Constructor A: Sets all instance variables to default values.
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public Stream(int componentCount) {
    this.name = "Stream";
    this.T = 273.15;
    this.P = 10.;
    this.F = 0.;
    this.speciesIndices = new int[componentCount];
    this.condensableFraction = 1.;
    this.vapourFraction = 0.;
    this.P_bp = 0.;
    this.P_dp = 0.;
    this.x = new double[componentCount];
    this.y = new double[componentCount];
    this.z = new double[componentCount];
    this.K = new double[componentCount];
    this.gamma = new double[componentCount];
    this.phi = new double[componentCount];
    this.omega_ij = new double[componentCount][componentCount];
    this.Tc_ij = new double[componentCount][componentCount];
    this.Pc_ij = new double[componentCount][componentCount];
    this.Zc_ij = new double[componentCount][componentCount];
    this.Vc_ij = new double[componentCount][componentCount];
    this.isCondensable = new boolean[componentCount];
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 1.2) Constructor B: Used by FlashSeparator children to construct their feed stream objects. 
    *       The feed is set to a liquid-phase stream by default.
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public Stream(String name, double T, double F, double[] z, int[] speciesIndices) 
    throws StreamException {
    
    this.name = name;
    this.T = T;
    this.P = 10.;
    this.F = F;
    this.speciesIndices = speciesIndices.clone();
    
    this.condensableFraction = 1.;
    this.vapourFraction = 0.;
    
    this.P_bp = 0.;
    this.P_dp = 0.;
    
    int componentCount = speciesIndices.length;
    if (z.length != componentCount) {
      throw new StreamCompositionException(this.clone(), "z");
    }
    
    this.x = new double[componentCount];
    this.y = new double[componentCount];
    this.setZ(z);
    this.K = new double[componentCount];
    this.gamma = new double[componentCount];
    this.phi = new double[componentCount];
    
    this.isCondensable = new boolean[componentCount];
    this.updateCondensableState(false);
    
    this.generateMixtureParameters();
    
    for (int i = 0; i < componentCount; i++) {
      if (this.isCondensable[i]) {
        this.x[i] = this.z[i] / this.condensableFraction;
        this.y[i] = 0.;
      }
      else {
        this.x[i] = 0.;
        this.y[i] = 0.;
      }
      
      this.K[i] = 0.;
      this.gamma[i] = 1.;
      this.phi[i] = 1.;
    }
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 1.3) Constructor C: Used by FlashSeparator to construct outlet streams.
    *       Define the number and type of phases in the stream via the phaseIndex variable: 
    *       Liquid only: phaseIndex = 0 
    *       Vapour/Gas only: phaseIndex = 1 
    *       Liquid, Vapour and Gas: phaseIndex = 2
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public Stream(String name, double T, double P, double F, int phaseIndex, 
                double vapourFraction, double P_bp, double P_dp,
                double[] x, double[] y, double[] z, double[] K, double[] gamma, double[] phi, 
                int[] speciesIndices) throws StreamException {
    
    this.name = name;
    this.T = T;
    this.P = P;
    this.F = F;
    this.speciesIndices = speciesIndices.clone();
    
    this.P_bp = 0.;
    this.P_dp = 0.;
    
    int componentCount = speciesIndices.length;
    if (z.length != componentCount) {
      throw new StreamCompositionException(this.clone(), "z");
    } else if (x != null) {
      if (x.length != componentCount) {
        throw new StreamCompositionException(this.clone(), "x");
      }
    } else if (y != null) {
      if (y.length != componentCount) {
        throw new StreamCompositionException(this.clone(), "y");
      }
    }
    else if (K != null) {
      if (K.length != componentCount) {
        throw new StreamCompositionException(this.clone(), "K");
      }
    }
    else if (gamma != null) {
      if (gamma.length != componentCount) {
        throw new StreamCompositionException(this.clone(), "gamma");
      }
    }
    else if (phi != null) {
      if (phi.length != componentCount) {
        throw new StreamCompositionException(this.clone(), "phi");
      }
    }
    
    switch (phaseIndex) {
      case 0:
        vapourFraction = 0.;
        
        this.setX(x);
        this.y = new double[componentCount];
        this.setZ(z);
        
        this.K = new double[componentCount];
        this.gamma = new double[componentCount];
        this.phi = new double[componentCount];
        for (int i = 0; i < componentCount; i++) {
          this.K[i] = 0.;
          this.gamma[i] = 1.;
          this.phi[i] = 1.;
        }
        
        break;
        
      case 1:
        vapourFraction = 1.;
        
        this.x = new double[componentCount];
        this.setY(y);
        this.setZ(z);
        
        this.K = new double[componentCount];
        this.gamma = new double[componentCount];
        this.phi = new double[componentCount];
        for (int i = 0; i < componentCount; i++) {
          this.K[i] = 0.;
          this.gamma[i] = 1.;
          this.phi[i] = 1.;
        }
        
        break;
        
      case 2:
        this.vapourFraction = vapourFraction;
        this.P_bp = P_bp;
        this.P_dp = P_dp;
        this.setX(x);
        this.setY(y);
        this.setZ(z);
        this.K = K.clone();
        this.gamma = gamma.clone();
        this.phi = phi.clone();
        break;
        
      default:
        break;
    }
    
    this.generateMixtureParameters();
    
    this.isCondensable = new boolean[componentCount];
    this.updateCondensableState(false);
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
    this.P_bp = source.P_bp;
    this.P_dp = source.P_dp;
    
    int componentCount = source.getComponentCount();
    if (source.speciesIndices != null) {
      this.speciesIndices = source.speciesIndices.clone();
    }
    if (source.x != null) {
      this.x = source.x.clone();
    }
    if (source.y != null) {
      this.y = source.y.clone();
    }
    if (source.z != null) {
      this.z = source.z.clone();
    }
    if (source.K != null) {
      this.K = source.K.clone();
    }
    if (source.gamma != null) {
      this.gamma = source.gamma.clone();
    }
    if (source.phi != null) {
      this.phi = source.phi.clone();
    }
    if (source.omega_ij != null) {
      this.omega_ij = new double[componentCount][];
      for (int i = 0; i < componentCount; i++) {
        this.omega_ij[i] = source.omega_ij[i].clone();
      }
    }
    if (source.Tc_ij != null) {
      this.Tc_ij = new double[componentCount][];
      for (int i = 0; i < componentCount; i++) {
        this.Tc_ij[i] = source.Tc_ij[i].clone();
      }
    }
    if (source.Pc_ij != null) {
      this.Pc_ij = new double[componentCount][];
      for (int i = 0; i < componentCount; i++) {
        this.Pc_ij[i] = source.Pc_ij[i].clone();
      }
    }
    if (source.Zc_ij != null) {
      this.Zc_ij = new double[componentCount][];
      for (int i = 0; i < componentCount; i++) {
        this.Zc_ij[i] = source.Zc_ij[i].clone();
      }
    }
    if (source.Vc_ij != null) {
      this.Vc_ij = new double[componentCount][];
      for (int i = 0; i < componentCount; i++) {
        this.Vc_ij[i] = source.Vc_ij[i].clone();
      }
    }
    if (source.isCondensable != null) {
      this.isCondensable = source.isCondensable.clone();
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
    
    message = this.name + ": \r\n" + "   T = " + formatter.format(this.T) + " K \r\n" 
      + "   P = " + formatter.format(this.P) + " bar \r\n" 
      + "   F = " + formatter.format(this.F) + " mol/h \r\n"
      + "   Fraction of condensable moles = " + formatter.format(this.condensableFraction * 100.) 
      + " % \r\n"
      + "   Fraction of condensable moles in the vapour phase = " 
      + formatter.format(this.vapourFraction * 100.) + " % \r\n";
    
    if (P_bp > 0.) {
      message += "   Bubble-point P = " + formatter.format(this.P_bp) + " bar \r\n";
    }
    if (P_dp > 0.) {
      message += "   Dew-point P = " + formatter.format(this.P_dp) + " bar \r\n";
    }
    
    message += "   Components: \r\n";
    for (int i = 0; i < this.getComponentCount(); i++) {
      message += "      " + (i + 1) + ". " + Menu.getSpecies(this.getSpeciesIndex(i)).getName() 
        + " (" + this.getSpeciesIndex(i) + ") " + ": \r\n"
        + "            x = " + formatter.format(this.x[i] * 100) + "% \r\n" 
        + "            y = " + formatter.format(this.y[i] * 100) + "% \r\n" 
        + "            z = " + formatter.format(this.z[i] * 100) + "% \r\n";
      
      if (this.K[i] > 0.) {
        message += "            K = " + formatter.format(this.K[i]) + " \r\n";
      }
      if (this.gamma[i] > 0.) {
        message += "            gamma = " + formatter.format(this.gamma[i]) + " \r\n";
      }
      if (this.phi[i] > 0.) {
        message += "            phi = " + formatter.format(this.phi[i]) + " \r\n";
      }
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
      // System.out.println("Test - Stream Class - evaluateStreamEnthalpy: Species = "
      // + species_i.getName());
      if (this.isCondensable[i]) {
        
        if (this.x[i] > 0) {
          hL_i = species_i.evaluateEnthalpyLiquid(this.T, Tref, derivative);
          // System.out.println("Test - Stream Class - evaluateStreamEnthalpy: hL = "
          // +hL_i);
        }
        if (this.y[i] > 0) {
          Hv_i = species_i.evaluateEnthalpyVapour(this.T, Tref, this.P, derivative);
          // System.out.println("Test - Stream Class - evaluateStreamEnthalpy: Hv = "
          // +Hv_i);
        }
        
        H += this.condensableFraction * this.F
          * (this.x[i] * (1 - this.vapourFraction) * hL_i + this.y[i] * this.vapourFraction * Hv_i);
      } else {
        Hv_i = species_i.evaluateEnthalpyVapour(this.T, Tref, this.P, derivative);
        // System.out.println("Test - Stream Class - evaluateStreamEnthalpy: Hv = " +
        // Hv_i);
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
  
  
  /**********************************************************************************************************************
    * 6) getComponentCount(): Returns the number of components in the stream.
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public int getComponentCount() {
    return this.speciesIndices.length;
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 7) updateCondensableState(): Checks whether each component is condensable at the current T, and updates the
    *         isCondensable array accordingly.
    * ---------------------------------------------------------------------------------------------------------------------
    */
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
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 8) generateMixtureParameters() : Calculates and stores the critical interactions.
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public void generateMixtureParameters() {
    
    int componentCount = this.getComponentCount();
    this.omega_ij = new double[componentCount][componentCount];
    this.Tc_ij = new double[componentCount][componentCount];
    this.Pc_ij = new double[componentCount][componentCount];
    this.Zc_ij = new double[componentCount][componentCount];
    this.Vc_ij = new double[componentCount][componentCount];
    Species[] components = new Species[componentCount];
    
    for (int i = 0; i < componentCount; i++) {
      components[i] = Menu.getSpecies(this.getSpeciesIndex(i));
    }
    
    /* Calculate interaction accentric factors (unitless) */
    for (int i = 0; i < componentCount; i++) {
      for (int j = 0; j < componentCount; j++) {
        this.omega_ij[i][j] = (components[i].getAccentricFactor() 
                                 + components[j].getAccentricFactor()) / 2.;
      }
    }
    
    /* Calculate interactions Z_c_ij (unitless) */
    for (int i = 0; i < componentCount; i++) {
      for (int j = 0; j < componentCount; j++) {
        this.Zc_ij[i][j] = (components[i].getZc() + components[j].getZc()) / 2.;
      }
    }
    
    /* Calculate interactions T_c_ij, in K */
    for (int i = 0; i < componentCount; i++) {
      for (int j = 0; j < componentCount; j++) {
        this.Tc_ij[i][j] = Math.pow((components[i].getTc() * components[j].getTc()), 0.5);
      }
    }
    
    /* Calculate interactions V_c_ij, in cm^3/mol */
    for (int i = 0; i < componentCount; i++) {
      for (int j = 0; j < componentCount; j++) {
        this.Vc_ij[i][j] = Math.pow((Math.pow(components[i].getVc(), 1./3.) 
                                       + Math.pow(components[j].getVc(), 1./3.)) / 2., 3);
      }
    }
    
    /* Calculate interactions P_c_ij, in bar */
    for (int i = 0; i < componentCount; i++) {
      for (int j = 0; j < componentCount; j++) {
        this.Pc_ij[i][j] = (this.Zc_ij[i][j] * 10.*Menu.GAS_CONSTANT * this.Tc_ij[i][j]) 
          / (this.Vc_ij[i][j]);
      }
    }
  }
  /*********************************************************************************************************************/
  
  
  public String getName() {
    return this.name;
  }
  
  
  public void setName(String name) {
    this.name = name;
  }
  
  
  public double getT() {
    return this.T;
  }
  
  
  public void setT(double T, boolean updateCondensableState, boolean updatePhaseFractions) {
    this.T = T;
    if (updateCondensableState) {
      this.updateCondensableState(updatePhaseFractions);
    }
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
  
  
  public double[] getX() {
    return this.x.clone();
  }
  
  
  public void setX(double[] x) throws StreamException {
    this.x = x.clone();
    
    if (x.length != this.speciesIndices.length) {
      throw new StreamCompositionException(this.clone(), "x");
    }
    
    double moleFractionSum = 0.;
    for (int i = 0; i < this.speciesIndices.length; i++) {
      moleFractionSum += x[i];
    }
    if (Math.abs(moleFractionSum - 1.0) > 0.001) {
      if (this.F > 0.001 || this.F < -0.001) {
        throw new ComponentFractionSumException(this.clone(), this.x, "liquid-phase mole");
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
    return this.y.clone();
  }
  
  
  public void setY(double[] y) throws StreamException {
    this.y = y.clone();
    
    if (y.length != this.speciesIndices.length) {
      throw new StreamCompositionException(this.clone(), "y");
    }
    
    double moleFractionSum = 0.;
    for (int i = 0; i < this.speciesIndices.length; i++) {
      moleFractionSum += y[i];
    }
    if (Math.abs(moleFractionSum - 1.0) > 0.001) {
      if (this.F > 0.001 || this.F < -0.001) {
        throw new ComponentFractionSumException(this.clone(), this.y, "vapour-phase mole");
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
  
  
  public void setZ(double[] z) throws StreamException {
    this.z = z.clone();
    
    if (z.length != this.speciesIndices.length) {
      throw new StreamCompositionException(this.clone(), "z");
    }
    
    double moleFractionSum = 0.;
    for (int i = 0; i < this.speciesIndices.length; i++) {
      moleFractionSum += z[i];
    }
    if (Math.abs(moleFractionSum - 1.0) > 0.001) {
      if (this.F > 0.001 || this.F < -0.001) {
        throw new ComponentFractionSumException(this.clone(), this.z, "overall mole");
      }
    }
  }
  
  
  public double getZi(int componentIndex) {
    return this.z[componentIndex];
  }
  
  
  public void setZi(double z_i, int componentIndex) {
    this.z[componentIndex] = z_i;
  }
  
  
  public double[] getK() {
    return this.K.clone();
  }
  
  
  public void setK(double[] K) {
    this.K = K.clone();
  }
  
  
  public double[] getGamma() {
    return this.gamma.clone();
  }
  
  
  public void setGamma(double[] gamma) {
    this.gamma = gamma.clone();
  }
  
  
  public double[] getPhi() {
    return this.phi.clone();
  }
  
  
  public void setPhi(double[] phi) {
    this.phi = phi.clone();
  }
  
  
  public double[][] getOmega_ij() {
    double[][] arrayCopy = new double[this.getComponentCount()][this.getComponentCount()];
    for (int i = 0; i < this.getComponentCount(); i++) {
      arrayCopy[i] = this.omega_ij[i].clone();
    }
    return arrayCopy;
  }
  
  
  public void setOmega_ij(double[][] omega_ij) {
    this.omega_ij = new double[this.getComponentCount()][this.getComponentCount()];
    for (int i = 0; i < omega_ij.length; i++) {
      this.omega_ij[i] = omega_ij[i].clone();
    }
  }
  
  
  public double[][] getTc_ij() {
    double[][] arrayCopy = new double[this.getComponentCount()][this.getComponentCount()];
    for (int i = 0; i < this.getComponentCount(); i++) {
      arrayCopy[i] = this.Tc_ij[i].clone();
    }
    return arrayCopy;
  }
  
  
  public void setTc_ij(double[][] Tc_ij) {
    this.Tc_ij = new double[this.getComponentCount()][this.getComponentCount()];
    for (int i = 0; i < Tc_ij.length; i++) {
      this.Tc_ij[i] = Tc_ij[i].clone();
    }
  }
  
  
  public double[][] getPc_ij() {
    double[][] arrayCopy = new double[this.getComponentCount()][this.getComponentCount()];
    for (int i = 0; i < this.getComponentCount(); i++) {
      arrayCopy[i] = this.Pc_ij[i].clone();
    }
    return arrayCopy;
  }
  
  
  public void setPc_ij(double[][] Pc_ij) {
    this.Pc_ij = new double[this.getComponentCount()][this.getComponentCount()];
    for (int i = 0; i < Pc_ij.length; i++) {
      this.Pc_ij[i] = Pc_ij[i].clone();
    }
  }
  
  
  public double[][] getZc_ij() {
    double[][] arrayCopy = new double[this.getComponentCount()][this.getComponentCount()];
    for (int i = 0; i < this.getComponentCount(); i++) {
      arrayCopy[i] = this.Zc_ij[i].clone();
    }
    return arrayCopy;
  }
  
  
  public void setZc_ij(double[][] Zc_ij) {
    this.Zc_ij = new double[this.getComponentCount()][this.getComponentCount()];
    for (int i = 0; i < Zc_ij.length; i++) {
      this.Zc_ij[i] = Zc_ij[i].clone();
    }
  }
  
  
  public double[][] getVc_ij() {
    double[][] arrayCopy = new double[this.getComponentCount()][this.getComponentCount()];
    for (int i = 0; i < this.getComponentCount(); i++) {
      arrayCopy[i] = this.Vc_ij[i].clone();
    }
    return arrayCopy;
  }
  
  
  public void setVc_ij(double[][] Vc_ij) {
    this.Vc_ij = new double[this.getComponentCount()][this.getComponentCount()];
    for (int i = 0; i < Vc_ij.length; i++) {
      this.Vc_ij[i] = Vc_ij[i].clone();
    }
  }
  
  
  public boolean[] getIsCondensable() {
    return this.isCondensable.clone();
  }
  
  
  public void setIsCondensable(boolean[] isCondensable) throws StreamCompositionException {
    this.isCondensable = isCondensable.clone();
    if (this.isCondensable.length != this.getComponentCount()) {
      throw new StreamCompositionException(this.clone(), "isCondensable");
    }
  }
  
  
  public boolean isComponentCondensable(int componentIndex) {
    return this.isCondensable[componentIndex];
  }
  
  
  public void setComponentCondensableState(boolean isCondensable, int componentIndex) {
    this.isCondensable[componentIndex] = isCondensable;
  }
  
}
