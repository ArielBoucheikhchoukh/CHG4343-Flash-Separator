import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Scanner;
 
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

public class Menu {
  
  private static String workbookFilePath; //FlashSeparatorWorkbook_22.xlsx
  private static Workbook workbook;
  
  public Menu() {
    
  }
  
  public static void launchMenu() {
    try {
      Menu.launchWorkbook();
    }
    catch (IOException e) {
      System.out.println("I/O Exception: Could not find the Excel workbook.");
      return;
    }
    catch (InvalidFormatException e) {
      System.out.println("Invalid Format Exception: Format of the workbook is invalid.");
      return;
    }
    
    Menu.launchFlashSeparator();
    
    try {
      Menu.workbook.close();
      System.out.println("Connection to workbook successfully closed.");
    }
    catch (IOException e) {
      System.out.println("I/O Exception: Could not close the connection to the Excel workbook.");
    }
  }
  
  public static void launchWorkbook() throws IOException, InvalidFormatException {
    Menu.workbookFilePath = "Workbook/FlashSeparatorWorkbook_22.xlsx";
    workbook = new XSSFWorkbook(new File(Menu.workbookFilePath)); 
      
    System.out.println("Workbook successfully connected to.");
  }
  
  public static void launchFlashSeparator() {
    String[] componentNames = null;
    Stream feedStream = new Stream();
    
    //FlashSeparator flashSeparator = Menu.initializeFlashSeparator(componentNames, feedStream);
    Menu.initializeFlashSeparator(componentNames, feedStream);
    Species[] speciesList = Menu.readSpeciesData(componentNames);
  }
  
  public static void initializeFlashSeparator(String[] componentNames, Stream feedStream) { //should return a derived object of the the FlashSeparator class
    
    //Column Indices
    int FIRST_COL = 1;
    int FIRST_SPECIES_COL = 9;
    
    //Row Indices
    int NAME_ROW = 5;
    int VALUE_ROW = 6;
    
    Sheet inputSheet = workbook.getSheetAt(2);
    Row row = inputSheet.getRow(VALUE_ROW);
    Row nameRow = inputSheet.getRow(NAME_ROW);
    
    //Read Flash Case
    int flashCase = (int) row.getCell(FIRST_COL).getNumericCellValue();
    
    //Read Ideal/Non-Ideal Case
    int idealCase = (int) row.getCell(FIRST_COL + 1).getNumericCellValue();
    
    //Read Temperature
    double T = row.getCell(FIRST_COL + 2).getNumericCellValue();
    
    //Read Pressure
    double P = row.getCell(FIRST_COL + 3).getNumericCellValue();
    
    //Read Flowrate
    feedStream.setF(row.getCell(FIRST_COL + 4).getNumericCellValue());
    
    int componentCount = 0;
    while (nameRow.getCell(FIRST_SPECIES_COL + componentCount) != null || componentCount > 100) {
      componentCount++;
      System.out.println(componentCount);
    }
    
    componentNames = new String[componentCount];
    feedStream.setComponentCount(componentCount);
    for (int i = 0; i < componentCount; i++) {
     componentNames[i] = nameRow.getCell(FIRST_SPECIES_COL + i).getStringCellValue();
     feedStream.setZ(row.getCell(FIRST_SPECIES_COL + i).getNumericCellValue(), i);
    }
    
    //FlashSeparator flashSeparator = null;
    if (flashCase == 0) {
      //flashSeparator = new FlashSeparator(); //should be the derived class for case 1
      feedStream.setT(T);
    }
    else if (flashCase == 1) {
      //flashSeparator = new FlashSeparator(); //should be the derived class for case 2
      feedStream.setT(T);
    }
    else {
      //flashSeparator = new FlashSeparator(); //should be the derived class for case 3
    }
    
    if (idealCase == 0) {
      Species.createResidualEnthalpy(false);
    }
    else {
      Species.createResidualEnthalpy(true);
    }
    
    //return flashSeparator;
  }
  
  public static Species[] readSpeciesData(String[] componentNames) {
    Species[] speciesList = null;
    
    Sheet speciesDataSheet = workbook.getSheetAt(1);
    
    return speciesList;
  }
}