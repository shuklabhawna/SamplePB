package priceBlender.service;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public interface ProcessingService {
	public static <T> void executeThreads (long requestTimeout, List<Callable<T>> taskList) {
		ExecutorService executor = Executors.newFixedThreadPool(taskList.size());
		try {
			executor.invokeAll(taskList);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} finally {
			executor.shutdown();
			try {
				if (!executor.awaitTermination(requestTimeout, TimeUnit.SECONDS))
					executor.shutdownNow();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				executor.shutdownNow();
			}
			executor = null;
		}
	}
	
	public class ExecutorThreadInvoker<T> implements Callable<T> {
		@Override
		public T call () {
			return null;
		}
	}
}
