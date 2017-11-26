import java.util.ArrayList;
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
 
 private static ArrayList<String> messages;
 private static final String SPECIES_FILE_PATH = "IO/Species.csv"; // IO/Spcies.csv
 private static final String SUB_GROUPS_FILE_PATH = "IO/SubGroups.csv"; // IO/SubGroups.csv
 private static final String INTERACTION_PARAMETERS_FILE_PATH 
       = "IO/InteractionParameters.csv"; // IO/InteractionParameters.csv
 private static final String INPUT_FILE_PATH = "IO/Input.txt"; // IO/Input.txt
 private static final String OUTPUT_FILE_PATH = "IO/Output.txt"; // IO/Output.txt
 private static Species[] species;
 private static int[] subGroupIndices;
 private static double[] subGroupRelativeVolume; //R
 private static double[] subGroupRelativeSurfaceArea; //Q
 private static double[][] interactionParameters;

 
/**********************************************************************************************************************
* 1) Main Method : Entry point of the program.
* ---------------------------------------------------------------------------------------------------------------------
*/
 public static void main(String[] args) {
  Menu.messages = new ArrayList<String>();
  Menu.launchMenu(); // Go to method 2
 }
/*********************************************************************************************************************/

 
/**********************************************************************************************************************
* 2) launchMenu() : Starts the menu.
* ---------------------------------------------------------------------------------------------------------------------
*/
 public static void launchMenu() {
  
  int menuIndex = 0;
  boolean correctInput = false;
  boolean exit = false;
  
  ArrayList<Double> operatingConditions = new ArrayList<Double>();
  ArrayList<String> componentNames = new ArrayList<String>();
  ArrayList<Double> moleFractions = new ArrayList<Double>();
  
  Scanner keyboardReader = new Scanner(System.in);
  
  try {
   // Read Input File
   Menu.readFlashSeparatorInputFile(operatingConditions, componentNames, moleFractions);
   Menu.readSpeciesData(componentNames);
   
   // Clear Output File
   Menu.outputToFile("", Menu.OUTPUT_FILE_PATH, false);

   // Menu
   while (!exit) {
    do {
     String menuPrompt = new String();
     menuPrompt = "Flash Separation Input: \n";

     String operatingTemp = new String();
     switch ((int) operatingConditions.get(0).doubleValue()) {
     case 0:
      menuPrompt += "   Calculate Isothermal Heat \n";
      operatingTemp = "   Feed Temperature: " 
        + operatingConditions.get(2).doubleValue() + " K \n";
      break;
     case 1:
      menuPrompt += "   Calculate Flash Temperature \n";
      operatingTemp = "   Feed Temperature: " 
        + operatingConditions.get(2).doubleValue() + " K \n";
      break;
     case 2:
      menuPrompt += "   Calculate Feed Temperature \n";
      operatingTemp = "   Flash Temperature: " 
        + operatingConditions.get(2).doubleValue() + " K \n";
      break;
     default:
      break;
     }

     switch ((int) operatingConditions.get(1).doubleValue()) {
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
     menuPrompt += "   Tank Pressure: " + operatingConditions.get(3).doubleValue() 
       + " bar \n";
     menuPrompt += "   Feed Flow Rate: " + operatingConditions.get(4).doubleValue() 
       + " mol/h \n\n"
       + "Feed Stream Components: \n";

     for (int i = 0; i < componentNames.size(); i++) {
      menuPrompt += componentNames.get(i) + ": " 
        + moleFractions.get(i).doubleValue() * 100.
        + " % \n";
     }

     menuPrompt += "\n\nEnter 0 to terminate the program."
       + " \nEnter 1 to proceed to the simulation." 
       + " \nEnter 9 for instructions.";
     
     System.out.println(menuPrompt);

     correctInput = false;
     try {
      menuIndex = keyboardReader.nextInt();
      correctInput = true;

      if (menuIndex != 0 && menuIndex != 1 && menuIndex != 9) {
       correctInput = false;
      }
     } catch (Exception e) {
      System.out.println("Error: Incorrect input. Enter an integer value.");
     }

    } while (!correctInput);

    switch (menuIndex) {
     case 0: 
      exit = true;
      break;
     case 1:
      Menu.messages.clear();
      Menu.runSimulation(operatingConditions, componentNames, moleFractions);
 
      // Print to Output File
      String messagesString = new String();
      for (int i = 0; i < Menu.messages.size(); i++) {
       messagesString += Menu.messages.get(i);
      }
 
      Menu.outputToFile(messagesString + "\r\n\r\n", Menu.OUTPUT_FILE_PATH, true);
 
      System.out.println("\nPress any key to continue.");
      keyboardReader.next();
      
      break;
     case 9:
      
      break;
     default:
      exit = true;
      break;
    }
   }
  } catch (Exception e) {
   System.out.println("\r\nError: Incorrect input. \r\n" + e.getMessage() + "\r\n\r\n");
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
  
  fileReader = new Scanner(new FileInputStream(Menu.INPUT_FILE_PATH));
  fileReader.useDelimiter(",");

  /*
  * Read Cases and Operating Conditions of the Flash Separator
  * -----------------------------------------------------------------------------
  */
  fileReader.nextLine(); // Skip Row 1
  
  operatingConditions.add(new Double((double) fileReader.nextInt())); // Move to A3
  operatingConditions.add(new Double((double) fileReader.nextInt())); // Move to B3
  operatingConditions.add(new Double(fileReader.nextDouble() + 273.15)); // Move to Cell C3
  operatingConditions.add(new Double(fileReader.nextDouble())); // Move to Cell D3
  operatingConditions.add(new Double(fileReader.nextDouble())); // Move to Cell E3

  /*
  * Read Feed Stream Composition
  * -----------------------------------------------------------------------------
  */
  fileReader.nextLine(); // Move to end of Row 2
  fileReader.nextLine(); // Skip Row 3

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
    throw new IOException("Abberant carriage return characters in " + Menu.INPUT_FILE_PATH 
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
     moleFractions.add(new Double(lineReader.nextDouble()));
    }
   }

   lineReader.close();
  }

  fileReader.close();
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
  Scanner fileReader;
  Scanner lineReader;
  
  int speciesCount = speciesNamesList.size();;
  int subGroupCount = 0;
  int speciesSubGroupCount;
  int masterTotalSubGroupCount;
  int totalSubGroupCount;
  int storedSpeciesIterator = 0;
  int speciesIndex;
  int subGroupIndex;
  double interactionParameter;
  
  String name;
  
  ArrayList<String> allSpeciesNamesList;
  ArrayList<Integer> allSpeciesIndicesList;
  ArrayList<String> speciesToStoreNamesList;
  ArrayList<String> allSubGroupNamesList;
  ArrayList<String> allSubGroupNamesList2;
  ArrayList<Integer> allSubGroupIndicesList;
  ArrayList<Integer> subGroupPositionsList;
  ArrayList<String> subGroupNamesList;
  ArrayList<Integer> speciesSubGroupIndicesList;
  ArrayList<Integer> speciesSubGroupCountsList;
  
  double[] properties;
  double[] correlationParameters;
  int[][] speciesSubGroups;
  
  boolean isUnique;
  
 /*
  * II. Store Species Data
  * -----------------------------------------------------------------------------
  */
  // i) Open the Species.csv File
  fileReader = new Scanner(new FileInputStream(Menu.SPECIES_FILE_PATH));
  fileReader.useDelimiter(",");
  
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
  while (fileReader.hasNextLine() || speciesToStoreNamesList.size() > 0) {
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
      speciesSubGroupIndicesList = new ArrayList<Integer>();
      speciesSubGroupCountsList = new ArrayList<Integer>();
      for (int j = 0; j < masterTotalSubGroupCount; j++) {
       String token = lineReader.next();
       
       //Check if the current sub-group count is non-zero
       if (token != null && !token.equals("")) {
        speciesSubGroupCount = Integer.parseInt(token); 
        if (speciesSubGroupCount != 0) {
         
         //If this species possesses this sub-group, 
         //add it to the species' own array
         speciesSubGroupIndicesList.add(new Integer(j));
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
      speciesSubGroups = new int[2][speciesSubGroupIndicesList.size()];
      for (int j = 0; j < speciesSubGroupIndicesList.size(); j++) {
       speciesSubGroups[0][j] = speciesSubGroupIndicesList.get(j).intValue();
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
 * Store Sub-Group Data
 * -----------------------------------------------------------------------------
 */
  // i) Open the SubGroups.csv File
  fileReader = new Scanner(new FileInputStream(Menu.SUB_GROUPS_FILE_PATH));
  fileReader.useDelimiter(",");
  fileReader.nextLine();
  
  // ii) Initialize Arrays
  Menu.subGroupIndices = new int[subGroupCount];
  Menu.subGroupRelativeVolume = new double[subGroupCount];
  Menu.subGroupRelativeSurfaceArea = new double[subGroupCount];
  
  allSubGroupNamesList.clear();
  allSubGroupIndicesList = new ArrayList<Integer>();
  
  // iii) Read and store the properties of each sub-group to be used in the simulation
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
 * Store Interaction Parameters
 * -----------------------------------------------------------------------------
 */
  // i) Open the InteractionParameters.csv file
  fileReader = new Scanner(new FileInputStream(Menu.INTERACTION_PARAMETERS_FILE_PATH));
  fileReader.useDelimiter(",");
  fileReader.nextLine(); // Skip a row
  
  lineReader = new Scanner(fileReader.nextLine()); // Store current row
  lineReader.useDelimiter(",");
  lineReader.next();
  
  // ii) Initialize arrays
  Menu.interactionParameters = new double[subGroupCount][subGroupCount];
  allSubGroupNamesList.clear();
  allSubGroupNamesList2 = new ArrayList<String>();
  
  // iii) Store the sub-group names along the horizontal axis
  totalSubGroupCount = 0;
  while (lineReader.hasNext()) {
   allSubGroupNamesList.add(lineReader.next());
   totalSubGroupCount++;
  }
  lineReader.close();
  
  // iV) Read and store the interaction parameters of all sub-groups to be used in the simulation
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
    Menu.outputToFile("\r\nSimulation was successful." 
      + " Flash separation was performed. \r\n", Menu.OUTPUT_FILE_PATH, true);
   } catch (FlashCalculationException | NumericalMethodException | FunctionException e) {
    System.out.println(e.getMessage());
    Menu.outputToFile("\r\nError: Simulation failed." 
      + " Flash separation could not be performed. \r\n" 
      + e.getMessage() + " \r\n\r\n", Menu.OUTPUT_FILE_PATH, true);
   }
   
   Menu.outputToFile("\r\n\r\n" + flashSeparator.toString(), 
     Menu.OUTPUT_FILE_PATH, true);
  } catch (StreamException e) {
   System.out.println(e.getMessage());
   Menu.appendToMessages("\r\nError: Could not build the FlashSeparator." 
     + " The composition of the feed stream is inconsistent. \r\n" 
     + e.getMessage() + " \r\n\r\n");
  } catch (Exception e) {
   System.out.println(e.getMessage());
   Menu.appendToMessages(e.getMessage() + "\r\n\r\n");
   Menu.appendToMessages("\r\nError: Could not build the FlashSeparator. \r\n\r\n");
  }
  
 }
 
 
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
  
  FlashSeparator flashSeparator;
  switch (flashCase) {

  case 0: // Isothermal Non-Adiabatic Operation; Find Q
   flashSeparator = new IsothermalHeat(T, P, F, z, speciesIndices);
   break;

  case 1: // Adiabatic Operation; Find the Flash Temperature
   flashSeparator = new AdiabaticFlashTemp(T, P, F, z, speciesIndices);
   break;

  default: // Adiabatic Operation; Find the Feed Temperature
   flashSeparator = new AdiabaticFeedTemp(T, P, F, z, speciesIndices);
   break;
  }

  // Create Behaviour Object
  switch (behaviourCase) {
  case 0: // Ideal Behaviour
   flashSeparator.setBehaviour(false);
   break;

  default: // Non-Ideal Behaviour
   flashSeparator.setBehaviour(true);
   break;
  }

  return flashSeparator;
 }
/*********************************************************************************************************************/

 
/**********************************************************************************************************************
* 6) outputToFile() : Prints a String to a text file.
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
* 7) getSpecies() : Returns a copy of the species at the given index.
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
* 8) getSubGroupR() : Returns the relative volume of the sub-group at the given index.
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
* 9) getSubGroupQ() : Returns the relative surface area of the sub-group at the given index.
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
* 10) getInteractionParameter() : Returns the interaction parameter of the subgroups i and j.
* ----------------------------------------------------------------------------------------------------------------------
*/
 public static double getInteractionParameter(int i, int j) {
  return Menu.interactionParameters[i][j];
 }
/*********************************************************************************************************************/

 
/**********************************************************************************************************************
* 11.1) findRoot() :
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
* 11.2) findRoot() :
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
* 11.3) findRoot() :
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
* 12) appendToMessages() :
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
* 13) areArrayListElementsUnique() : Checks if all of the elements inside the ArrayList are unique.
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