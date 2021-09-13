package priceBlender.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import priceBlender.entity.MarketData;


@Repository
public interface MarketDataRepository extends JpaRepository<MarketData, String>{

	MarketData findTopByBidGreaterThanOrderByBidDesc(double bid);
	MarketData findTopByAskGreaterThanOrderByAskAsc(double ask);

}
