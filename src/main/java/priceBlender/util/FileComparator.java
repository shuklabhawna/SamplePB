package priceBlender.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;

import org.springframework.stereotype.Component;

@Component
public class FileComparator implements Comparator<Path>{

	@Override
	public int compare(Path p1, Path p2) {
		        try {
					return (int) (Files.readAttributes(p1, BasicFileAttributes.class).lastModifiedTime().toMillis() -
					            Files.readAttributes(p2, BasicFileAttributes.class).lastModifiedTime().toMillis());
				} catch (IOException e) {
					e.printStackTrace();
				}
				return 0;
            };
            
       

}
