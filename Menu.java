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
 private static final String constantsFilePath = "IO/Constants.csv"; // IO/Constants.csv
 private static final String inputFilePath = "IO/Input.txt"; // IO/Input.txt
 private static final String outputFilePath = "IO/Output.txt"; // IO/Output.txt
 private static Species[] speciesList;

 
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
  Scanner keyboardReader = new Scanner(System.in);
  
  // Clear Output File
  Menu.outputToFile("", Menu.outputFilePath, false);
  
  // Read Input File
  ArrayList<Double> operatingConditions = new ArrayList<Double>();
  ArrayList<String> componentNames = new ArrayList<String>();
  ArrayList<Double> moleFractions = new ArrayList<Double>();
  ArrayList<String> availableSpeciesNames = new ArrayList<String>();
  
  try {
   Menu.readFlashSeparatorInputFile(operatingConditions, componentNames, moleFractions);
   Menu.readSpeciesData(componentNames);
  } catch (FileNotFoundException e) {
   Menu.appendToMessages(e.getMessage());
  }
  
  // Menu
  while (!exit) {
   do {
    String menuPrompt = new String();
    menuPrompt = "Flash Separation Input: \n";
    
    String operatingTemp = new String(); 
    switch((int) operatingConditions.get(0).doubleValue()) {
     case 0:
      menuPrompt += "   Calculate Isothermal Heat \n";
      operatingTemp = "   Feed Temperature: " 
      +  operatingConditions.get(2).doubleValue() + " K \n";
      break;
     case 1:
      menuPrompt += "   Calculate Flash Temperature \n";
      operatingTemp = "   Feed Temperature: " 
      +  operatingConditions.get(2).doubleValue() + " K \n";
      break;
     case 2:
      menuPrompt += "   Calculate Feed Temperature \n";
      operatingTemp = "   Flash Temperature: " 
      +  operatingConditions.get(2).doubleValue() + " K \n";
      break;
     default:
      break;
    }
    
    switch((int) operatingConditions.get(1).doubleValue()) {
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
      + " mol/h \n\n" + "Feed Stream Components: \n";
    
    for (int i = 0; i < componentNames.size(); i++) {
     menuPrompt += componentNames.get(i) + ": " 
       + moleFractions.get(i).doubleValue() * 100. + " % \n";
    }
    
    menuPrompt += "\n\nEnter 0 to proceed to the simulation." 
      + " \nEnter 1 to change the simulation input."
      + " \nEnter 9 to terminate the program.";
    
    System.out.println(menuPrompt);
    
    correctInput = false;
    try {
     menuIndex = keyboardReader.nextInt();
     correctInput = true;
     
     if (menuIndex != 0 && menuIndex != 1 && menuIndex != 9) {
      correctInput = false;
     }
    }
    catch (Exception e) {
     System.out.println("Error: Incorrect input. Enter an integer value.");
    }
   
   } while (!correctInput);
   
   if (menuIndex == 0) {
    Menu.messages.clear();
    Menu.runSimulation(operatingConditions, componentNames, moleFractions);
    
    // Print to Output File
    String messagesString = new String();
    for (int i = 0; i < Menu.messages.size(); i++) {
     messagesString += Menu.messages.get(i);
    }

    Menu.outputToFile(messagesString + "\r\n\r\n", Menu.outputFilePath, true);
    
    System.out.println("\nPress any key to continue.");
    keyboardReader.next();
   } else if (menuIndex == 1) {
    
   }
   else {
    exit = true;
   }
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
     throws FileNotFoundException {

  Scanner fileReader;
  Scanner lineReader;

  /*
  * Open Input File
  * -----------------------------------------------------------------------------
  */
  fileReader = new Scanner(new FileInputStream(Menu.inputFilePath));
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

   String name = lineReader.next();

   if (name.equals("") || name == null) {
    break;
   } else {
    System.out.println(name);
    componentNames.add(name);
    moleFractions.add(new Double(lineReader.nextDouble()));
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
 public static void readSpeciesData(ArrayList<String> componentNames) throws FileNotFoundException {

  Scanner fileReader;
  Scanner lineReader;
  
 /*
  * Open Constants File
  * -----------------------------------------------------------------------------
  */
  fileReader = new Scanner(new FileInputStream(Menu.constantsFilePath));
  fileReader.useDelimiter(",");

 /*
  * Store Species Data
  * -----------------------------------------------------------------------------
  */
  for (int i = 0; i < 3; i++) {
   fileReader.nextLine(); // Skip to Row 10
  }
  
  int componentCount = componentNames.size();
  
  // Create a deep copy of the component names array
  ArrayList<String> componentsToStore = new ArrayList<String>();
  for (int i = 0; i < componentCount; i++) {
   componentsToStore.add(componentNames.get(i));
  }

  int speciesIndex = -1;
  int componentIndex = 0;
  Menu.speciesList = new Species[componentCount]; // Initialize the species array

  // Read and store the properties of each species present in the feed stream
  while (fileReader.hasNextLine() || componentsToStore.size() > 0) {
   lineReader = new Scanner(fileReader.nextLine()); // Store current row
   lineReader.useDelimiter(",");

   speciesIndex++;
   String name = lineReader.next(); // Store the name of the species in Column A
   System.out.println(name);

   // Check whether the file reader has reached the end of the file
   if (name.equals("") || name == null) {

    // End of the Input File
    break;
   } else {

    // Species name is not blank - continue reading
    for (int i = 0; i < componentsToStore.size(); i++) {

     System.out.println("Compare to: " + componentsToStore.get(i));

     // Check whether the species is among the components of the feed stream
     if (name.equals(componentsToStore.get(i))) {
      componentsToStore.remove(i); // Since the data for the current component is about to be stored,
              // remove it from the array

      // Store each token in the rest of the line in the properties array
      double[] properties = new double[Species.propertyCount];
      for (int j = 0; j < Species.propertyCount; j++) {
       properties[j] = lineReader.nextDouble(); // Next column
       // System.out.println(properties[j]);
      }

      // Create a new species object
      Menu.speciesList[componentIndex] = new Species(name, speciesIndex, properties);
      componentIndex++;

      break;
     }
    }
   }

   lineReader.close();
  }
  fileReader.close();

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
   Menu.outputToFile("", Menu.outputFilePath, false);
   Stream[] flashStreams = new Stream[2];
   try {
    flashStreams = flashSeparator.flashCalculation();
    System.out.println("Simulation was successful. \n");
   } catch (FlashCalculationException | NumericalMethodException | FunctionException e) {
    System.out.println(e.getMessage());
    Menu.appendToMessages("Error: Simulation failed." 
      + "Flash separation could not be performed. \r\n" 
      + e.getMessage() + " \r\n\r\n");
    flashSeparator.setStatus("Simulation failed. \r\n");
   }
   
   Menu.outputToFile("\r\n\r\n" + flashSeparator.toString(), 
     Menu.outputFilePath, true);
  } catch (StreamException e) {
   System.out.println(e.getMessage());
   Menu.appendToMessages("Error: Could not build the FlashSeparator." 
     + " The composition of the feed stream is inconsistent. \r\n" 
     + e.getMessage() + " \r\n\r\n");
  } catch (Exception e) {
   System.out.println(e.getMessage());
   Menu.appendToMessages(e.getMessage() + "\r\n\r\n");
   Menu.appendToMessages("Error: Could not build the FlashSeparator. \r\n\r\n");
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

  int[] speciesIndices = Menu
    .convertSpeciesNamesToIndices(componentNames);
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

  for (int i = 0; i < Menu.speciesList.length; i++) {
   if (speciesIndex == Menu.speciesList[i].getIndex()) {
    return new Species(Menu.speciesList[i]);
   }
  }
  return null;
 }
/*********************************************************************************************************************/

 
/**********************************************************************************************************************
* 8) convertSpeciesNamesToIndices() : Converts an array of species names into
*           an array of corresponding species indices.
* ----------------------------------------------------------------------------------------------------------------------
*/
 public static int[] convertSpeciesNamesToIndices(String[] speciesNames) {

  System.out.println("Test: In Menu, print the names of the components in the feed stream.");
  for (int i = 0; i < speciesNames.length; i++) {
   System.out.println("Component " + i + ": " + speciesNames[i]);
  }

  int[] speciesIndices = new int[speciesNames.length];

  for (int i = 0; i < speciesNames.length; i++) {
   for (int j = 0; j < Menu.speciesList.length; j++) {
    Species species = Menu.speciesList[j];
    if (speciesNames[i].equals(species.getName())) {
     speciesIndices[i] = species.getIndex();
    }
   }
  }

  return speciesIndices;
 }

/*********************************************************************************************************************/

 
/**********************************************************************************************************************
* 9.1) findRoot() :
* ----------------------------------------------------------------------------------------------------------------------
*/
 public static double findRoot(Function f, double[] constants, double startPoint, 
   boolean positiveDirection) throws NumericalMethodException, FunctionException {

  // RootFinder rootFinder = new NewtonRaphsonRootFinder(maxEvaluationCount);
  RootFinder rootFinder = new RiddersMethodRootFinder(
    BracketingRootFinder.DEFAULT_INCREMENT_FACTOR * RootFinder.DEFAULT_TOLERANCE,
    positiveDirection, RootFinder.DEFAULT_MAX_EVALUATION_COUNT, true);

  return rootFinder.findRoot(f, constants, startPoint, RootFinder.DEFAULT_TOLERANCE);
 }
/*********************************************************************************************************************/

 
/**********************************************************************************************************************
* 9.2) findRoot() :
* ----------------------------------------------------------------------------------------------------------------------
*/
 public static double findRoot(Function f, double[] constants, double startPoint, double endPoint,
   double incrementLength, double tolerance, int maxEvaluationCount) 
     throws NumericalMethodException, FunctionException {

  // RootFinder rootFinder = new NewtonRaphsonRootFinder(maxEvaluationCount);
  RootFinder rootFinder = new RiddersMethodRootFinder(endPoint, incrementLength, 
    maxEvaluationCount);

  return rootFinder.findRoot(f, constants, startPoint, tolerance);
 }
/*********************************************************************************************************************/

 
/**********************************************************************************************************************
* 9.3) findRoot() :
* ----------------------------------------------------------------------------------------------------------------------
*/
 public static double findRoot(Function f, double[] constants, double startPoint, 
   boolean positiveDirection, double incrementLength, double tolerance, int maxEvaluationCount,
   boolean useFunctionBounds) 
     throws NumericalMethodException, FunctionException {

  // RootFinder rootFinder = new NewtonRaphsonRootFinder(maxEvaluationCount);
  RootFinder rootFinder = new RiddersMethodRootFinder(incrementLength, positiveDirection,
    maxEvaluationCount, useFunctionBounds);

  return rootFinder.findRoot(f, constants, startPoint, tolerance);
 }
/*********************************************************************************************************************/

 
 
/**********************************************************************************************************************
* 10) appendToMessages() :
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
}