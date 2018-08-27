package com.example.validationservice;

import com.marcobehler.microservices.BankStatement;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Thanks for watching this episode! Send any feedback to info@marcobehler.com!
 */
@Component
public class ArchiveQueueListener {

    @JmsListener(destination = "archive.queue")
    public void receiveMessage(List<BankStatement> bankStatements) {
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
}
