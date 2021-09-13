package priceBlender.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import priceBlender.dto.MarketDataDTO;
import priceBlender.entity.MarketData;
import priceBlender.repository.MarketDataRepository;
import priceBlender.service.PriceBlender;
import priceBlender.util.FileComparator;

@Component
public class MarketDataProcessor {

	final Logger logger = LoggerFactory.getLogger(MarketDataProcessor.class);
	final Logger dataLogger = LoggerFactory.getLogger("priceBlender.dataLogger");
	
	@Value("${marketdata.file.input.location}")
	String fileInputLocation;

	@Value("${marketdata.file.processed.location}")
	String fileProcessedLocation;
	
	@Value("${marketdata.file.exception.location}")
	String fileExceptionLocation;
	
	@Value("${marketdata.file.reader.skip.row:0}")
	int rowsToSkip;

	@Autowired
	PriceBlender priceBlender;
	
	@Autowired 
	FileComparator fileComparator;
	
	/*
	 * @Autowired ProcessingService processingService;
	 */
	
	@Autowired
	MarketDataRepository marketDataRepository;
	
	@Value("${batch.job.timeout.interval}")
	private long fileProcessingJobTimeout;

	@PostConstruct
	@Scheduled(cron = "${marketdata.file.read.cron.expression}")
	@SchedulerLock(name = "MarketDataProcessor_processMessage", lockAtLeastFor = "${marketdata.file.read.lockAtLeastFor}", lockAtMostFor = "${marketdata.file.read.lockAtMostFor}")
	public void processMessage() {

		logger.info("Starting process ");
		final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
		/**
		 * This is a scheduler that will run based on cron expression. 
		 * It will read the text files from fileInputLocation and process them.
		 * Each file will acquire read write lock before start processing and will be released once done.   
		 */
		try {
			List<File> files = Files.list(Paths.get(fileInputLocation))
					.sorted(fileComparator)
					.filter(Files::isRegularFile)
					.filter(path -> path.toString().endsWith(".txt"))
					.map(Path::toFile)
					.collect(Collectors.toList());
			
			if (!files.isEmpty()) {
				files.forEach(t -> {
					try {
						readWriteLock.readLock().lock();
					    processFile(t);
					} catch (IOException e) {
						logger.error("Error While processing " + t.getName());
						e.printStackTrace();
					}
					finally {
				        readWriteLock.readLock().unlock();
				    }
					
				});
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void processFile(File file) throws IOException {
		
		List<MarketDataDTO> records = getData(file);
		/**
		 * Each record will invoke updatePrice from priceBlender service.
		 * After updating the price it will show the latest snapshot from database and the best bid ask and mid based on latest data.
		 * 
		 */
		 
		if (!records.isEmpty()) {
			AtomicInteger recordsFound = new AtomicInteger(0);
			AtomicInteger processedRecords = new AtomicInteger(0);;
			AtomicInteger failedRecords = new AtomicInteger(0);;
			
			records.forEach(marketDataDto -> {
				try{
					MarketData  marketData = marketDataDto.getEntityObject();
					if ((marketData.getAsk() > 0) && (marketData.getBid() > marketData.getAsk())) {
						logger.error("Invalid feed [Bid>Ask]" + marketData.toString());
						failedRecords.getAndIncrement();
					}else if((marketData.getAsk() == 0) && (marketData.getBid() == 0)) {
						logger.error("Invalid feed [Bid and Ask missing]" + marketData.toString());
						failedRecords.getAndIncrement();
					} else {
						priceBlender.updatePrice(marketData.getBid(), marketData.getAsk(), marketData.getSource());
						processedRecords.getAndIncrement();
					}
				}catch (Exception e) {
					e.printStackTrace();
					logger.error("Error while processing record ["+e.getMessage()+"]" + marketDataDto.toString());
				}
				
				
				
			});
			logger.info("Total Records were ["+recordsFound.intValue()+"] , "
					+ "Processed ["+processedRecords.intValue()+"] , "
							+ "failed ["+failedRecords.intValue()+"]");
			if(processedRecords.intValue() > 0) {
				dataLogger.info("_________After updating data from file["+file.getName()+"]___________");
				dataLogger.info("\n"+priceBlender.getBestPrices());
				dataLogger.info("********************************************************\n");
			}else {
				logger.info("processedRecords > 0 "+(processedRecords.intValue() > 0));
			}
		}else {
			logger.error("No records in file "+file.getName());
		}
		
		

	}

	private List<MarketDataDTO> getData(File file) throws IOException, FileNotFoundException {
		logger.info(Thread.currentThread().getName()+" ->  Reading from file ->"+file.getName());
		String fileSuffix =  "/" + file.getName() + "_" + new Date().getTime();
		String newFileName=null;
		
		/**
		 * Read the file and prepare list of market data object from each row.
		 * After completing a file it will be moved to processed folder along with timestamp to avoid duplicate processing. 
		 */
		List<MarketDataDTO> records = new ArrayList<>();
		
		try(Reader reader = new BufferedReader(new FileReader(file)))  {
			CsvToBean<MarketDataDTO> csvToBean = new CsvToBeanBuilder<MarketDataDTO>(reader)
					.withType(MarketDataDTO.class)
					.withIgnoreLeadingWhiteSpace(true)
					.build();
			records = csvToBean.parse();
			newFileName = fileProcessedLocation+fileSuffix;
			logger.info("File renamed as "+newFileName);
			return records;
		} catch (Exception e) {
			e.printStackTrace();
			String errorMsg = "Error while reading file ["+file.getName()+"] "+e.getMessage();
			if(e.getCause() instanceof CsvRequiredFieldEmptyException) {
				errorMsg = errorMsg+"\n"+e.getCause().getMessage();
			}
			logger.error(errorMsg);
			newFileName = fileExceptionLocation+fileSuffix;
			logger.info("File renamed as "+newFileName);
			throw e;
		}finally {
			logger.info("Deleting file "+file.getAbsolutePath());
			file.renameTo(new File(newFileName));
			file.delete();
			file=null;
		}	
		
	}

}
