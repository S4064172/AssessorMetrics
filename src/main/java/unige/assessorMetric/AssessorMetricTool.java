package unige.assessorMetric;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Quintet;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import jdk.internal.org.jline.utils.Log;


public class AssessorMetricTool {
	
	static Logger logger = LoggerFactory.getLogger(AssessorMetricTool.class);
	
	public static void main(String[] args) throws IOException{
		
		HashMap<String,HashMap<String, Triplet<
			HashMap<String, HashMap<String, Integer>>, 
			HashMap<String, Pair<String, Integer>>,
			HashMap<String, Pair<String, Integer>>> >> statistic = new HashMap<String, HashMap<String,Triplet<
					HashMap<String, HashMap<String, Integer>>, 
					HashMap<String, Pair<String, Integer>>,
					HashMap<String, Pair<String, Integer>>>>>();
		
		HashMap<String, Triplet<
		HashMap<String, HashMap<String, Integer>>, 
		HashMap<String, Pair<String, Integer>>,
		HashMap<String, Pair<String, Integer>>> > result;
		
		logger.info("Input inputDir: "+args[0]);
		logger.info("Input outBaseDir: "+args[1]);
		logger.info("Input folder: "+args[2]);
		logger.info("Input namefile: "+args[3]);
		String inputDir = args[0];
		String outBaseDir = args[1];
		String folder = args[2];
		String namefile = args[3];
		
		if(logger.isTraceEnabled())		   
			logger.trace("*****TRACE LEVEL****");
		else if(logger.isDebugEnabled())
			logger.debug("*****DEBUG LEVEL****");
		else if(logger.isInfoEnabled())
			logger.info("*****INFO LEVEL****");
		else if(logger.isWarnEnabled())
			logger.warn("*****WARN LEVEL****");
		else if(logger.isErrorEnabled())
			logger.error("*****ERROR LEVEL****");
		
		
		
		for(File file : searchFilesToAnalyze(inputDir)) {
			CompilationUnit compilationUnit = recoverCompilationUnit(file);
			TreeParser parser = new TreeParser();
			logger.trace(compilationUnit.toString());
			result = parser.analyzeCompilationUnit(compilationUnit);
			statistic.put(file.getName(), result);
			
		}
		writeResutl(statistic,outBaseDir,folder,namefile);
		logger.info("Refactoring complete");
	}
	
	
	private static void writeResutl(HashMap<String,HashMap<String, Triplet<
			HashMap<String, HashMap<String, Integer>>, 
			HashMap<String, Pair<String, Integer>>,
			HashMap<String, Pair<String, Integer>>> >> statistic,
			String path, String folder, String namefile) {
		// workbook object
        XSSFWorkbook workbook = new XSSFWorkbook();
  
        // spreadsheet object
        XSSFSheet spreadsheet = workbook.createSheet("NumeroLocatorDuplicatiNelMetodo");
  
        String csvfile = namefile+";";
        // creating a row object
        XSSFRow row;
        int rowId = 0;
        int cellid = 0;
        Set<String> fileList = statistic.keySet();
        logger.trace("File list:" + fileList.toString());
        
        row = spreadsheet.createRow(rowId++);
        Cell cell = row.createCell(cellid++);
		cell.setCellValue("FileName");
		cell = row.createCell(cellid++);
		cell.setCellValue("ClassName");
		cell = row.createCell(cellid++);
		cell.setCellValue("MethodName");
		cell = row.createCell(cellid++);
		cell.setCellValue("LocarotName");
		cell = row.createCell(cellid++);
		cell.setCellValue("Metrics");
        
        int classNumber = 0;
        int totLocator = 0;
        int totDupLoc = 0;
        for (String file : fileList) {
        	 Set<String> classKeyList = statistic.get(file).keySet();
             logger.trace("Class key list:" + classKeyList.toString());
             classNumber += classKeyList.size();
             for (String classKey : classKeyList) {
             	HashMap<String, HashMap<String, Integer>> methodLocators = statistic.get(file).get(classKey).getValue0();
             	        	
             	Set<String> methodKeyList = methodLocators.keySet();
             	logger.trace("method key list:" + methodKeyList.toString());
             	
             	for (String methodKey : methodKeyList) {
             		HashMap<String, Integer> locators = methodLocators.get(methodKey);
             		
             		Set<String> locatorKeyList = locators.keySet();
                 	logger.trace("method key list:" + locatorKeyList.toString());
                 	totLocator += locatorKeyList.size();
                 	for (String locatorKey : locatorKeyList) {
                 		 cellid = 0;
                 		 row = spreadsheet.createRow(rowId++);
                 		 cell = row.createCell(cellid++);
                 		 cell.setCellValue((String)file);
                 		 cell = row.createCell(cellid++);
                 		 cell.setCellValue((String)classKey);
                 		 cell = row.createCell(cellid++);
                 		 cell.setCellValue((String)methodKey);
                 		 cell = row.createCell(cellid++);
                 		 cell.setCellValue((String)locatorKey);
                 		 cell = row.createCell(cellid++);
                 		 cell.setCellValue(locators.get(locatorKey));
                 		 totDupLoc += locators.get(locatorKey);
     				}            	
     			}
             	
     		}
		}
        
        spreadsheet = workbook.createSheet("NumeroDiLocatorStessoMetodo");
        
        // creating a row object
        rowId = 0;
        cellid = 0;
        fileList = statistic.keySet();
        logger.trace("File list:" + fileList.toString());
        
        row = spreadsheet.createRow(rowId++);
        cell = row.createCell(cellid++);
		cell.setCellValue("FileName");
		cell = row.createCell(cellid++);
		cell.setCellValue("ClassName");
		cell = row.createCell(cellid++);
		cell.setCellValue("MethodName");
		cell = row.createCell(cellid++);
		cell.setCellValue("Metrics");
	        
		 int totLocFunc = 0;
		 int totLocator1 = 0;
        for (String file : fileList) {
        	 Set<String> classKeyList = statistic.get(file).keySet();
             logger.trace("Class key list:" + classKeyList.toString());
             
             for (String classKey : classKeyList) {
             	HashMap<String, HashMap<String, Integer>> methodLocators = statistic.get(file).get(classKey).getValue0();
             	        	
             	Set<String> methodKeyList = methodLocators.keySet();
             	logger.trace("method key list:" + methodKeyList.toString());
             	totLocator1 += methodKeyList.size();
             	for (String methodKey : methodKeyList) {
             		HashMap<String, Integer> locators = methodLocators.get(methodKey);
             		
             		Set<String> locatorKeyList = locators.keySet();
                 	logger.trace("method key list:" + locatorKeyList.toString());
                 	
	         		 cellid = 0;
	         		 row = spreadsheet.createRow(rowId++);
	         		 cell = row.createCell(cellid++);
	         		 cell.setCellValue((String)file);
	         		 cell = row.createCell(cellid++);
	         		 cell.setCellValue((String)classKey);
	         		 cell = row.createCell(cellid++);
	         		 cell.setCellValue((String)methodKey);
	         		 cell = row.createCell(cellid++);
	         		 cell.setCellValue(locatorKeyList.size()); 
	         		totLocFunc += locatorKeyList.size();
	         		
     			}
             	
     		}
		}
        
        // spreadsheet object
        spreadsheet = workbook.createSheet("NumeroDiMetodi");
  
              
        rowId = 0;
        cellid = 0;
        fileList = statistic.keySet();
        logger.trace("File list:" + fileList.toString());
        
        row = spreadsheet.createRow(rowId++);
        cell = row.createCell(cellid++);
		cell.setCellValue("FileName");
		cell = row.createCell(cellid++);
		cell.setCellValue("ClassName");
		cell = row.createCell(cellid++);
		cell.setCellValue("Metrics");
		
		int totMethod = 0;
        for (String file : fileList) {
        	 Set<String> classKeyList = statistic.get(file).keySet();
             logger.trace("Class key list:" + classKeyList.toString());
             
             for (String classKey : classKeyList) {
             	HashMap<String, HashMap<String, Integer>> methodLocators = statistic.get(file).get(classKey).getValue0();
             	        	
             	Set<String> methodKeyList = methodLocators.keySet();
             	logger.trace("method key list:" + methodKeyList.toString());
             	totMethod += methodKeyList.size();
	     		cellid = 0;
	     		row = spreadsheet.createRow(rowId++);
	     		cell = row.createCell(cellid++);
	     		cell.setCellValue((String)file);
	     		cell = row.createCell(cellid++);
	     		cell.setCellValue((String)classKey);
	     		cell = row.createCell(cellid++);
	     		cell.setCellValue(methodKeyList.size());
                 		 	             	
     		}
		}
        
        
     // spreadsheet object
        spreadsheet = workbook.createSheet("LunghezzaDeiMetodi");
  
              
        rowId = 0;
        cellid = 0;
        fileList = statistic.keySet();
        logger.trace("File list:" + fileList.toString());
        
        row = spreadsheet.createRow(rowId++);
        cell = row.createCell(cellid++);
		cell.setCellValue("FileName");
		cell = row.createCell(cellid++);
		cell.setCellValue("ClassName");
		cell = row.createCell(cellid++);
		cell.setCellValue("MethodName");
		cell = row.createCell(cellid++);
		cell.setCellValue("isForm");
		cell = row.createCell(cellid++);
		cell.setCellValue("Metrics");
		
		int totLenghtNoFrom = 0;
		int totLenghtYesFrom = 0;
		int totNoFrom = 0;
		int totYesFrom = 0;
        for (String file : fileList) {
        	 Set<String> classKeyList = statistic.get(file).keySet();
             logger.trace("Class key list:" + classKeyList.toString());
             
             for (String classKey : classKeyList) {
             	HashMap<String,   Pair<String, Integer>> methodSize = statistic.get(file).get(classKey).getValue1();
             	        	
             	Set<String> methodKeyList = methodSize.keySet();
             	logger.trace("method key list:" + methodKeyList.toString());
             	
             	for (String methodKey : methodKeyList) {
             		
                 		 cellid = 0;
                 		 row = spreadsheet.createRow(rowId++);
                 		 cell = row.createCell(cellid++);
                 		 cell.setCellValue((String)file);
                 		 cell = row.createCell(cellid++);
                 		 cell.setCellValue((String)classKey);
                 		 cell = row.createCell(cellid++);
                 		 cell.setCellValue((String)methodKey);
                 		 cell = row.createCell(cellid++);
                 		 cell.setCellValue((String)methodSize.get(methodKey).getValue0());
                		 cell = row.createCell(cellid++);
                 		 cell.setCellValue(methodSize.get(methodKey).getValue1());
                 		 
                 		 if ("N".equals(methodSize.get(methodKey).getValue0())) {
                 			totLenghtNoFrom += methodSize.get(methodKey).getValue1();
                 			totNoFrom++;
                 		 }else {
                 			totLenghtYesFrom += methodSize.get(methodKey).getValue1();
                 			totYesFrom ++;
                 		 }                				            	
     			}              		 	             	
     		}
		}
        
     // spreadsheet object
        spreadsheet = workbook.createSheet("NumeroDiParametri");
  
              
        rowId = 0;
        cellid = 0;
        fileList = statistic.keySet();
        logger.trace("File list:" + fileList.toString());
        
        row = spreadsheet.createRow(rowId++);
        cell = row.createCell(cellid++);
		cell.setCellValue("FileName");
		cell = row.createCell(cellid++);
		cell.setCellValue("ClassName");
		cell = row.createCell(cellid++);
		cell.setCellValue("MethodName");
		cell = row.createCell(cellid++);
		cell.setCellValue("FormType");
		cell = row.createCell(cellid++);
		cell.setCellValue("Metrics");
		
		int totParamNoFrom = 0;
		int totParamYesFrom = 0;
        for (String file : fileList) {
        	 Set<String> classKeyList = statistic.get(file).keySet();
             logger.trace("Class key list:" + classKeyList.toString());
             
             for (String classKey : classKeyList) {
             	HashMap<String,   Pair<String, Integer>> methodSize = statistic.get(file).get(classKey).getValue2();
             	        	
             	Set<String> methodKeyList = methodSize.keySet();
             	logger.trace("method key list:" + methodKeyList.toString());
             	
             	for (String methodKey : methodKeyList) {
             		
                 		 cellid = 0;
                 		 row = spreadsheet.createRow(rowId++);
                 		 cell = row.createCell(cellid++);
                 		 cell.setCellValue((String)file);
                 		 cell = row.createCell(cellid++);
                 		 cell.setCellValue((String)classKey);
                 		 cell = row.createCell(cellid++);
                 		 cell.setCellValue((String)methodKey);
                 		 cell = row.createCell(cellid++);
                 		 cell.setCellValue((String)methodSize.get(methodKey).getValue0());
                 		 cell = row.createCell(cellid++);
                 		 cell.setCellValue(methodSize.get(methodKey).getValue1());	
                 		 
                 		if ("N".equals(methodSize.get(methodKey).getValue0())) {
                 			totParamNoFrom += methodSize.get(methodKey).getValue1();
                 		 }else {
                 			totParamYesFrom += methodSize.get(methodKey).getValue1();
                 		 } 
     			}     	
     		}
		}
        
        File directory = new File(path+"//"+folder);
        if (! directory.exists()){
            directory.mkdirs();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }
        File file = new File(path+"//"+folder+"//"+namefile+".xlsx");
        if (file.exists()){
            file.delete();
        }  
        
		try {
			FileOutputStream out = new FileOutputStream(file);
			workbook.write(out);
            out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		csvfile += 
				classNumber + ";"+ 
				totMethod + ";" +
				totNoFrom + ";" +  totYesFrom + ";" + 
				(totLenghtNoFrom+totLenghtYesFrom)  + ";" + 
				((namefile.contains("old")?(totLenghtNoFrom+totLenghtYesFrom)-totMethod:(totLenghtNoFrom+totLenghtYesFrom)-totLocator))  + ";" + 
				totLenghtNoFrom + ";" +  totLenghtYesFrom + ";" +
				(float)(totLenghtNoFrom+totLenghtYesFrom)/(totNoFrom+totYesFrom) + ";" +
				(float)totLenghtNoFrom/totNoFrom + ";"  + 
				(float)totLenghtYesFrom/totYesFrom + ";"  + 
				(totParamNoFrom+totParamYesFrom) + ";"  + 
				totParamNoFrom + ";"  + 
				totParamYesFrom + ";"  + 
				(float)(totParamNoFrom+totParamYesFrom)/(totNoFrom+totYesFrom) + ";"  +
				(float)totParamNoFrom/totNoFrom + ";"  + 
				(float)totParamYesFrom/totYesFrom + ";" + 
				totLocator + ";" + 
				(float)totDupLoc/totLocator + ";" + 
				(float)totLocFunc/totLocator1 + ";";	
		logger.warn(csvfile);
		String savestr = "H://_TESI//_Fase7_EsecuzioneMetrice//MetricsReport//Report//mysave.csv"; 
		File f = new File(savestr);

		PrintWriter out = null;
		if ( f.exists() && !f.isDirectory() ) {
		    try {
				out = new PrintWriter(new FileOutputStream(new File(savestr), true));
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
		    try {
				out = new PrintWriter(savestr);
				out.append(
						"Nome Applicazione;"
						+ "Numero di Classi;"
						+ "Numero di metodi;"
						+ "Numero di setter-getter;"
						+ "Numero di metodi-form;"
						+ "Totale linee codice;"
						+ "Totale linne codice no wait;"
						+ "Totale linee codice setter-getter;"
						+ "Totale linee codice medoti-form;"
						+ "Media lunghezza metodi;"
						+ "Media lunghezza metodi setter-getter;"
						+ "Media lunghezza metodi metodi-form;"
						+ "Numeto di parametri;"
						+ "Numeto di parametri setter-getter;"
						+ "Numeto di parametri metodi-form;"
						+ "Meida del numero dei parametri;"
						+ "Meida del numero dei parametri setter-getter;"
						+ "Meida del numero dei parametri metodi-form;"
						+ "Numero dei locator;"
						+ "Media dei locator duplicati nella stessa funzione;"
						+ "Media dei locator nella stessa funzione\n");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		out.append(csvfile+"\n");
		out.close();
		
 	} 
			
	private static List<File> searchFilesToAnalyze(String inputDir) {
		File dir = new File(inputDir);
		
		List<File> matchingFiles = new LinkedList<File>();
		matchingFiles.addAll(Arrays.asList(dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String fileName) {
				return fileName.contains(".java");
			}		
		})));
		
		String[] directories = dir.list(new FilenameFilter() {
			  @Override
			  public boolean accept(File current, String name) {
			    return new File(current, name).isDirectory();
			  }
		});
		
		for (String directory : directories) {
			dir = new File(inputDir+"//"+directory);
			matchingFiles.addAll(Arrays.asList(dir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String fileName) {
					return fileName.contains(".java");
				}		
			})));
		}
		
		return matchingFiles;
	}
	
	private static CompilationUnit recoverCompilationUnit(File file) throws IOException {
		// Set up a minimal type solver that only looks at the classes used to run this sample.
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        
        // Configure JavaParser to use type resolution
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
        String fileText = Files.readString(Path.of(file.getAbsolutePath()));
        
        // Parse some code
        return StaticJavaParser.parse(fileText);
   }
	
}
