import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Menu {
  
  private static final String inputFilePath = "IO/Input_22.csv"; //IO/Input_22.csv
  private static Species[] speciesList;
  
  
/**********************************************************************************************************************
  1) Main Method : Entry point of the program.
---------------------------------------------------------------------------------------------------------------------*/
  public static void main(String[] args) {
    Menu.launchMenu(); //Go to method 2
  }
/*********************************************************************************************************************/

  
/**********************************************************************************************************************
  2) launchMenu() : Starts the menu. 
---------------------------------------------------------------------------------------------------------------------*/
  public static void launchMenu() {
    
    //Initialize the Input File Reader
    Scanner fileReader;
    
    try {
      fileReader = new Scanner(new FileInputStream(Menu.inputFilePath));
    }
    catch (FileNotFoundException e) {
      System.out.println("Input file not found.");
      return;
    }
    
    fileReader.useDelimiter(",");
    
    //Build the Flash Separator
    FlashSeparator flashSeparator;
    
    try {
      flashSeparator = Menu.buildFlashSeparator(fileReader); //Go to method 3
      fileReader.close();
    }
    catch (NoSuchElementException e) {
      System.out.println("Error reading input file.");
      return;
    }
    
    //Flash Calculation
    try {
      Stream[] flashStreams = flashSeparator.flashCalculation();
      
      System.out.println("Test: Vapour pressure of water at 100C is " + Menu.getSpecies(1).evaluateVapourPressure(373.15) + " Pa.");
      System.out.println("Test: Molar enthalpy of liquid-phase water at 100C is " 
                           + Menu.getSpecies(1).evaluateEnthalpyLiquid(373.15, 100) + " J/mol.");
      System.out.println("Test: Molar enthalpy of vapour-phase water at 100C is " 
                           + Menu.getSpecies(1).evaluateEnthalpyVapour(373.15, 100) + " J/mol.");
    }
    catch (Exception e) {
      System.out.println("Error : " + e.getMessage()); 
    }
    
    //Write Results of Flash Calculation to Output File
    //...
    
    System.out.println("Program successfully run.");
  }
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  3) buildFlashSeparaor() : Reads the species data and the user input from the input file and builds a FlashSeparator.
---------------------------------------------------------------------------------------------------------------------*/
  public static FlashSeparator buildFlashSeparator(Scanner fileReader) {
    
    Scanner lineReader;
    
    /* Array of Component Names
    -----------------------------------------------------------------------------------------------------------------*/
    fileReader.nextLine(); //Skip Row 1
    
    lineReader = new Scanner(fileReader.nextLine()); //Store Row 2
    lineReader.useDelimiter(",");
    
    for (int i = 0; i < 7; i++) {
      lineReader.next(); //Skip to Cell H2
    }
    
    //Store the names of the components in the feed stream
    ArrayList<String> componentNames = new ArrayList<String>();
    while (lineReader.hasNext()) {
      String name = lineReader.next(); //Move to next Column
      if (name.equals("") || name == null) {
        break;
      }
      else {
        componentNames.add(name);
      }
    }
    int componentCount = componentNames.size();
    lineReader.close();
    
    /* Read Cases and Operating Conditions of the Flash Separator
    -----------------------------------------------------------------------------------------------------------------*/
    int flashCase = fileReader.nextInt(); // Move to A3
    int behaviourCase = fileReader.nextInt(); // Move to B3 
    
    double T = fileReader.nextDouble(); // Move to Cell C3
    double P = fileReader.nextDouble(); // Move to Cell D3
    double F = fileReader.nextDouble(); // Move to Cell E3
    
    for (int i = 0; i < 3; i++) {
      fileReader.next(); // Skip to Cell H3
    }
    
    //Store component mole fractions
    double[] z = new double[componentCount];
    for (int i = 0; i < componentCount; i++) {
      z[i] = fileReader.nextDouble(); //Move to next Column
    }
    
    /* Store Species Data
    -----------------------------------------------------------------------------------------------------------------*/
    for (int i = 0; i < 7; i++) {
      fileReader.nextLine(); //Skip to Row 10
    }
    
    //Create a deep copy of the component names array
    ArrayList<String> componentsToStore = new ArrayList<String>();
    for (int i = 0; i < componentCount; i++) {
      componentsToStore.add(componentNames.get(i));
    }
    
    int speciesIndex = 0;
    Menu.speciesList = new Species[componentCount]; // Initialize the species array
    
    //Read and store the properties of each species present in the feed stream
    while (fileReader.hasNextLine() || componentsToStore.size() > 0) {
      lineReader = new Scanner(fileReader.nextLine()); // Store current row
      lineReader.useDelimiter(",");
      
      String name = lineReader.next(); // Store the name of the species in Column A
      System.out.println(name);
      
      // Check whether the file reader has reached the end of the file
      if (name.equals("") || name == null) { 
        
        //End of the Input File
        break; 
      }
      else {
        
        //Species name is not blank - continue reading
        for (int i = 0; i < componentsToStore.size(); i++) {
          
          System.out.println("Compare to: " + componentsToStore.get(i));
          
          //Check whether the species is among the components of the feed stream
          if (name.equals(componentsToStore.get(i))) {
            componentsToStore.remove(i); // Since the data for the current component is about to be stored, remove it from the array
            
            //Store each token in the rest of the line in the properties array
            double[] properties = new double[Species.propertyCount];
            for (int j = 0; j < Species.propertyCount; j++) {
              properties[j] = lineReader.nextDouble(); // Next column
            }
            
            //Create a new species object
            Menu.speciesList[speciesIndex] = new Species(name, speciesIndex, properties);
            
            speciesIndex++;
            
            break;
          }
        }
      }
      
      lineReader.close();
    }
    
    /* Create FlashSeparator and Feed Stream Objects
    -----------------------------------------------------------------------------------------------------------------*/
    int[] speciesIndices = Menu.convertSpeciesNamesToIndices(componentNames.toArray(new String[componentNames.size()]));
    FlashSeparator flashSeparator;
    switch (flashCase) {
      
      case 0: //Isothermal Non-Adiabatic Operation; Find Q
        flashSeparator = new IsothermalHeat(T, P, F, z, speciesIndices);
        break;
        
      case 1: //Adiabatic Operation; Find the Flash Temperature
        flashSeparator = new AdiabaticFlashTemp(T, P, F, z, speciesIndices);
        break;
        
      default: //Adiabatic Operation; Find the Feed Temperature
        flashSeparator = new AdiabaticFeedTemp(T, P, F, z, speciesIndices);
        break;
    }
    
    //Create Behaviour Object
    switch (behaviourCase) {
      case 0: //Ideal Behaviour
        flashSeparator.setBehaviour(false);
        break;
        
      default: //Non-Ideal Behaviour
        flashSeparator.setBehaviour(true);
        break;
    }
    
    return flashSeparator;
  }
