package com.marcobehler.microservices;

/**
 * Thanks for watching this episode! Send any feedback to info@marcobehler.com!
 */
public class BankStatement {
    private Boolean valid;
    private String errorMessage;
    private String xml;

    public BankStatement() {
        // easier for mapping frameworks, like jackson...
    }

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
