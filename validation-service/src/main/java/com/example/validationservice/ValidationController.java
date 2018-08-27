package com.example.validationservice;

import com.marcobehler.microservices.BankStatement;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Thanks for watching this episode! Send any feedback to info@marcobehler.com!
 */
@RestController
public class ValidationController {

    @PostMapping(value = "/validate")
    public List<BankStatement> validate(@RequestBody List<String> xmlAsString) {

        System.out.println("Validating files....");
        List<BankStatement> bankStatements = new ArrayList<>();

        String lastProcessedXml = null;
        try (InputStream is = ValidationController.class.getResourceAsStream("/schema.xsd")) {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new StreamSource(is));
            Validator validator = schema.newValidator();

            for (String each : xmlAsString) {
                lastProcessedXml = each;
                StringReader reader = new StringReader(each);
                try {
                    validator.validate(new StreamSource(reader));
                    bankStatements.add(new BankStatement(true, "", lastProcessedXml));
                } catch (SAXException | IOException e) {
                    e.printStackTrace();
                    bankStatements.add(new BankStatement(false, e.getMessage(), lastProcessedXml));
                }
            }
        } catch (SAXException | IOException e) {
            e.printStackTrace();
            bankStatements.add(new BankStatement(false, e.getMessage(), lastProcessedXml));
        }
        System.out.println("Done.");
        return bankStatements;
    }

}
