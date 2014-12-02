# $Revision$
# $Date$

INSERT INTO ofVersion (name, version) VALUES ('gcmcedro', 1);

CREATE TABLE ofGCMCedro (
	JID VARCHAR(200) NOT NULL,
	phoneAppID VARCHAR(200) NOT NULL,
	phoneUrl VARCHAR(200) NOT NULL,
  PRIMARY KEY (JID)
);
