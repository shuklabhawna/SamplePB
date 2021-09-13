CREATE TABLE IF NOT EXISTS shedlock (
  name VARCHAR(64),
  lock_until TIMESTAMP(3) NULL,
  locked_at TIMESTAMP(3) NULL,
  locked_by VARCHAR(255),
  PRIMARY KEY (name)
);

CREATE TABLE IF NOT EXISTS marketData (
  source VARCHAR(20) NOT NULL,
  bid NUMBER NOT NULL,
  ask NUMBER NOT NULL,
  PRIMARY KEY (source)
);
