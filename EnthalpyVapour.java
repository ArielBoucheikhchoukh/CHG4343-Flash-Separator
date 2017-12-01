/* Evaluates the enthalpy of a species in the vapour phase.  
 * T = [K]
 * H = [J/mol*K]
 */

public class EnthalpyVapour extends Correlation {
  
  public static final int CONSTANT_COUNT = 4;
  public static final double R = 8.314;
  
  /**********************************************************************************************************************
    * 1.1) Constructor A
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public EnthalpyVapour(String id) {
    super(id);
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 1.2) Constructor B
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public EnthalpyVapour(String id, double[] C, double minX, double maxX, int form) {
    super(id, C, minX, maxX, form);
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 2) Copy Constructor 
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public EnthalpyVapour(EnthalpyVapour source) {
    super(source);
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 3) clone()
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public EnthalpyVapour clone() {
    return new EnthalpyVapour(this);
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 4) evaluateWithinBounds: Calculates the vapour-phase molar enthalpy of the species at temperature T.
    *                          Constants:
    *                             constants[0] = Normal Boiling Point Temperature
    *                             constants[1] = Liquid-Phase Enthalpy
    *                             constants[2] = Latent Heat of Vaporization
    * ---------------------------------------------------------------------------------------------------------------------
    */
  protected double evaluateWithinBounds(double x, double[] constants) {
    
    double T = x;
    double Tb = constants[0];
    double hL = constants[1];
    double lambda = constants[2];
    double[] C = super.getC();
    
    double Hv = EnthalpyVapour.R 
      * ((C[0] * (T - Tb)) 
           + (0.5 * C[1] * (Math.pow(T, 2) - Math.pow(Tb, 2)))
           + ((1. / 3.) * C[2] * (Math.pow(T, 3) - Math.pow(Tb, 3)))
           + (-1. * C[3] * (Math.pow(T, -1) - Math.pow(Tb, -1))));
    
    return hL + lambda + Hv;
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 5) evaluateDerivativeWithinBounds
    * ---------------------------------------------------------------------------------------------------------------------
    */
  protected double evaluateDerivativeWithinBounds(double x, double[] constants) {
    double T = x;
    double dhLdT = constants[1];
    double[] C = super.getC();
    
    double dHvdT = EnthalpyVapour.R 
      * (C[0] 
           + C[1] * T 
           + C[2] * Math.pow(T, 2) 
           + C[3] * Math.pow(T, -2));
    
    return dhLdT + dHvdT;
  }
  /*********************************************************************************************************************/
  
  
  /**********************************************************************************************************************
    * 6) getConstantCount() : Returns number of constants.
    * ---------------------------------------------------------------------------------------------------------------------
    */
  public int getConstantCount() {
    return EnthalpyVapour.CONSTANT_COUNT;
  }
  /*********************************************************************************************************************/
  
  
}