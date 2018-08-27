package com.marcobehler.microservices;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    public void successfully_validates_correct_xml_files() throws Exception {

        // 1. make sure validation-service is ALWAYS running/deployed...fixed url..
        // 2. make sure to boot up server specifically: custom gradle scripts...
        // some spring boot test annotations -> WORK
        // 3. mocking the validation-service

        String xmlString = "<transaction id=\"a89123ndsf732nf\">\n" +
                "    <card_number>42424224242424</card_number>\n" +
                "    <transaction_time>1495286440</transaction_time>\n" +
                "    <amount>-1,299.00</amount>\n" +
                "    <currency>EUR</currency>\n" +
                "    <reference>Apple.de Fancy Macbook Pro</reference>\n" +
                "</transaction>";
        Path xml = dir.resolve("yes.xml");
        Files.write(xml, xmlString.getBytes());

        BankStatementImporter bankStatementImporter = new BankStatementImporter();
        BankStatementImporter.ValidationClient mock = mock(BankStatementImporter.ValidationClient.class);

        // duplicate the internal behavior?? how complex is the rest service?? mocking hell???
        when(mock.validate(Arrays.asList(xmlString))).thenReturn(Arrays.asList(new BankStatement(true, "", xmlString)));
        bankStatementImporter.setClient(mock);

        List<BankStatement> bankStatements = bankStatementImporter
                .validate(Arrays.asList(xmlString));
        assertThat(bankStatements).hasSize(1);
        assertThat(bankStatements)
                .containsExactly(new BankStatement(true, "", xmlString));
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

    @Test
    public void successfully_reads_xml_files() throws Exception {
        String xmlString = "<xml></xml>";
        Path xml = dir.resolve("yes.xml");
        Files.write(xml, xmlString.getBytes());

        List<String> strings = new BankStatementImporter().readFiles(Arrays.asList(xml));
        assertThat(strings).hasSize(1);
        assertThat(strings).containsExactly(xmlString);
    }





    @Test
    @Ignore
    public void forwardTest_ok() throws Exception {
        String result = new BankStatementImporter()
                .forwardToAuditServer(new BankStatement(false, "", "<xml></xml>"));
        assertThat(result).contains("OK");

        // TODO for you: tests if the call fails. i.e. server unavailable or similar
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }
}
