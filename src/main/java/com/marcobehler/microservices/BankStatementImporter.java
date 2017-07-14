package com.marcobehler.microservices;

import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * Thanks for watching this episode! Send any feedback to info@marcobehler.com!
 */
public class BankStatementImporter {


    public void run(Path dir) {
        List<Path> xmlFiles = importFiles(dir);
        List<String> xmlAsStrings = readFiles(xmlFiles);

        List<BankStatement> bankStatements = validate(xmlAsStrings);
        saveToDatabase(bankStatements);
    }

    public void saveToDatabase(List<BankStatement> bankStatements) {
        String insertStatement = "insert into bank_statements (xml, valid, message) values (?, ?, ?)";

        try (Connection con = DriverManager.getConnection("jdbc:h2:~/lulu")) {
            con.setAutoCommit(false);
            PreparedStatement preparedStatement = con.prepareStatement(insertStatement);

            for (BankStatement each : bankStatements) {
                preparedStatement.setString(1, each.getXml());
                preparedStatement.setBoolean(2, each.getValid());
                preparedStatement.setString(3, each.getErrorMessage());
                preparedStatement.executeUpdate();
            }
            con.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public List<String> readFiles(List<Path> xmlFiles) {
        List<String> result = new ArrayList<>();
        for (Path each : xmlFiles) {
            try {
                result.add(new String(Files.readAllBytes(each), Charset.forName("UTF-8")));
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        return result;
    }

    public List<BankStatement> validate(List<String> xmlAsStrings) {
        List<BankStatement> bankStatements = new ArrayList<>();

        String lastProcessedXml = null;
        try (InputStream is = BankStatementImporter.class.getResourceAsStream("/schema.xsd")) {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new StreamSource(is));
            Validator validator = schema.newValidator();

            for (String each : xmlAsStrings) {
                lastProcessedXml = each;
                StringReader reader = new StringReader(each);
                validator.validate(new StreamSource(reader));
                bankStatements.add(new BankStatement(true, "", lastProcessedXml));
            }
        } catch (SAXException | IOException e) {
            e.printStackTrace();
            bankStatements.add(new BankStatement(false, e.getMessage(), lastProcessedXml));
        }
        return bankStatements;
    }

    public static class BankStatement {
        private final Boolean valid;
        private final String errorMessage;
        private final String xml;

        public BankStatement(Boolean valid, String errorMessage, String xml) {
            this.valid = valid;
            this.errorMessage = errorMessage;
            this.xml = xml;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BankStatement bankStatement = (BankStatement) o;

            if (valid != null ? !valid.equals(bankStatement.valid) : bankStatement.valid != null) return false;
            return errorMessage != null ? errorMessage.equals(bankStatement.errorMessage) : bankStatement.errorMessage == null;
        }

        @Override
        public int hashCode() {
            int result = valid != null ? valid.hashCode() : 0;
            result = 31 * result + (errorMessage != null ? errorMessage.hashCode() : 0);
            return result;
        }

        public Boolean getValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public String getXml() {
            return xml;
        }
    }

    public List<Path> importFiles(Path dir) {
        List<Path> result = new ArrayList<>();

        try (DirectoryStream<Path> paths = Files.newDirectoryStream(dir, "*.xml")) {
            for (Path path : paths) {
                result.add(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
