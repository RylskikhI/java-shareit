DELETE FROM COMMENTS;
DELETE FROM BOOKINGS;
DELETE FROM ITEMS;
DELETE FROM REQUESTS;
DELETE FROM USERS;

ALTER TABLE BOOKINGS ALTER COLUMN ID RESTART WITH 1;
ALTER TABLE ITEMS ALTER COLUMN ID RESTART WITH 1;
ALTER TABLE REQUESTS ALTER COLUMN ID RESTART WITH 1;
ALTER TABLE USERS ALTER COLUMN ID RESTART WITH 1;
ALTER TABLE COMMENTS ALTER COLUMN ID RESTART WITH 1;

