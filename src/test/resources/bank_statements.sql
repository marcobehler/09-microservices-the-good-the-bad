CREATE TABLE bank_statements (
  id      INT AUTO_INCREMENT PRIMARY KEY,
  xml     CLOB,
  message VARCHAR,
  valid   BOOLEAN
)