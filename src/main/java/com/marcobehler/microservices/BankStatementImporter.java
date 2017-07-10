package com.marcobehler.microservices;

import okhttp3.*;
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
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * Thanks for watching this episode! Send any feedback to info@marcobehler.com!
 */
public class BankStatementImporter {

    private OkHttpClient client = new OkHttpClient();

    // make configurable
    private String url = "http://localhost:8999";


    public void run(Path dir) {
        System.out.println("Starting with xml import in dir[=" + dir.toAbsolutePath().toString() + "]");

        List<Path> files = importFiles(dir);

        List<String> xmls = readFiles(files);

        List<Result> validationResults = validate(xmls);
        saveToDatabase(validationResults);

        postToServer(validationResults);

        System.out.println("....Done!");
    }

    private List<String> readFiles(List<Path> files) {
        List<String> xmls = new ArrayList<>();
        for (Path xmlFile : files) {
            try {
                xmls.add(new String(Files.readAllBytes(xmlFile), Charset.forName("UTF-8")));
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        return xmls;
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


    public static class Result {

        private boolean successful;
        private String message;

        public Result(boolean successful, String message) {
            this.successful = successful;
            this.message = message;
        }

        public boolean isSuccessful() {
            return successful;
        }
    }

    private List<Result> validate(List<String> xmls) {
        List<Result> results = new ArrayList<>();
        try (InputStream schemaIS = BankStatementImporter.class.getResourceAsStream("/schema.xsd")) {
            for (String xml : xmls) {
                StringReader reader = new StringReader(xml);

                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = schemaFactory.newSchema(new StreamSource(schemaIS));

                Validator validator = schema.newValidator();
                validator.validate(new StreamSource(reader));
                results.add(new Result(true, ""));
            }
        } catch (SAXException | IOException e) {
            e.printStackTrace();
            results.add(new Result(false, e.getMessage()));
        }
        return results;
    }

   /* public static void main(String[] args) throws Exception {
        Result result = new BankStatementImporter().validate(Paths.get("C:\\\\dev\\\\microservices\\\\src\\\\test\\\\resources\\\\test.xml"));

        boolean successful = result.isSuccessful();

        System.out.println(successful);

        //  new BankStatementImporter().validate("C:\\dev\\microservices\\src\\test\\resources\\test.xml", "C:\\dev\\microservices\\src\\test\\resources\\schema.xsd");
        //new AuditServer().start();
        //String s = new BankStatementImporter().postToServer("<xml>juhu</xml>");
        //System.out.println(s);
    }*/


    private void saveToDatabase(List<Result> validationResults) {

    }

    private void insertIntoDatabase(String xml, Result result) {
        try (java.sql.Connection conn = DriverManager.getConnection("jdbc:h2:mem:exercise_db;DB_CLOSE_DELAY=-1")) {
            conn.setAutoCommit(false);
            PreparedStatement statement = conn.prepareStatement("insert into bla values ? ?");
            statement.setString(1, "ble");
            statement.setString(2, "ble");
            statement.setString(3, "ble");
            statement.execute();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String postToServer(List<Result> results) {
        for (Result result : results) {
            RequestBody body = RequestBody.create(MediaType.parse("application/xml; charset=utf-8"), result.getXML());
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                response.isSuccessful();
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalStateException(e);
            }

        }
    }
}
