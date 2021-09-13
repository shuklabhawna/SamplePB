package priceBlender;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableSchedulerLock(defaultLockAtMostFor = "${scheduler.defaultLockAtMostFor}")
public class PriceBlenderApplication {

	public static void main(String[] args) {
		final Logger logger = LoggerFactory.getLogger(PriceBlenderApplication.class);
		
		try {
			SpringApplication.run(PriceBlenderApplication.class, args);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		
		
	}

}
