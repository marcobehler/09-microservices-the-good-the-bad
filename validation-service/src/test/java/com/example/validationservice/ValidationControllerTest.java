package com.example.validationservice;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.marcobehler.microservices.BankStatement;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Thanks for watching this episode! Send any feedback to info@marcobehler.com!
 */
public class ValidationControllerTest {

    private FileSystem fileSystem;
    private Path dir;

    @Before
    public void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        dir = fileSystem.getPath("/someDir");
        Files.createDirectory(dir);
    }

    @Test
    public void successfully_validates_correct_xml_files() throws Exception {
        String xmlString = "<transaction id=\"a89123ndsf732nf\">\n" +
                "    <card_number>42424224242424</card_number>\n" +
                "    <transaction_time>1495286440</transaction_time>\n" +
                "    <amount>-1,299.00</amount>\n" +
                "    <currency>EUR</currency>\n" +
                "    <reference>Apple.de Fancy Macbook Pro</reference>\n" +
                "</transaction>";
        Path xml = dir.resolve("yes.xml");
        Files.write(xml, xmlString.getBytes());

        List<BankStatement> bankStatements = new ValidationController().validate(Arrays.asList(xmlString));
        assertThat(bankStatements).hasSize(1);
        assertThat(bankStatements).containsExactly(new BankStatement(true, "", xmlString));
    }


}