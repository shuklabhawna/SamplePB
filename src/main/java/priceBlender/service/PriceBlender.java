package priceBlender.service;

import org.springframework.stereotype.Component;

@Component
public interface PriceBlender {

	double getBestBid();
	double getBestAsk();
	double getBestMid();
	void updatePrice(double bid, double ask,  MarketSource source);
	String getBestPrices();
	
	public enum MarketSource {
	    SOURCE_A, SOURCE_B, SOURCE_C
	}

}
