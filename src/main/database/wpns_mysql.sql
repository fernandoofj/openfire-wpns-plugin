# $Revision$
# $Date$

INSERT INTO ofVersion (name, version) VALUES ('wpns', 1);

CREATE TABLE ofWPNS (
	JID VARCHAR(200) NOT NULL,
	phoneAppID VARCHAR(200) NOT NULL,
	phoneUrl VARCHAR(200) NOT NULL,
  PRIMARY KEY (JID)
);
