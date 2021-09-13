package priceBlender.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import priceBlender.service.PriceBlender;
import javax.validation.constraints.NotNull;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@ToString
@Table(name="marketData")
public class MarketData {
	
	@Id
	@NotNull
	@Column(name="source")
	@Enumerated(EnumType.STRING)
	private PriceBlender.MarketSource source;
	
	@NotNull
	@Column(name="bid")
	private double bid;
	
	@NotNull
	@Column(name="ask")
	private double ask;
	
}
