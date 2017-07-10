package com.hackovation.hybo.Controllers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.algo.array.Array2D;
import org.algo.finance.FinanceUtils;
import org.algo.finance.data.GoogleSymbol;
import org.algo.finance.data.GoogleSymbol.Data;
import org.algo.finance.portfolio.BlackLittermanModel;
import org.algo.finance.portfolio.FinancePortfolio;
import org.algo.finance.portfolio.MarketEquilibrium;
import org.algo.matrix.BasicMatrix;
import org.algo.matrix.BigMatrix;
import org.algo.series.CalendarDateSeries;
import org.algo.type.CalendarDate;
import org.algo.type.CalendarDateUnit;

import com.hackovation.hybo.ReadFile;





public class BlackLittermanImpl {
	public static void main(String...args){
		
		// Step 1. Calculate Covariance Matrix
		BasicMatrix covarianceMatrix = getCovarianceMatrix();
		System.out.println("--------------Covarioance Matrix---------------------");
		System.out.println(covarianceMatrix);

		// Step 2. Calculate Lambda (Risk Aversion Factor)
		Double lambda = 2.5d;
		
		
		String[] tickers = getAssetsTickers();
//		double[][] marketWeight = {{0.25},{3},{0.25},{0.25}};
		double[][] marketWeight = getMarketWeight();
		BasicMatrix marketWeightMatrix =  BigMatrix.FACTORY.rows(marketWeight);
		MarketEquilibrium marketEquilibrium = new MarketEquilibrium(tickers, covarianceMatrix, lambda);
		BlackLittermanModel bl  = new BlackLittermanModel(marketEquilibrium, marketWeightMatrix);
		List<BigDecimal> weights = new ArrayList<>();
		weights.add(new BigDecimal(30));
		weights.add(new BigDecimal(20));
		weights.add(new BigDecimal(25));
		weights.add(new BigDecimal(25));
		bl.addViewWithBalancedConfidence(weights, 0.26);
		
/*		System.out.println("--------------Asset Return Matrix---------------------");
		System.out.println(bl.getAssetReturns());
*/		System.out.println("--------------Asset Weights---------------------");
		System.out.println(bl.getAssetWeights());
		
		System.out.println("Till Here");
		
	}
	
