import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.marcobehler.microservices.BankStatementImporter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Thanks for watching this episode! Send any feedback to info@marcobehler.com!
 */
public class BankStatementImporterTest {

    private FileSystem fileSystem;
    private Path dir;

    @Before
    public void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        dir = fileSystem.getPath("/someDir");
        Files.createDirectory(dir);
    }

    @Test
    public void our_dir_exists() throws Exception {
        assertTrue(Files.exists(dir));
    }

    @Test
    public void successfully_handles_empty_directory() throws Exception {
        List<Path> paths = new BankStatementImporter().importFiles(dir);
        assertThat(paths).hasSize(0);
    }

    @Test
    public void successfully_handles_xml_files() throws Exception {
        Path xml = dir.resolve("test.xml");
        Files.createFile(xml);

        List<Path> paths = new BankStatementImporter().importFiles(dir);
        assertThat(paths).hasSize(1);
        assertThat(paths).containsExactly(xml);
    }

    @Test
    public void successfully_handles_only_xml_files_ignores_rest() throws Exception {
        Path xml = dir.resolve("test.xml");
        Files.createFile(xml);
        Files.createFile(dir.resolve("donaltrump.jpg"));

        List<Path> paths = new BankStatementImporter().importFiles(dir);
        assertThat(paths).hasSize(1);
        assertThat(paths).containsExactly(xml);
    }



    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }
}
