import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BankStatementImporter {

    public List<Path> importFiles(Path directory) {
        List<Path> result = new ArrayList<>();

        try (DirectoryStream<Path> paths = Files.newDirectoryStream(directory, "*.xml")) {
            for (Path each : paths) {
                result.add(each);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
