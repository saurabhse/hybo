package com.hackovation.hybo.Controllers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.websocket.server.PathParam;

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
import org.assertj.core.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.hackovation.hybo.ReadFile;
import com.hackovation.hybo.bean.dao.TestDaoInterface;
import com.hackovation.hybo.entities.PortfolioEntity;
import com.hackovation.hybo.services.PortfolioService;

@RestController
@RequestMapping(value="/rest/bl")
public class BlackLittermanController {
	
	@Autowired
	PortfolioService portfolioService;
	
	@RequestMapping(method=RequestMethod.GET,value="/getPortfolio")
	public @ResponseBody Map<String,PortfolioEntity> getPortfolio(@RequestParam(name="clientId") String clientId){
		return portfolioService.buildPortfolio(clientId);
		
		
	}
	

/*	BasicMatrix getCovarianceMatrixOld(){
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
	Double getLambda(){
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
	
    public static BigDecimal getRiskAversionFactor() {
        return BigDecimal.valueOf(3.07);
    }
	
	
	
	// Source: http://stattrek.com/matrix-algebra/covariance-matrix.aspx
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
		HashMap<CalendarDate, Double> aorMap = new HashMap<>();
		for(Data d:data)
			aorMap.put(d.getKey(), d.getValue());

		data = gs.getHistoricalPrices();
		HashMap<CalendarDate, Double> mdivMap = new HashMap<>();

		double[][] dataArray = {{4.0,2.0,0.60},
							{4.2,2.1,0.59},
							{3.9,2.0,0.58},
							{4.3,2.1,0.62},
							{4.1,2.2,0.63}};
		getCovarianceMatrix(dataArray);
	}
	
}
*/}
