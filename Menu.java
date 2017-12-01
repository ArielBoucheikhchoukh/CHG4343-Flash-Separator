import java.util.ArrayList;
import java.text.DecimalFormat;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Menu {
 
 /*
  * Note : 
  *   If running in Eclipse, the file paths should begin with 'src/'.
  *   If running in Dr Java, the file paths should omit 'src/'.
  */
 public static final double GAS_CONSTANT = 8.314;
 
 private static final String SPECIES_FILE_PATH = "IO/Species.csv"; // IO/Spcies.csv
 private static final String SUB_GROUPS_FILE_PATH = "IO/SubGroups.csv"; // IO/SubGroups.csv
 private static final String INTERACTION_PARAMETERS_FILE_PATH 
       = "IO/InteractionParameters.csv"; // IO/InteractionParameters.csv
 private static final String INPUT_FILE_PATH = "IO/Input.csv"; // IO/Input.csv
 private static final String OUTPUT_FILE_PATH = "IO/Output.txt"; // IO/Output.txt
 
 private static Species[] species;
 private static int[] subGroupIndices;
 private static double[] subGroupRelativeVolume; //R
 private static double[] subGroupRelativeSurfaceArea; //Q
 private static double[][] interactionParameters;
 
 private static ArrayList<String> messages;

 
/**********************************************************************************************************************
* 1) Main Method : Entry point of the program.
* ---------------------------------------------------------------------------------------------------------------------
*/
 public static void main(String[] args) {
  Menu.messages = new ArrayList<String>();
  Menu.launchMainMenu(); // Go to method 2
 }
/*********************************************************************************************************************/

 
/**********************************************************************************************************************
* 2) launchMenu() : Starts the menu.
* ---------------------------------------------------------------------------------------------------------------------
*/
 public static void launchMainMenu() {
  
 /*
 * I. Initialize Local Variables
 * -----------------------------------------------------------------------------
 */
  int menuIndex = 0; // Denotes the menu option the user has chosen
  boolean correctInput = false; // true if the user input is legal
  boolean exit = false; // true if the user chooses to terminate the program
  
  /* operatingConditionsList : 
   *   [0] = flash case
   *   [1] = behaviour case
   *   [2] = input temperature [K]
   *   [3] = input pressure [bar]
   *   [4] = input flow rate [mol/h]
   * */
  ArrayList<Double> operatingConditionsList = new ArrayList<Double>(); 
   
  
  ArrayList<String> componentNamesList = new ArrayList<String>(); // Names of Components in Feed
  ArrayList<Double> moleFractionsList = new ArrayList<Double>(); // Feed Mole Fractions
  
  Scanner keyboardReader = new Scanner(System.in); // Reads keyboard input
  
  try {
   // Clear Output File
   Menu.outputToFile("", Menu.OUTPUT_FILE_PATH, false);
   
  /*
  * II. Read Input Files 
  * ----------------------------------------------------------------------------
  */
  // Read Input.csv File
   Menu.readFlashSeparatorInputFile(operatingConditionsList, componentNamesList, moleFractionsList);
   
   // Read Species.csv, SubGroups.csv and InteractionParameters.csv Files
   Menu.readSpeciesData(componentNamesList);
    
  /*
  * III. Main Menu
  * -----------------------------------------------------------------------------
  */
   while (!exit) {
    do {
     
     // Build menu prompt for user
     String menuPrompt = "";

     menuPrompt = "Flash Separation Input: \n";

     String operatingTemp = new String();
     switch ((int) operatingConditionsList.get(0).doubleValue()) {
     case 0:
      menuPrompt += "   Calculate Isothermal Heat \n";
      operatingTemp = "   Feed Temperature: " + operatingConditionsList.get(2).doubleValue()
        + " K \n";
      break;
     case 1:
      menuPrompt += "   Calculate Flash Temperature \n";
      operatingTemp = "   Feed Temperature: " + operatingConditionsList.get(2).doubleValue()
        + " K \n";
      break;
     case 2:
      menuPrompt += "   Calculate Feed Temperature \n";
      operatingTemp = "   Flash Temperature: " + operatingConditionsList.get(2).doubleValue()
        + " K \n";
      break;
     default:
      break;
     }

     switch ((int) operatingConditionsList.get(1).doubleValue()) {
     case 0:
      menuPrompt += "   Ideal Behaviour \n";
      break;
     case 1:
      menuPrompt += "   Non-Ideal Behaviour \n";
      break;
     default:
      break;
     }

     menuPrompt += operatingTemp;
     menuPrompt += "   Tank Pressure: " + operatingConditionsList.get(3).doubleValue() + " bar \n";
     menuPrompt += "   Feed Flow Rate: " + operatingConditionsList.get(4).doubleValue() + " mol/h \n\n"
       + "Feed Stream Components: \n";

     for (int i = 0; i < componentNamesList.size(); i++) {
      menuPrompt += componentNamesList.get(i) + ": " + moleFractionsList.get(i).doubleValue() * 100.
        + " % \n";
     }

     menuPrompt += "\n\nEnter 0 to terminate the program." + " \nEnter 1 to proceed to the simulation."
       + " \nEnter 2 for instructions.";
     
     // Print Menu Prompt
     System.out.println("\r\n" + menuPrompt + "\r\n");

     correctInput = false;
     try {
      menuIndex = keyboardReader.nextInt(); // Store user-selected option
      correctInput = true;
      
      // Verify whether user input is legal
      if (menuIndex < 0 || menuIndex > 2) {
       System.out.println("Error: Incorrect input. Enter 0, 1 or 2.");
       correctInput = false;
      }
     } catch (Exception e) {
      System.out.println("Error: Incorrect input. Enter 0, 1 or 2.");
     }

    } while (!correctInput);

    switch (menuIndex) {
    case 0: // Terminate the program
     exit = true;
     break;
    case 1: // Run the Simulation
     Menu.messages.clear();
     Menu.runSimulation(operatingConditionsList, componentNamesList, moleFractionsList);

     // Print to Output File
     String messagesString = new String();
     for (int i = 0; i < Menu.messages.size(); i++) {
      messagesString += Menu.messages.get(i);
     }
     Menu.outputToFile(messagesString + "\r\n\r\n", Menu.OUTPUT_FILE_PATH, true);
     System.out.println("\nPress any key to continue.");
     keyboardReader.next();
     break;
    case 2: // Prompt User to Read Instructions File
     System.out.println("\r\nRefer to the Instructions.txt file. \r\n");
     break;
    default:
     exit = true;
     break;
    }
   }
  }
  catch (Exception e) {
   System.out.println("\r\nError: Incorrect input. \r\n" + e.getMessage() + "\r\n\r\n");
   Menu.outputToFile("\r\nError: Incorrect input. \r\n" + e.getMessage() + "\r\n\r\n", 
     Menu.OUTPUT_FILE_PATH, true);
  }
  
  keyboardReader.close();
  System.out.println("Program terminated.");
 }
/*********************************************************************************************************************/
 

/**********************************************************************************************************************
* 3) readFlashSeparatorInputFile() : Reads the user input from the input file.
* ---------------------------------------------------------------------------------------------------------------------
*/
 public static void readFlashSeparatorInputFile(ArrayList<Double> operatingConditions, 
   ArrayList<String> componentNames, ArrayList<Double> moleFractions) 
     throws IOException {

  Scanner fileReader;
  Scanner lineReader;
  
  try {
   fileReader = new Scanner(new FileInputStream(Menu.INPUT_FILE_PATH));
   fileReader.useDelimiter(",");
  }
  catch (Exception e) {
   throw new FileNotFoundException(Menu.INPUT_FILE_PATH + " was not found.");
  }

  /*
  * Read Cases and Operating Conditions of the Flash Separator
  * -----------------------------------------------------------------------------
  */
  fileReader.nextLine(); // Skip Row 1
  
  try {
   operatingConditions.add(new Double((double) fileReader.nextInt())); // Move to A2
   operatingConditions.add(new Double((double) fileReader.nextInt())); // Move to B2
   operatingConditions.add(new Double(fileReader.nextDouble() + 273.15)); // Move to Cell C2
   operatingConditions.add(new Double(fileReader.nextDouble())); // Move to Cell D2
   operatingConditions.add(new Double(fileReader.nextDouble())); // Move to Cell E2
  }
  catch (Exception e) {
   fileReader.close();
   throw new IOException("Inputted operating conditions in " + Menu.INPUT_FILE_PATH 
     + " are incorrect.");
  }
  
  if (operatingConditions.get(0).doubleValue() < 0 || operatingConditions.get(0).doubleValue() > 2) {
   fileReader.close();
   throw new IllegalArgumentException("IllegalArgumentException: Inputted flash case is incorrect.");
  }
  else if (operatingConditions.get(1).doubleValue() < 0 || operatingConditions.get(1).doubleValue() > 1) {
   fileReader.close();
   throw new IllegalArgumentException("IllegalArgumentException: Inputted behaviour case is incorrect.");
  }
  else if (operatingConditions.get(2).doubleValue() <= 0 ) {
   fileReader.close();
   throw new IllegalArgumentException("IllegalArgumentException: Inputted temperature is below absolute zero.");
  }
  else if (operatingConditions.get(2).doubleValue() > Double.MAX_VALUE) {
   fileReader.close();
   throw new IllegalArgumentException("IllegalArgumentException: Inputted temperature is above maximum.");
  }
  else if (operatingConditions.get(3).doubleValue() <= 0 ) {
   fileReader.close();
   throw new IllegalArgumentException("IllegalArgumentException: Inputted pressure is less than or equal to 0.");
  }
  else if (operatingConditions.get(3).doubleValue() > Double.MAX_VALUE) {
   fileReader.close();
   throw new IllegalArgumentException("IllegalArgumentException: Inputted pressure is above maximum.");
  }
  else if (operatingConditions.get(4).doubleValue() <= 0 ) {
   fileReader.close();
   throw new IllegalArgumentException("IllegalArgumentException: Inputted molar flow rate is less than or equal to 0.");
  }
  else if (operatingConditions.get(4).doubleValue() > Double.MAX_VALUE) {
   fileReader.close();
   throw new IllegalArgumentException("IllegalArgumentException: Inputted molar flow rate is above maximum.");
  }
  
  /*
  * Read Feed Stream Composition
  * -----------------------------------------------------------------------------
  */
  fileReader.nextLine(); // Skip current row

  // Store the names of the components in the feed and their respective mole fractions
  while (fileReader.hasNext()) {
   
   lineReader = new Scanner(fileReader.nextLine()); // Store current row
   lineReader.useDelimiter(",");
   
   String name;
   try {
    name = lineReader.next();
   }
   catch (Exception e) {
    lineReader.close();
    fileReader.close();
    throw new IOException("Illegal carriage return characters in " + Menu.INPUT_FILE_PATH 
      + ". \r\nDelete all blank lines after the final component.");
   }

   if (name != null) {
    if (name.equals("")) {
     lineReader.close();
     fileReader.close();
     throw new IOException("A species name is missing in " + Menu.SPECIES_FILE_PATH + ".");
    } else {
     System.out.println(name);
     
     componentNames.add(name);
     
     double z = lineReader.nextDouble();
     moleFractions.add(new Double(z));
     
     if (z <= 0.) {
      lineReader.close();
      fileReader.close();
      throw new IllegalArgumentException("IllegalArgumentException: Inputted mole fraction is less than or equal to 0.");
     }
    }
   }

   lineReader.close();
  }
  fileReader.close();
  
  if (!Menu.areArrayListElementsUnique(componentNames)) {
   throw new IOException("Component names in " + Menu.INPUT_FILE_PATH + " are not unique.");
  }
  
  double sum = 0.;
  for (int i = 0; i < moleFractions.size(); i++) {
   sum += moleFractions.get(i).doubleValue();
  }
  if (sum > 1.001 || sum < 0.999) {
   throw new IOException("Mole fractions in " + Menu.INPUT_FILE_PATH + " do not sum to unity.");
  }
 }
/*********************************************************************************************************************/

 
/**********************************************************************************************************************
* 4) readSpeciesData() : Reads the constants file.
* ---------------------------------------------------------------------------------------------------------------------
*/
 public static void readSpeciesData(ArrayList<String> speciesNamesList) 
   throws IOException {

 /*
  * I. Local Variable Declarations
 * -----------------------------------------------------------------------------
 */
  Scanner fileReader; // Parses comma-delimited text files
  Scanner lineReader; // Parses an individual line from a file
  
  int speciesCount = speciesNamesList.size(); // Number of required species
  int subGroupCount = 0; // Number of required sub-group types
  int speciesSubGroupCount; // Number of a given sub-group type associated with a given species
  int masterTotalSubGroupCount; // Number of defined sub-group types in Species.csv
  int totalSubGroupCount; // Number of defined sub-group types in all other files
  int storedSpeciesIterator = 0; // Denotes the instantaneous number of required species objects that have been built
  int speciesIndex; // Unique identifier of a species
  int subGroupIndex; // Unique identifier of a sub-group
  double interactionParameter; // Describes the interaction between two sub-groups i and j
  
  String name; // Stores species and sub-group names
  
  ArrayList<String> allSpeciesNamesList; // Stores the names of all defined species in Species.csv
  ArrayList<Integer> allSpeciesIndicesList; // Stores the indices of all defined species in Species.csv
  ArrayList<String> speciesToStoreNamesList; // Stores the names of all required species from Input.csv
  ArrayList<String> allSubGroupNamesList; // Stores the names of all defined sub-groups in a given file
  ArrayList<String> allSubGroupNamesList2; // Stores the names of all defined sub-groups (row headers) in InteractionParameters.csv
  ArrayList<Integer> allSubGroupIndicesList; // Stores the indices of all defined sub-groups in SubGroups.csv
  ArrayList<Integer> subGroupPositionsList; // Stores the column positions of all defined sub-groups in Species.csv
  ArrayList<String> subGroupNamesList; // Stores the names of all required sub-groups 
  ArrayList<Integer> speciesSubGroupPositionsList; // Stores the column positions (Species.csv) of all sub-group types in a given species
  ArrayList<Integer> speciesSubGroupCountsList; // Stores the numbers of all sub-group types in a given species
  
  double[] properties; // Stores the properties of a given species
  double[] correlationParameters; // Stores the correlation parameters of a given species
  int[][] speciesSubGroups; // Stores the sub-group column positions and numbers of a given species
  
  boolean isUnique;
  
 /*
  * II. Store Species Data
  * -----------------------------------------------------------------------------
  */
  // i) Open Species.csv
  try {
   fileReader = new Scanner(new FileInputStream(Menu.SPECIES_FILE_PATH));
   fileReader.useDelimiter(",");
  }
  catch (Exception e) {
   throw new FileNotFoundException(Menu.SPECIES_FILE_PATH + " was not found.");
  }
  
  // ii) Initialize Arrays
  Menu.species = new Species[speciesCount]; // Initialize the species array
  allSpeciesNamesList = new ArrayList<String>();
  allSpeciesIndicesList = new ArrayList<Integer>();
  speciesToStoreNamesList = new ArrayList<String>();
  allSubGroupNamesList = new ArrayList<String>();
  subGroupPositionsList = new ArrayList<Integer>();
  subGroupNamesList = new ArrayList<String>();
  
  // iii) Flag Unstored Species 
  for (int i = 0; i < speciesCount; i++) {
   speciesToStoreNamesList.add(speciesNamesList.get(i));
  }
  
  // iv) Store Sub-Group Names
  fileReader.nextLine();
  lineReader = new Scanner(fileReader.nextLine()); // Store current row
  lineReader.useDelimiter(",");
  
  masterTotalSubGroupCount = 0;
  for (int i = 0; i < Species.PHYSICAL_PROPERTY_COUNT 
    + Species.getCorrelationParameterCount() + 2; i++) {
   lineReader.next();
  }
  while (lineReader.hasNext()) {
   name = lineReader.next();
   
   if (!name.equals("")) {
    allSubGroupNamesList.add(name);
    masterTotalSubGroupCount++;
   }
  }
  lineReader.close();
  
  // v) Store Species Data and Create Species Objects
  while (fileReader.hasNextLine()) {
   lineReader = new Scanner(fileReader.nextLine()); // Store current row
   lineReader.useDelimiter(",");
   
   name = lineReader.next(); // Store the name of the species in Column A
   allSpeciesNamesList.add(name);
   
   speciesIndex = (int) lineReader.nextDouble(); // Store the index of the species in Column B
   allSpeciesIndicesList.add(new Integer(speciesIndex));
   
   System.out.println(name);

   // Check whether the file reader has reached the end of the file
   if (name.equals("") || name == null) {

    // End of the Species.csv File
    break;
   } else {

    // Species name is not blank - continue reading
    for (int i = 0; i < speciesToStoreNamesList.size(); i++) {

     System.out.println("Compare to: " + speciesToStoreNamesList.get(i));

     // Check whether the current species requires storage 
     if (name.equals(speciesToStoreNamesList.get(i))) {
      
      //Remove current species name from species-to-store array
      speciesToStoreNamesList.remove(i); 

      //Store the species properties
      properties = new double[Species.PHYSICAL_PROPERTY_COUNT];
      for (int j = 0; j < Species.PHYSICAL_PROPERTY_COUNT; j++) {
       properties[j] = lineReader.nextDouble(); // Next column
      }
      
      //Store the species correlation parameters
      correlationParameters 
       = new double[Species.getCorrelationParameterCount()];
      for (int j = 0; j < Species.getCorrelationParameterCount(); j++) {
       correlationParameters[j] = lineReader.nextDouble(); // Next column
      }
      
      //Store the species sub-groups 
      speciesSubGroupPositionsList = new ArrayList<Integer>();
      speciesSubGroupCountsList = new ArrayList<Integer>();
      for (int j = 0; j < masterTotalSubGroupCount; j++) {
       String token = lineReader.next();
       
       //Check if the current sub-group count is non-zero
       if (token != null && !token.equals("")) {
        speciesSubGroupCount = Integer.parseInt(token); 
        if (speciesSubGroupCount != 0) {
         
         //If this species possesses this sub-group, 
         //add it to the species' own array
         speciesSubGroupPositionsList.add(new Integer(j));
         speciesSubGroupCountsList.add(new Integer(speciesSubGroupCount));
         
         //Check if the current sub-group has already been flagged as in use
         isUnique = true;
         for (int k = 0; k < subGroupPositionsList.size(); k++) {
          if (j == subGroupPositionsList.get(k).intValue()) {
           isUnique = false;
           break;
          }
         }
         
         //Check if the current sub-group has yet to be flagged, add it to the
         // sub-group position and name arrays.
         if (isUnique) {
          subGroupPositionsList.add(new Integer(j));
          subGroupNamesList.add(allSubGroupNamesList.get(j));
          subGroupCount++;
         }
        }
       }
      }
      
      //Convert the species sub-group array lists into arrays
      speciesSubGroups = new int[2][speciesSubGroupPositionsList.size()];
      for (int j = 0; j < speciesSubGroupPositionsList.size(); j++) {
       speciesSubGroups[0][j] = speciesSubGroupPositionsList.get(j).intValue();
       speciesSubGroups[1][j] = speciesSubGroupCountsList.get(j).intValue();
      }
      
      //Create a new species object
      Menu.species[storedSpeciesIterator] = new Species(name, speciesIndex, 
        properties, correlationParameters, speciesSubGroups);
      storedSpeciesIterator++;

      break;
     }
    }
   }

   lineReader.close();
  }
  fileReader.close();
  
  // vi) Verify Data Integrity
  if (speciesToStoreNamesList.size() != 0) {
   throw new IOException("The following species were not found in " + Menu.SPECIES_FILE_PATH 
     + " : " + speciesToStoreNamesList.toString());
  }
  if (!Menu.areArrayListElementsUnique(allSpeciesNamesList)) {
   throw new IOException("Species names in " + Menu.SPECIES_FILE_PATH + " are not unique.");
  }
  if (!Menu.areArrayListElementsUnique(allSpeciesIndicesList)) {
   throw new IOException("Species indices in " + Menu.SPECIES_FILE_PATH + " are not unique.");
  }
  if (!Menu.areArrayListElementsUnique(allSubGroupNamesList)) {
   throw new IOException("Sub-group names in " + Menu.SPECIES_FILE_PATH + " are not unique.");
  }
  
 /*
 * III. Store Sub-Group Data
 * -----------------------------------------------------------------------------
 */
  // i) Open SubGroups.csv
  try {
   fileReader = new Scanner(new FileInputStream(Menu.SUB_GROUPS_FILE_PATH));
   fileReader.useDelimiter(",");
   fileReader.nextLine();
  }
  catch (Exception e) {
   throw new FileNotFoundException(Menu.SUB_GROUPS_FILE_PATH + " was not found.");
  }
  
  // ii) Initialize Arrays
  Menu.subGroupIndices = new int[subGroupCount];
  Menu.subGroupRelativeVolume = new double[subGroupCount];
  Menu.subGroupRelativeSurfaceArea = new double[subGroupCount];
  
  allSubGroupNamesList.clear();
  allSubGroupIndicesList = new ArrayList<Integer>();
  
  // iii) Store Sub-Groups
  while (fileReader.hasNext()) {
   lineReader = new Scanner(fileReader.nextLine()); // Store current row
   lineReader.useDelimiter(",");
   
   name = lineReader.next();
   allSubGroupNamesList.add(name);
   
   subGroupIndex = lineReader.nextInt();
   allSubGroupIndicesList.add(new Integer(subGroupIndex));
   
   for (int i = 0; i < subGroupCount; i++) {
    if (name.equals(subGroupNamesList.get(i))) {
     Menu.subGroupIndices[i] = subGroupIndex;
     Menu.subGroupRelativeVolume[i] = lineReader.nextDouble();
     Menu.subGroupRelativeSurfaceArea[i] = lineReader.nextDouble();
     break;
    }
   }
     
   lineReader.close();
  }
  fileReader.close();
  
  // iv) Convert Sub-Group Positions stored in Species Objects to Sub-Group Indices
  for (int i = 0; i < speciesCount; i++) {
   speciesSubGroups = Menu.species[i].getSubGroups();
   for (int j = 0; j < speciesSubGroups[0].length; j++) {
    for (int k = 0; k < subGroupCount; k++) {
     if (speciesSubGroups[0][j] == subGroupPositionsList.get(k).intValue()) {
      speciesSubGroups[0][j] = Menu.subGroupIndices[k];
      break;
     }
    }
   }
   Menu.species[i].setSubGroups(speciesSubGroups);
  }
  
  // v) Verify Data Integrity
  if (!Menu.areArrayListElementsUnique(allSubGroupNamesList)) {
   throw new IOException("Sub-group names in " + Menu.SUB_GROUPS_FILE_PATH + " are not unique.");
  }
  if (!Menu.areArrayListElementsUnique(allSubGroupIndicesList)) {
   throw new IOException("Sub-group indices in " + Menu.SUB_GROUPS_FILE_PATH + " are not unique.");
  }
  if (allSubGroupNamesList.size() != masterTotalSubGroupCount) {
   throw new IOException("Number of sub-groups in " + Menu.SUB_GROUPS_FILE_PATH 
     + " is inconsistant with that in " + Menu.SPECIES_FILE_PATH 
     + ". \r\nThere are " + masterTotalSubGroupCount + " sub-groups defined in " 
     + Menu.SPECIES_FILE_PATH + " and " + allSubGroupNamesList.size() + " in " 
     + Menu.SUB_GROUPS_FILE_PATH);
  }
  
 /*
 * IV. Store Interaction Parameters
 * -----------------------------------------------------------------------------
 */
  // i) Open InteractionParameters.csv
  try {
   fileReader = new Scanner(new FileInputStream(Menu.INTERACTION_PARAMETERS_FILE_PATH));
   fileReader.useDelimiter(",");
   fileReader.nextLine(); // Skip a row
  }
  catch (Exception e) {
   throw new FileNotFoundException(Menu.INTERACTION_PARAMETERS_FILE_PATH + " was not found.");
  }
   
  lineReader = new Scanner(fileReader.nextLine()); // Store current row
  lineReader.useDelimiter(",");
  lineReader.next();
  
  // ii) Initialize Arrays
  Menu.interactionParameters = new double[subGroupCount][subGroupCount];
  allSubGroupNamesList.clear();
  allSubGroupNamesList2 = new ArrayList<String>();
  
  // iii) Store Sub-Group Names (Row Headers)
  totalSubGroupCount = 0;
  while (lineReader.hasNext()) {
   allSubGroupNamesList.add(lineReader.next());
   totalSubGroupCount++;
  }
  lineReader.close();
  
  // iV) Store Interaction Parameters
  while (fileReader.hasNext()) {
   lineReader = new Scanner(fileReader.nextLine()); // Store current row
   lineReader.useDelimiter(",");
   
   name = lineReader.next();
   allSubGroupNamesList2.add(name);
   
   for (int i = 0; i < subGroupCount; i++) {
    if (name.equals(subGroupNamesList.get(i))) {
     for (int j = 0; j < totalSubGroupCount; j++) {
      interactionParameter = lineReader.nextDouble();
      for (int k = 0; k < subGroupCount; k++) {
       if (allSubGroupNamesList.get(j).equals(subGroupNamesList.get(k))) {
        Menu.interactionParameters[i][k] = interactionParameter;
       }
      }
     }
     break;
    }
   }
     
   lineReader.close();
  }
  fileReader.close();
  
  // v) Verify Data Integrity
  if (!Menu.areArrayListElementsUnique(allSubGroupNamesList)) {
   throw new IOException("Sub-group names in " + Menu.INTERACTION_PARAMETERS_FILE_PATH 
     + " are not unique.");
  }
  if (allSubGroupNamesList.size() != masterTotalSubGroupCount) {
   throw new IOException("Number of sub-groups in " + Menu.INTERACTION_PARAMETERS_FILE_PATH 
     + " (row headers) is inconsistant with that in " + Menu.SPECIES_FILE_PATH 
     + ". \r\nThere are " + masterTotalSubGroupCount + " sub-groups defined in " 
     + Menu.SPECIES_FILE_PATH + " and " + allSubGroupNamesList.size() + " in " 
     + Menu.INTERACTION_PARAMETERS_FILE_PATH);
  }
  if (allSubGroupNamesList2.size() != masterTotalSubGroupCount) {
   throw new IOException("Number of sub-groups in " + Menu.INTERACTION_PARAMETERS_FILE_PATH 
     + " (column headers) is inconsistant with that in " + Menu.SPECIES_FILE_PATH 
     + ". \r\nThere are " + masterTotalSubGroupCount + " sub-groups defined in " 
     + Menu.SPECIES_FILE_PATH + " and " + allSubGroupNamesList2.size() + " in " 
     + Menu.INTERACTION_PARAMETERS_FILE_PATH);
  }
  if (allSubGroupNamesList.size() != allSubGroupNamesList2.size()) {
   throw new IOException("The interaction parameters matrix in " 
     + Menu.INTERACTION_PARAMETERS_FILE_PATH + " is not a square matrix.");
  }

 }
/*********************************************************************************************************************/


/**********************************************************************************************************************
* 5) runSimulation() : 
* ---------------------------------------------------------------------------------------------------------------------
*/
 public static void runSimulation(ArrayList<Double> operatingConditions, 
   ArrayList<String> componentNames, ArrayList<Double> moleFractions) {
  
  // Build the Flash Separator
  FlashSeparator flashSeparator;
  try {
   double[] z = new double[moleFractions.size()];
   for (int i = 0; i < moleFractions.size(); i++) {
    z[i] = moleFractions.get(i).doubleValue();
   }
   
   flashSeparator = Menu.buildFlashSeparator(
     (int) operatingConditions.get(0).doubleValue(), 
     (int) operatingConditions.get(1).doubleValue(), 
     operatingConditions.get(2).doubleValue(), 
     operatingConditions.get(3).doubleValue(), 
     operatingConditions.get(4).doubleValue(), 
     componentNames.toArray(new String[componentNames.size()]), 
     z); // Go to method (6)
   
   // Flash Calculation
   Menu.outputToFile("", Menu.OUTPUT_FILE_PATH, false);
   Stream[] flashStreams = new Stream[2];
   try {
    flashStreams = flashSeparator.flashCalculation();
    System.out.println("Simulation was successful. \n");
    Menu.outputToFile("\r\nSimulation was successful. \r\n", Menu.OUTPUT_FILE_PATH, true);
    Menu.outputToFile("\r\n\r\n" + flashSeparator.toString(), 
      Menu.OUTPUT_FILE_PATH, true);
   } catch (FlashCalculationException | NumericalMethodException | FunctionException 
     | IllegalArgumentException e) {
    
    System.out.println(e.getMessage());
    Menu.outputToFile("\r\nError: Simulation failed. "
      + e.getMessage() + " \r\n\r\n", Menu.OUTPUT_FILE_PATH, true);
   } catch (Exception e) {
    System.out.println("\r\nUnknown Error. Simulation failed. \r\n\r\n" + e.getMessage());
    Menu.appendToMessages(e.getMessage() + "\r\n\r\n");
    Menu.appendToMessages("\r\nUnknown Error. Simulation failed. \r\n\r\n");
   }
  } catch (StreamException e) {
   System.out.println(e.getMessage());
   Menu.appendToMessages("\r\nError: Could not build the FlashSeparator." 
     + " The composition of the feed stream is inconsistent. \r\n" 
     + e.getMessage() + " \r\n\r\n");
  } catch (Exception e) {
   System.out.println("\r\nError: Could not build the FlashSeparator. \r\n\r\n" + e.getMessage());
   Menu.appendToMessages(e.getMessage() + "\r\n\r\n");
   Menu.appendToMessages("\r\nError: Could not build the FlashSeparator. \r\n\r\n");
  }
  
 }

/*********************************************************************************************************************/

 
/**********************************************************************************************************************
* 6) buildFlashSeparator() : Reads the species data and the user input from the
*         input file and builds a FlashSeparator.
* ---------------------------------------------------------------------------------------------------------------------
*/
 public static FlashSeparator buildFlashSeparator(int flashCase, int behaviourCase,
   double T, double P, double F, String[] componentNames, double[] z) 
     throws FileNotFoundException, StreamException {

  int[] speciesIndices = new int[componentNames.length];

  for (int i = 0; i < componentNames.length; i++) {
   for (int j = 0; j < Menu.species.length; j++) {
    Species species = Menu.species[j];
    if (componentNames[i].equals(species.getName())) {
     speciesIndices[i] = species.getIndex();
    }
   }
  }
  
  // Create Behaviour Object
  Behaviour behaviour;
  switch (behaviourCase) {
   case 0: // Ideal Behaviour
    behaviour = new Behaviour();
    break;
    
   case 1: // Non-Ideal Behaviour
    behaviour = new NonIdealBehaviour();
    break;

   default: 
    behaviour = new Behaviour();
    break;
  }
  
  FlashSeparator flashSeparator;
  switch (flashCase) {
   case 0: // Isothermal Non-Adiabatic Operation; Find Q
    flashSeparator = new IsothermalHeat(T, P, F, z, speciesIndices, behaviour);
    break;
 
   case 1: // Adiabatic Operation; Find the Flash Temperature
    flashSeparator = new AdiabaticFlashTemp(T, P, F, z, speciesIndices, behaviour);
    break;
 
   case 2: // Adiabatic Operation; Find the Feed Temperature
    flashSeparator = new AdiabaticFeedTemp(T, P, F, z, speciesIndices, behaviour);
    break;
    
   default:
    flashSeparator = new IsothermalHeat(T, P, F, z, speciesIndices, behaviour);
    break;
  }

  return flashSeparator;
 }
/*********************************************************************************************************************/

 
/**********************************************************************************************************************
* 7) outputToFile() : Prints a String to a text file.
* ----------------------------------------------------------------------------------------------------------------------
*/
 public static void outputToFile(String output, String outputFilePath, boolean append) {
  PrintWriter fileWriter = null;

  try {
   fileWriter = new PrintWriter(new FileOutputStream(outputFilePath, append));
   fileWriter.println(output);
   fileWriter.close();
  } catch (FileNotFoundException e) {
   System.out.print(e.getMessage() + "\n" + output);
  }

 }
/*********************************************************************************************************************/

 
/**********************************************************************************************************************
* 8) getSpecies() : Returns a copy of the species at the given index.
* ----------------------------------------------------------------------------------------------------------------------
*/
 public static Species getSpecies(int speciesIndex) {

  for (int i = 0; i < Menu.species.length; i++) {
   if (speciesIndex == Menu.species[i].getIndex()) {
    return new Species(Menu.species[i]);
   }
  }
  return null;
 }
/*********************************************************************************************************************/


/**********************************************************************************************************************
* 9) getSubGroupIndex() : Returns the sub-group index at position i.
* ----------------------------------------------------------------------------------------------------------------------
*/
 public static int getSubGroupIndex(int i) {
  return Menu.subGroupIndices[i];
 }
/*********************************************************************************************************************/


/**********************************************************************************************************************
* 10) getSubGroupTypeCount() : Returns the number of stored sub-groups.
* ----------------------------------------------------------------------------------------------------------------------
*/
 public static int getSubGroupTypeCount() {
  return Menu.subGroupIndices.length;
 }
/*********************************************************************************************************************/
 

/**********************************************************************************************************************
* 11) getSubGroupR() : Returns the relative volume of the sub-group at the given index.
* ----------------------------------------------------------------------------------------------------------------------
*/
 public static double getSubGroupR(int subGroupIndex) {
  for (int i = 0; i < Menu.subGroupIndices.length; i++) {
   if (subGroupIndex == Menu.subGroupIndices[i]) {
    return Menu.subGroupRelativeVolume[i];
   }
  }
  return 0.;
 }
/*********************************************************************************************************************/


/**********************************************************************************************************************
* 12) getSubGroupQ() : Returns the relative surface area of the sub-group at the given index.
* ----------------------------------------------------------------------------------------------------------------------
*/
 public static double getSubGroupQ(int subGroupIndex) {
  for (int i = 0; i < Menu.subGroupIndices.length; i++) {
   if (subGroupIndex == Menu.subGroupIndices[i]) {
    return Menu.subGroupRelativeSurfaceArea[i];
   }
  }
  return 0.;
 }
/*********************************************************************************************************************/
 
 
/**********************************************************************************************************************
* 13) getInteractionParameter() : Returns the interaction parameter of the subgroups i and j.
* ----------------------------------------------------------------------------------------------------------------------
*/
 public static double getInteractionParameter(int i, int j) {
  return Menu.interactionParameters[i][j];
 }
/*********************************************************************************************************************/

 
/**********************************************************************************************************************
* 14.1) findRoot() :
* ----------------------------------------------------------------------------------------------------------------------
*/
 public static double findRoot(Function f, double[] constants, double startPoint, 
   boolean positiveDirection) throws NumericalMethodException, FunctionException {

  // RootFinder rootFinder = new NewtonRaphsonRootFinder(maxEvaluationCount);
  RootFinder rootFinder = new RiddersMethodRootFinder(
    BracketingRootFinder.DEFAULT_INCREMENT_FACTOR * RootFinder.DEFAULT_TOLERANCE, 
    BracketingRootFinder.DEFAULT_SUB_INCREMENT_FRACTION,
    positiveDirection, RootFinder.DEFAULT_MAX_EVALUATION_COUNT, true);

  return rootFinder.findRoot(f, constants, startPoint, RootFinder.DEFAULT_TOLERANCE);
 }
/*********************************************************************************************************************/

 
/**********************************************************************************************************************
* 14.2) findRoot() :
* ----------------------------------------------------------------------------------------------------------------------
*/
 public static double findRoot(Function f, double[] constants, double startPoint, double endPoint,
   double incrementLength, double subIncrementFraction, double tolerance, int maxEvaluationCount) 
     throws NumericalMethodException, FunctionException {

  // RootFinder rootFinder = new NewtonRaphsonRootFinder(maxEvaluationCount);
  RootFinder rootFinder = new RiddersMethodRootFinder(endPoint, incrementLength, subIncrementFraction, 
    maxEvaluationCount);

  return rootFinder.findRoot(f, constants, startPoint, tolerance);
 }
/*********************************************************************************************************************/

 
/**********************************************************************************************************************
* 14.3) findRoot() :
* ----------------------------------------------------------------------------------------------------------------------
*/
 public static double findRoot(Function f, double[] constants, double startPoint, 
   boolean positiveDirection, double incrementLength, double subIncrementFraction, 
   double tolerance, int maxEvaluationCount, boolean useFunctionBounds) 
     throws NumericalMethodException, FunctionException {

  // RootFinder rootFinder = new NewtonRaphsonRootFinder(maxEvaluationCount);
  RootFinder rootFinder = new RiddersMethodRootFinder(incrementLength, subIncrementFraction, 
    positiveDirection, maxEvaluationCount, useFunctionBounds);

  return rootFinder.findRoot(f, constants, startPoint, tolerance);
 }
/*********************************************************************************************************************/

 
/**********************************************************************************************************************
* 15) appendToMessages() :
* ----------------------------------------------------------------------------------------------------------------------
*/
 public static void appendToMessages(String message) {

  boolean unique = true;

  for (int i = 0; i < Menu.messages.size(); i++) {
   // System.out.println(message);
   // System.out.println(Menu.messages.get(i));
   if (message.equals(Menu.messages.get(i))) {
    unique = false;
   }
  }

  if (unique) {
   Menu.messages.add(message);
  }
 }
/*********************************************************************************************************************/

 
/**********************************************************************************************************************
* 16) areArrayListElementsUnique() : Checks if all of the elements inside the ArrayList are unique.
*           Returns true by default if the array is null;
* ----------------------------------------------------------------------------------------------------------------------
*/
 public static boolean areArrayListElementsUnique(ArrayList array) {

  boolean areUnique = true;
  
  if (array != null) {
   for (int i = 0; i < array.size(); i++) {
    for (int j = i + 1; j < array.size(); j++) {
     if (array.get(i).equals(array.get(j))) {
      areUnique = false;
     }
    }
   }
  }

  return areUnique;
 }
/*********************************************************************************************************************/

 
}