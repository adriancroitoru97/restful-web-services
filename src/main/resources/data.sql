INSERT INTO user_details(id, birth_date, name)
VALUES(10001, current_date(), 'Adrian');

INSERT INTO user_details(id, birth_date, name)
VALUES(10002, current_date(), 'Valeriu');

INSERT INTO user_details(id, birth_date, name)
VALUES(10003, current_date(), 'Croitoru');

INSERT INTO post(id, description, user_id)
VALUES(20001, 'I want to learn AWS', 10001);

INSERT INTO post(id, description, user_id)
VALUES(20002, 'I want to learn DevOPS', 10001);

INSERT INTO post(id, description, user_id)
VALUES(20003, 'I want to learn Spring', 10002);

INSERT INTO post(id, description, user_id)
VALUES(20004, 'I want to learn CLOUD', 10002);