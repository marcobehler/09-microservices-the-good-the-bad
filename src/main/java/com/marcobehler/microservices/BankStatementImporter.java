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
import java.util.ArrayList;
import java.util.List;


/**
 * Thanks for watching this episode! Send any feedback to info@marcobehler.com!
 */
public class BankStatementImporter {


    public void run(Path dir) {
        List<Path> xmlFiles = importFiles(dir);
        List<String> xmlAsStrings = readFiles(xmlFiles);
        validate(xmlAsStrings);
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

    public List<Result> validate(List<String> xmlAsStrings) {
        List<Result> results = new ArrayList<>();
        try (InputStream is = BankStatementImporter.class.getResourceAsStream("/schema.xsd")) {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new StreamSource(is));
            Validator validator = schema.newValidator();

            for (String each : xmlAsStrings) {
                StringReader reader = new StringReader(each);
                validator.validate(new StreamSource(reader));
                results.add(new Result(true, ""));
            }
        } catch (SAXException | IOException e) {
            e.printStackTrace();
            results.add(new Result(false, e.getMessage()));
        }
        return results;
    }

    public static class Result {
        private final Boolean valid;
        private final String message;

        public Result(Boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Result result = (Result) o;

            if (valid != null ? !valid.equals(result.valid) : result.valid != null) return false;
            return message != null ? message.equals(result.message) : result.message == null;
        }

        @Override
        public int hashCode() {
            int result = valid != null ? valid.hashCode() : 0;
            result = 31 * result + (message != null ? message.hashCode() : 0);
            return result;
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
