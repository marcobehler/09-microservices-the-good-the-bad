package com.marcobehler.microservices;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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


    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("You must specify a directory paramm...");
            return;
        }

        String dir = args[0];
        new BankStatementImporter().run(Paths.get(dir));
    }

    public void run(Path dir) {
        System.out.println("Running bankstatement importer on dir[=" + dir.toAbsolutePath().normalize().toString() + "]");
        List<Path> xmlFiles = importFiles(dir);
        List<String> xmlAsStrings = readFiles(xmlFiles);

        List<BankStatement> bankStatements = validate(xmlAsStrings);
        saveToDatabase(bankStatements);
        forwardToAuditServer(bankStatements);

        System.out.println("Bank Statement IMporter run finished. Check the logs for errors.");
    }

    private void forwardToAuditServer(List<BankStatement> bankStatements) {
        System.out.println("Forwarding to Audit Server...");
        bankStatements
                .stream()
                .filter(BankStatement::getValid)
                .forEach(this::forwardToAuditServer);

        System.out.println("Done.");
    }

    public String forwardToAuditServer(BankStatement bankStatement) {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("application/xml"), bankStatement.getXml());
        Request request = new Request.Builder()
                .url("http://localhost:8999")
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("There was a problem forwarding [xml=" + bankStatement.getXml() + "]. Double check.");
            }
            return response.body().string();
        } catch (IOException e) {
            System.err.println("There was a problem with the http request [xml=" + bankStatement.getXml() + "]");
            return null;
        }
    }

    public void saveToDatabase(List<BankStatement> bankStatements) {
        System.out.println("Saving our bank statements to the database....");
        String insertStatement = "INSERT INTO bank_statements (xml, valid, message) VALUES (?, ?, ?)";

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

        System.out.println("Done.");
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
        return client.validate(xmlAsStrings); // delegating!
    }


    private ValidationClient client;

    public void setClient(ValidationClient client) {
        this.client = client;
    }

    public static class ValidationClient {
        public List<BankStatement> validate(List<String> xmlAsStrings) {
            try {
                ObjectMapper mapper = new ObjectMapper(); // from to json
                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(MediaType.parse("application/json"), mapper.writeValueAsString(xmlAsStrings));
                Request request = new Request.Builder().url("http://localhost:8080/validate").post(body).build();   // TODO make url configurable!
                Response response = client.newCall(request).execute(); // what happens if call returns 40x, 50x errors? find out in the next episodes!
                return mapper.readValue(response.body().string(), new TypeReference<List<BankStatement>>(){}); // take the http resonse and convert the json!
            } catch (IOException e) {
                throw new RuntimeException(e); // log, do something else
            }
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

        System.out.println("Found [" + result.size() + "] xml files");
        return result;
    }
}
