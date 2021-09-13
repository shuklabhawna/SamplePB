package priceBlender.dto;

import com.opencsv.bean.CsvBindByName;

import lombok.Getter;
import lombok.Setter;
import priceBlender.entity.MarketData;
import priceBlender.service.PriceBlender.MarketSource;

@Getter
@Setter
public class MarketDataDTO {

	@CsvBindByName(column = "marketDataSource", required = true)
    private String source;
	
	@CsvBindByName(column = "bid", required = true)
    private String bid;
	
	@CsvBindByName(column = "ask", required = true)
    private String ask;
	
	public MarketData getEntityObject(){
		MarketData marketData = new MarketData();
		marketData.setSource(MarketSource.valueOf(getSource()));
		marketData.setAsk(convertToDouble(getAsk()));
		marketData.setBid(convertToDouble(getBid()));
		return marketData;
	}
	
	private double convertToDouble(String value) {
		if(null == value) {
			return 0;
		}else {
			try {
				return Double.parseDouble(value);
			} catch (Exception e) {
				return 0;
			}
			
		}

	}
}
