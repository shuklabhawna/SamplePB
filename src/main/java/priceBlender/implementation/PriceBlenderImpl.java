package priceBlender.implementation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import priceBlender.entity.MarketData;
import priceBlender.repository.MarketDataRepository;
import priceBlender.service.PriceBlender;

@Service
public class PriceBlenderImpl implements PriceBlender{
	
	private double bestBid =0;
	private double bestAsk =0;
	
	@Autowired
	MarketDataRepository marketDataRepository;
	
	@Override
	public double getBestBid() {
		MarketData data = marketDataRepository.findTopByBidGreaterThanOrderByBidDesc(0);
		return null!=data? data.getBid():0;
	}

	@Override
	public double getBestAsk() {
		MarketData data = marketDataRepository.findTopByAskGreaterThanOrderByAskAsc(0);
		return null!=data? data.getAsk():0;
	}

	@Override
	public double getBestMid() {
		if(this.bestBid>this.bestAsk) {
			return 0;
		}else {
		return ((BigDecimal.valueOf(this.bestBid)
		.add(BigDecimal.valueOf(this.bestAsk)))
		.divide(BigDecimal.valueOf(2d), 4, RoundingMode.UP).doubleValue());
		}
	}

	@Override
	public void updatePrice(double bid, double ask, MarketSource source) {
		MarketData entity = new MarketData(source,bid,ask);
		marketDataRepository.save(entity);
	}
	
	private Sort orderByIdSourceAsc() {
	    return new Sort(Sort.Direction.ASC, "source");
	}
	
	private List<MarketData> getLatestData() {
		return marketDataRepository.findAll(orderByIdSourceAsc());
	}

	@Override
	public String getBestPrices() {
		StringBuilder stringBuilder = new StringBuilder("\nBest Price based on latest data set");
		getLatestData().forEach( marketData -> {
			stringBuilder.append("\n"+marketData.toString());
		});
		
		this.bestBid = getBestBid();
		this.bestAsk = getBestAsk();
		
		stringBuilder.append("\n\tBest Bid ="+this.bestBid);
		stringBuilder.append("\t,Best Ask ="+this.bestAsk);
		stringBuilder.append("\t,Best Mid ="+getBestMid());
		return stringBuilder.toString();
	}
}
