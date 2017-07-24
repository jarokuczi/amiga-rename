package pl.jarokuczi.amigaRename;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class AmigaRenameApplication {

	@Autowired
	private DirectoryScanner directoryScanner;

	public static void main(String[] args) {
		SpringApplication.run(AmigaRenameApplication.class, args);
	}

	@PostConstruct
	public void init() throws Exception {
		directoryScanner.start();
	}
}