	static double[][] getMarketWeight(){
		double[][] marketWeight = new double[4][1];
		double[] totalValue = new double[4]; 
		try{
			ArrayList<String> files = new ArrayList<>(4);
			files.add("D:\\Wkspace\\Hacovation\\CRSP US Total Market_IndividualMarketValue.txt");
			files.add("D:\\Wkspace\\Hacovation\\CRSP US Large Cap Value_IndividualMarketValue.txt");
			files.add("D:\\Wkspace\\Hacovation\\CRSP US MID CAP VALUE_IndividualMarketValue.txt");
			files.add("D:\\Wkspace\\Hacovation\\CRSP US SMALL CAP VALUE_IndividualMarketValue.txt");
			int i = 0;
			for(String fileName:files){
				BufferedReader f = new BufferedReader(new FileReader(fileName));
				String ln=null;
				while((ln=f.readLine())!=null){
					totalValue[i] +=Double.valueOf(ln.substring(ln.lastIndexOf("#")+1));
				}
				i++;
				f.close();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		double total=0;
		for(double d:totalValue)
			total+=d;
		marketWeight[0][0]=totalValue[0]/total;
		marketWeight[1][0]=totalValue[1]/total;
		marketWeight[2][0]=totalValue[2]/total;
		marketWeight[3][0]=totalValue[3]/total;
		return marketWeight;
	}
	static BasicMatrix getCovarianceMatrix(){
		Collection<CalendarDateSeries<Double>> col = new ArrayList<>();
		ReadFile readFile = new ReadFile();
		col.add(readFile.getCalendarDataSeries("D:\\Wkspace\\Hacovation\\CRSP US Total Market.txt","A"));
		col.add(readFile.getCalendarDataSeries("D:\\Wkspace\\Hacovation\\CRSP US Large Cap Value.txt","B"));
		col.add(readFile.getCalendarDataSeries("D:\\Wkspace\\Hacovation\\CRSP US MID CAP VALUE.txt","C"));
		col.add(readFile.getCalendarDataSeries("D:\\Wkspace\\Hacovation\\CRSP US SMALL CAP VALUE.txt","D"));
		BasicMatrix covarianceMatrix = FinanceUtils.makeCovarianceMatrix(col);
		return covarianceMatrix;
	}
	static BasicMatrix getCovarianceMatrixOld(){
 		GoogleSymbol gs = new GoogleSymbol("AOR");
		Collection<CalendarDateSeries<Double>> col = new ArrayList<>();
		col.add(gs.getPriceSeries());
		gs = new GoogleSymbol("MDIV");
		col.add(gs.getPriceSeries());
		gs = new GoogleSymbol("AOM");
		col.add(gs.getPriceSeries());
		gs = new GoogleSymbol("PCEF");
		col.add(gs.getPriceSeries());
		gs = new GoogleSymbol("AOA");
		col.add(gs.getPriceSeries());
		BasicMatrix covarianceMatrix = FinanceUtils.makeCovarianceMatrix(col);
		return covarianceMatrix;
	}
	
	//Reference : https://www.fool.com/knowledge-center/how-to-calculate-the-historical-variance-of-stock.aspx
	static Double getLambda(){
 		GoogleSymbol referenceData = new GoogleSymbol("OSPTX",CalendarDateUnit.DAY);
		List<Data> historicalPriceReference = referenceData.getHistoricalPrices();
		ArrayList<Double> historicalValuesRefernce = new ArrayList<>();
		for(Data d:historicalPriceReference)
			historicalValuesRefernce.add(d.getPrice());
		ArrayList<Double> logReturnOfMarketPortfolio = new ArrayList<>();
		ArrayList<Double> returnOfMarketPortfolio = new ArrayList<>();

		int size=historicalPriceReference.size();
		for(int i=0;i<size-1;i++){
			returnOfMarketPortfolio.add((historicalValuesRefernce.get(i+1)/historicalValuesRefernce.get(i))*100);
			logReturnOfMarketPortfolio.add(Math.log((historicalValuesRefernce.get(i+1)/historicalValuesRefernce.get(i))*100));
		}
		Double sumOfLogReturns = 0.0;
		for(Double d:logReturnOfMarketPortfolio){
			sumOfLogReturns+=d;
		}
		//1.1
		Double Er = sumOfLogReturns/logReturnOfMarketPortfolio.size();
		
		Double sumOfReturns=0.0;
		for(Double d:returnOfMarketPortfolio)
			sumOfReturns+=d;
		Double averageOfReturns = sumOfReturns/returnOfMarketPortfolio.size();
		ArrayList<Double> differenceBetweenReturnAndAverage = new ArrayList<>();
		for(Double d:returnOfMarketPortfolio){
			differenceBetweenReturnAndAverage.add(Math.pow(d-averageOfReturns,2));
		}
		Double sumOfSquaredDifference=0.0;
		for(Double d:differenceBetweenReturnAndAverage){
			sumOfSquaredDifference+=d;
		}
		//1.2
		Double varianceOfMarketPortfolio  = sumOfSquaredDifference/(Double.valueOf(differenceBetweenReturnAndAverage.size()));
		
		//1.3
		Double Rf = 2.0; // This value need to be retrieved from USA Government
		
		Double lambda = (Er-Rf)/varianceOfMarketPortfolio;
		System.out.println("Returns of Market Portfolio : "+Er);
		System.out.println("Variance of Market Portfolio : "+varianceOfMarketPortfolio);
		System.out.println("Lambda : "+lambda);
		return lambda;
	}
	
/*    public static BigDecimal getRiskAversionFactor() {
        return BigDecimal.valueOf(3.07);
    }
*/	
	public static String[] getAssetsTickers(){
		String[] tickers = new String[5];
		tickers[0] = "A";
		tickers[1] = "B";
		tickers[2] = "C";
		tickers[3] = "D";
		return tickers;
	}
	public static String[] getAssetsTickersOld(){
		String[] tickers = new String[5];
		tickers[0] = "AOR";
		tickers[1] = "MDIV";
		tickers[2] = "AOM";
		tickers[3] = "PCEF";
		tickers[4] = "AOA";
		return tickers;
	}
	
/*	// Source: http://stattrek.com/matrix-algebra/covariance-matrix.aspx
	public static void getCovarianceMatrixOld(double[][] dataArray){
		int rows = dataArray.length;
		// Step 1: Deviation Score
		int n=dataArray.length;
		double[][] sparseMatrix = getSparseMatrix(rows);
		double[][] multipliedMatrix = multiplyMatrix(sparseMatrix,dataArray,true,n);
		double[][] a = subtractMatrix(dataArray,multipliedMatrix);
		double[][] a_dash = inverseMatrix(a);
		double[][] V = multiplyMatrix(a_dash, a, true, n);
		printMatrix(V);
		System.out.println("Aman");
		System.out.println(multipliedMatrix);
		
	}
	
	public static double[][] getSparseMatrix(int n){
		double[][] matrix = new double[n][n];
		for(int i=0;i<n;i++){
			for(int j=0;j<n;j++){
				matrix[i][j]=1;
			}
		}
		return matrix;
	}
	public static double[][] multiplyMatrix(double[][] left,double[][] right,boolean divide,int div){
		int m = left.length;
		int n=right[0].length;
		int p=right.length;
		double[][] sol = new double[m][n];
		for(int i=0;i<m;i++){
			for(int j=0;j<n;j++){
				double sum=0;
				for(int k=0;k<p;k++){
					 sum = sum + left[i][k]*right[k][j];
				}
				if(divide)
					sol[i][j]=sum/div;
				else
					sol[i][j]=sum;
			}
		}
		return sol;
	}
	public static double[][] subtractMatrix(double[][] first,double[][] second){
		int m=first.length;
		int n=first[0].length;
		for(int i=0;i<m;i++){
			for(int j=0;j<n;j++){
				first[i][j]=first[i][j]-second[i][j];
			}
		}
		return first;
	}
	public static double[][] inverseMatrix(double[][] data){
		int m=data.length;
		int n=data[0].length;
		double[][] sol = new double[n][m];
		for(int i=0;i<m;i++){
			for(int j=0;j<n;j++){
				sol[j][i]=data[i][j];
			}
		}
		return sol;
	}
	
	public static void printMatrix(double[][] dataArray){
		int m=dataArray.length;
		int n=dataArray[0].length;
		for(int i=0;i<m;i++){
			for(int j=0;j<n;j++){
				System.out.print(dataArray[i][j]+",");;
			}
			System.out.print("\n");
		}
	}
*/	
	public static void deprecated(){
		String[] tickers = getAssetsTickersOld();
		
	// Step 1. Calculate Covariance Matrix
		BasicMatrix covarianceMatrix = getCovarianceMatrix();
		
		// Step 2. Calculate Lambda (Risk Aversion Factor)
		Double lambda = getLambda();
		
		
		double[][] marketWeight = {{0.25},{0.25},{0.25},{0.25}};
		BasicMatrix marketWeightMatrix =  BigMatrix.FACTORY.rows(marketWeight);
		MarketEquilibrium marketEquilibrium = new MarketEquilibrium(tickers, covarianceMatrix, lambda);
		BlackLittermanModel bl  = new BlackLittermanModel(marketEquilibrium, marketWeightMatrix);
		
		System.out.println("Assets Returns Matrix (Prior)"+bl.getAssetReturns());
		
		System.out.println("Till Here");
		
		
		
		
		
		
//		List<Data> data = gs.getHistoricalPrices();
/*		HashMap<CalendarDate, Double> aorMap = new HashMap<>();
		for(Data d:data)
			aorMap.put(d.getKey(), d.getValue());
*/
/*		data = gs.getHistoricalPrices();
		HashMap<CalendarDate, Double> mdivMap = new HashMap<>();
*/
/*		double[][] dataArray = {{4.0,2.0,0.60},
							{4.2,2.1,0.59},
							{3.9,2.0,0.58},
							{4.3,2.1,0.62},
							{4.1,2.2,0.63}};
		getCovarianceMatrix(dataArray);
	}
	
*/}
}
