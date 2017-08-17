import java.util.List;

import org.algo.finance.data.GoogleSymbol;
import org.algo.finance.data.GoogleSymbol.Data;

import com.hack17.hybo.domain.RiskTolerance;

public class Test {

	public static void main(String...args){
		GoogleSymbol gs = new GoogleSymbol("VTI");
 		List<Data> dataList = gs.getHistoricalPrices();
 		System.out.println("Hello");
 		}
}
