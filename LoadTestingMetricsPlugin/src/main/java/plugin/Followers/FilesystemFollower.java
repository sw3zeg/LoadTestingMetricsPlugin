package plugin.Followers;

import plugin.Abstractions.Follower;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Paths;


public class FilesystemFollower implements Follower {

    private static final String OUTPUT_DIR = "/output";

    @Override
    public void SendData(InputStream file, String fileName) throws Exception {

        File outputFile = Paths.get(OUTPUT_DIR, fileName).toFile();

        try (FileOutputStream out = new FileOutputStream(outputFile)) {
            file.transferTo(out);
        }
    }
}
