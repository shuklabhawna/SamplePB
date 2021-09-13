package priceBlender;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;

@Configuration
public class SchedulerConfig {
	@Bean
	public LockProvider lockProvider(DataSource dataSource) {
		final Logger logger = LoggerFactory.getLogger(SchedulerConfig.class);
		try {

			logger.debug(dataSource.getConnection().getMetaData().getURL());
			return new JdbcTemplateLockProvider(JdbcTemplateLockProvider.Configuration.builder()
					.withJdbcTemplate(new JdbcTemplate(dataSource)).usingDbTime().build());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			return null;
		}
	}
}
