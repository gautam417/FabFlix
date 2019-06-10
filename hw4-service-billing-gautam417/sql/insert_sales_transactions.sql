DELIMITER //
CREATE PROCEDURE insert_sales_transactions (IN email VARCHAR(50), IN movieId VARCHAR(10), quantity INT, saleDate DATE, token VARCHAR(50))
BEGIN
START TRANSACTION;
INSERT INTO sales (email, movieId, quantity, saleDate)
  VALUES(email, movieId, quantity, saleDate);
INSERT INTO transactions (sId, token)
  VALUES(LAST_INSERT_ID(), token);
COMMIT;
END//
DELIMITER;