/*********************************************************************************************************************/


/**********************************************************************************************************************
  4) getSpecies() : Returns a copy of the species at the given index.
----------------------------------------------------------------------------------------------------------------------*/
  public static Species getSpecies(int speciesIndex) {
    return new Species(Menu.speciesList[speciesIndex]); // Guarantees data integrity, but very wasteful...
  }
/*********************************************************************************************************************/
  

/**********************************************************************************************************************
  5) getSpeciesName() : Returns a copy of the name of a species at the given index.
----------------------------------------------------------------------------------------------------------------------*/
  public static String getSpeciesName(int speciesIndex) {
    return Menu.speciesList[speciesIndex].getName();
  }
/*********************************************************************************************************************/
  
  
/**********************************************************************************************************************
  6) convertSpeciesNamesToIndices() : Converts an array of species names into an array of corresponding species indices.
----------------------------------------------------------------------------------------------------------------------*/
  public static int[] convertSpeciesNamesToIndices(String[] speciesNames) {
    
    int[] speciesIndices = new int[speciesNames.length];
    
    for (int i = 0; i < speciesNames.length; i++) {
      for (int j = 0; j < Menu.speciesList.length; j++) {
        if (speciesNames[i] == Menu.getSpeciesName(j)) {
          speciesIndices[i] = j;
        }
      }
    }
    
    return speciesIndices;
  }
/*********************************************************************************************************************/
  
}