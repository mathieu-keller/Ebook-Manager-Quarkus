CREATE SEQUENCE HIBERNATE_SEQUENCE START 1;

CREATE TABLE BOOK (
    ID    BIGINT PRIMARY KEY,
    DATE  VARCHAR(4000),
    META  VARCHAR(4000),
    PATH  VARCHAR(4000),
    COLLECTION_ID  BIGINT,
    GROUP_POSITION BIGINT
);

CREATE TABLE CREATOR (
    ID   BIGINT PRIMARY KEY,
    NAME VARCHAR(4000)
);


CREATE TABLE TITLE (
    ID          BIGINT PRIMARY KEY,
    TITLE       VARCHAR(4000),
    TITLE_TYPE  VARCHAR(4000),
    TITLE_ORDER BIGINT,
    BOOK_ID     BIGINT,
    FOREIGN KEY (BOOK_ID) REFERENCES BOOK(ID)
);


CREATE TABLE CREATOR2BOOK (
    BOOK_ID    BIGINT,
    CREATOR_ID BIGINT,
    FOREIGN KEY (BOOK_ID) REFERENCES BOOK(ID),
    FOREIGN KEY (CREATOR_ID) REFERENCES CREATOR(ID)
);

CREATE TABLE CONTRIBUTOR (
    ID   BIGINT PRIMARY KEY,
    NAME VARCHAR(4000)
);

CREATE TABLE CONTRIBUTOR2BOOK (
    BOOK_ID        BIGINT,
    CONTRIBUTOR_ID BIGINT,
    FOREIGN KEY (BOOK_ID) REFERENCES BOOK(ID),
    FOREIGN KEY (CONTRIBUTOR_ID) REFERENCES CONTRIBUTOR(ID)
);

CREATE TABLE PUBLISHER (
    ID   BIGINT PRIMARY KEY,
    NAME VARCHAR(4000)
);

CREATE TABLE PUBLISHER2BOOK (
    BOOK_ID      BIGINT,
    PUBLISHER_ID BIGINT,
    FOREIGN KEY (BOOK_ID) REFERENCES BOOK(ID),
    FOREIGN KEY (PUBLISHER_ID) REFERENCES PUBLISHER(ID)
);

CREATE TABLE IDENTIFIER (
    ID       BIGINT PRIMARY KEY,
    BOOK_ID  BIGINT,
    VALUE    VARCHAR(4000),
    IDENT_ID VARCHAR(4000),
    FOREIGN KEY (BOOK_ID) REFERENCES BOOK(ID)
);

CREATE TABLE SUBJECT (
    ID   BIGINT PRIMARY KEY,
    NAME VARCHAR(4000)
);

CREATE TABLE SUBJECT2BOOK (
    BOOK_ID    BIGINT,
    SUBJECT_ID BIGINT,
    FOREIGN KEY (BOOK_ID) REFERENCES BOOK(ID),
    FOREIGN KEY (SUBJECT_ID) REFERENCES SUBJECT(ID)
);

CREATE TABLE LANGUAGE (
    ID   BIGINT PRIMARY KEY,
    NAME VARCHAR(4000)
);

CREATE TABLE LANGUAGE2BOOK (
    BOOK_ID     BIGINT,
    LANGUAGE_ID BIGINT,
    FOREIGN KEY (BOOK_ID) REFERENCES BOOK(ID),
    FOREIGN KEY (LANGUAGE_ID) REFERENCES LANGUAGE(ID)
);

