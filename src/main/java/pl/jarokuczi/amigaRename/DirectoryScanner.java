package pl.jarokuczi.amigaRename;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

import static java.lang.String.format;

/**
 * Created by jarokuczi on 2017-07-24.
 */
@Component
public class DirectoryScanner {
    private static final Logger logger = Logger.getLogger(DirectoryScanner.class);

    @Value("${sourceDirectory}")
    private String sourceDirectory;

    @Value("${targetDirectory}")
    private String targetDirectory;

    @Value("${maxFileNameLength}")
    private Integer maxFileNameLength;

    public void start() throws Exception {
        final int[] counter = {1};
        Comparator<Path> byFileName = Comparator.comparing(e1 -> e1.getFileName().toString().toLowerCase());
        try (Stream<Path> paths = Files.walk(Paths.get(sourceDirectory))) {
            paths

                    .filter(Files::isRegularFile)
                    .sorted(byFileName)
                    .forEach(path -> {
                        counter[0]++;
                        String filePath = path.getParent().toString().replace(File.separator, "/");
                        String fileName = path.getFileName().toString();
                        try {
                            copyFileToTarget(filePath, fileName);
                        } catch (IOException e) {
                            logger.error(format("Error during processing file %s/%s cause: %s", filePath, fileName, e.getMessage()));
                        }
                    });
        }
        logger.info(format("processed %d files.", counter[0]));
    }

    private void copyFileToTarget(String filePath, String fileName) throws IOException {
        String newPath = filePath.replace(sourceDirectory, targetDirectory);
        File targetPath = new File(newPath);
        attempToCreateTargetDirectory(targetPath);
        String newName = generateNewName(newPath, fileName);
        logger.info(format("%s => %s", fileName, newName));
        StreamUtils.copy(new FileInputStream(format("%s/%s", filePath, fileName)), new FileOutputStream(newName));
    }

    private void attempToCreateTargetDirectory(File targetPath) {
        if (!targetPath.exists()) {
            targetPath.mkdirs();
        }
    }

    private String generateNewName(String newPath, String fileName) {
        int counter = 0;
        fileName = fileName.replace(" ", "").replace("_", "").replace("Disk", "");
        //.replace("(.+)(Disk)(\\d)\\.(.+)", "$10$3.$4")
        if (fileName.length() >= maxFileNameLength + 4) {
            String fileNameBase = fileName.substring(0, maxFileNameLength - 1);
            String[] splited = fileName.split("\\.");
            File newFile;
            String newFileName;

            do {
                newFileName = format("%s/%s_%d.%s", newPath, fileNameBase, counter++, splited[splited.length - 1]);
                newFile = new File(newFileName);
            } while (newFile.exists());

            return newFileName;
        }
        return newPath + "/" + fileName;
    }
}
