package unige.assessorMetric;

import java.util.HashMap;
import java.util.List;

import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Quintet;
import org.javatuples.Triplet;
import org.javatuples.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;

public class TreeParser {

	static Logger logger = LoggerFactory.getLogger(TreeParser.class);
	HashMap<String,Triplet<
		HashMap<String, HashMap<String, Integer>>, 
		HashMap<String, Pair<String, Integer>>,
		HashMap<String, Pair<String, Integer>>> > statistic;
	
	public TreeParser() {
		statistic = new HashMap<String,Triplet<
				HashMap<String, HashMap<String, Integer>>, 
				HashMap<String, Pair<String, Integer>>,
				HashMap<String, Pair<String, Integer>>> >();
	}
	
	
	/** Starting Point to analyze a Java File
	 * For each import in this file, if the import wasn't declared in the Main Compilation unit, this import will be added
	 * Also because the import is needed for the test method, this import will be added to the PageObject class
	 * @param unitToAnalyze
	 */
	public 
	HashMap<String,Triplet<HashMap<String, HashMap<String, Integer>>, HashMap<String, Pair<String, Integer>>,HashMap<String, Pair<String, Integer>>>>
	analyzeCompilationUnit(CompilationUnit unitToAnalyze) {
		//Search for all the Class declaration in the files
 		for(ClassOrInterfaceDeclaration classToAnalyze : unitToAnalyze.findAll(ClassOrInterfaceDeclaration.class)) {
 			logger.trace(classToAnalyze.getNameAsString());
 			if (classToAnalyze.getNameAsString().equals("MyUtils") ) {
 				logger.warn("Skipped class: "+classToAnalyze.getNameAsString());
 				continue;
 			}
 			Triplet<HashMap<String, HashMap<String, Integer>>,HashMap<String, Pair<String, Integer>>,HashMap<String, Pair<String, Integer>>>  result = analyzeClass(classToAnalyze);
 			statistic.put(classToAnalyze.getNameAsString(), result);
 			logger.info("result for class" + classToAnalyze.getNameAsString()+ " " + statistic.get(classToAnalyze.getNameAsString()));
 		}
 		return  statistic;
	}
	
	
	
	/** Analyze the class searching for Field Declaration, Method Declaration
	 * Because the class will return itself, the class analysis is skipped, the main operation is to analyze only Field Declaration and MethodDeclaration
	 * Other operation can cause the IllegalArgumentException with the class type that has generated
	 * @param classToAnalyze
	 */
	private
	Triplet<
		HashMap<String, HashMap<String, Integer>>, 
		HashMap<String, Pair<String, Integer>>,
		HashMap<String, Pair<String, Integer>>>
	analyzeClass(ClassOrInterfaceDeclaration classToAnalyze) {
		HashMap<String, HashMap<String, Integer>> tempMethodLocatorMapping = new HashMap<String, HashMap<String, Integer>>();
		HashMap<String, Pair<String, Integer>> tempMethodSize = new HashMap<String, Pair<String, Integer>>();
		HashMap<String, Pair<String, Integer>> tempMethodParamites = new HashMap<String, Pair<String, Integer>>();
		for(BodyDeclaration<?> declaration : classToAnalyze.findAll(BodyDeclaration.class)) {
			if(declaration.isFieldDeclaration()) {	
				logger.trace("Skip field declaration: " + declaration.toString());
			}else if(declaration.isMethodDeclaration()) {
				MethodDeclaration method = declaration.asMethodDeclaration();
				logger.trace("Analize method declaration: "+method.getNameAsString());			
				HashMap<String, Integer> result = analyzeLocatorMethod(method);
				
				tempMethodLocatorMapping.put(method.getNameAsString(), result);
				
				if(method.toString().contains("By")) {
					tempMethodParamites.put(method.getNameAsString(), Pair.with("N",method.getParameters().size()));
					tempMethodSize.put(method.getNameAsString(), Pair.with("N",method.getBody().get().getStatements().size()));
				}
				else {
					tempMethodParamites.put(method.getNameAsString(),Pair.with("Y", method.getParameters().size()));
					tempMethodSize.put(method.getNameAsString(), Pair.with("Y",method.getBody().get().getStatements().size()));
				}
			}else if(declaration.isClassOrInterfaceDeclaration()){
				logger.trace("Skip classOrIterface declaretion: "+declaration.asClassOrInterfaceDeclaration().getNameAsString());		
			}else if (declaration.isConstructorDeclaration()) {
				logger.trace("Skip consctuctor declaretion: "+declaration.asConstructorDeclaration().getNameAsString());
			}else
				throw new IllegalArgumentException("Cannot analyze this class: "+ classToAnalyze.getClass());				
			}
		Triplet<
			HashMap<String, HashMap<String, Integer>>, 
			HashMap<String, Pair<String, Integer>>,
			HashMap<String, Pair<String, Integer>>> result = Triplet.with(tempMethodLocatorMapping,tempMethodSize,tempMethodParamites);
		return result;
	}
	
	private HashMap<String, Integer> analyzeLocatorMethod(MethodDeclaration method) {
		HashMap<String, Integer> tempLocatorCount = new HashMap<String, Integer>();
		//the By expression are MethodCallExpr
		List<MethodCallExpr> callExpList = method.findAll(MethodCallExpr.class);
		for (MethodCallExpr methodCallExpr : callExpList) {
			if(methodCallExpr.toString().startsWith("By") && !methodCallExpr.toString().contains("key")) {
				logger.debug("Find locator: "+ methodCallExpr.toString());
				if(!tempLocatorCount.containsKey(methodCallExpr.toString()))
					tempLocatorCount.put(methodCallExpr.toString(), 1);
				else
					tempLocatorCount.put(methodCallExpr.toString(), tempLocatorCount.get(methodCallExpr.toString())+1);
			}
		}
		
		return tempLocatorCount;
	}
	
	
}
