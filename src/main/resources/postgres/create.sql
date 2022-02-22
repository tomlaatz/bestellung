-- Copyright (C) 2022 - present Juergen Zimmermann, Hochschule Karlsruhe
--
-- This program is free software: you can redistribute it and/or modify
-- it under the terms of the GNU General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
--
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU General Public License for more details.
--
-- You should have received a copy of the GNU General Public License
-- along with this program.  If not, see <https://www.gnu.org/licenses/>.

-- docker compose exec postgres bash
-- psql --dbname=kunde --username=kunde --file=/scripts/create.sql

-- https://www.postgresql.org/docs/devel/app-psql.html
-- https://www.postgresql.org/docs/current/ddl-schemas.html
-- https://www.postgresql.org/docs/current/ddl-schemas.html#DDL-SCHEMAS-CREATE
-- "user-private schema" (Default-Schema: public)
CREATE SCHEMA IF NOT EXISTS AUTHORIZATION kunde;

ALTER ROLE kunde SET search_path = 'kunde';

-- https://www.postgresql.org/docs/current/sql-createtable.html
-- https://www.postgresql.org/docs/current/datatype.html
CREATE TABLE IF NOT EXISTS umsatz (
  -- https://www.postgresql.org/docs/current/datatype-uuid.html
  -- https://www.postgresql.org/docs/current/ddl-constraints.html#DDL-CONSTRAINTS-PRIMARY-KEYS
  -- impliziter Index fuer Primary Key
                                    id        uuid PRIMARY KEY,
  -- https://www.postgresql.org/docs/current/datatype-numeric.html#DATATYPE-NUMERIC-DECIMAL
  -- https://www.postgresql.org/docs/current/datatype-money.html
                                    betrag    NUMERIC(10,2) NOT NULL,
  -- https://www.postgresql.org/docs/current/ddl-constraints.html#id-1.5.4.6.6
  -- https://www.postgresql.org/docs/current/functions-matching.html#FUNCTIONS-POSIX-REGEXP
  waehrung  varchar(3) NOT NULL CHECK (waehrung ~ '[A-Z]{3}')
  );

CREATE TABLE IF NOT EXISTS adresse (
                                     id    uuid PRIMARY KEY,
                                     plz   varchar(5) NOT NULL CHECK (plz ~ '\d{5}'),
  ort   varchar(40) NOT NULL
  );

-- default: btree
CREATE INDEX IF NOT EXISTS plz_idx ON adresse (plz);

CREATE TABLE IF NOT EXISTS kunde (
                                   id            uuid PRIMARY KEY,
  -- https://www.postgresql.org/docs/current/datatype-numeric.html#DATATYPE-INT
                                   version       integer NOT NULL DEFAULT 0,
                                   nachname      varchar(40) NOT NULL,
  -- impliziter Index als B-Baum durch UNIQUE
  -- https://www.postgresql.org/docs/current/ddl-constraints.html#DDL-CONSTRAINTS-UNIQUE-CONSTRAINTS
  email         varchar(40) UNIQUE NOT NULL,
  -- https://www.postgresql.org/docs/current/ddl-constraints.html#DDL-CONSTRAINTS-CHECK-CONSTRAINTS
  kategorie     integer NOT NULL CHECK (kategorie >= 0 AND kategorie <= 9),
  -- https://www.postgresql.org/docs/current/datatype-boolean.html
  newsletter    boolean NOT NULL DEFAULT FALSE,
  -- https://www.postgresql.org/docs/current/datatype-datetime.html
  geburtsdatum  date,
  homepage      varchar(40),
  geschlecht    varchar(1) CHECK (geschlecht ~ 'M|W|D'),
  familienstand varchar(2) CHECK (familienstand ~ 'L|VH|G|VW'),
  -- https://www.postgresql.org/docs/current/ddl-constraints.html#DDL-CONSTRAINTS-FK
  umsatz_fk     uuid REFERENCES umsatz,
  adresse_fk    uuid NOT NULL REFERENCES adresse,
  username      varchar(20) NOT NULL,
  -- https://www.postgresql.org/docs/current/datatype-datetime.html
  erzeugt       timestamp NOT NULL,
  aktualisiert  timestamp NOT NULL
  );

CREATE INDEX IF NOT EXISTS nachname_idx ON kunde (nachname);

CREATE TABLE IF NOT EXISTS kunde_interesse (
                                             kunde_fk  uuid NOT NULL REFERENCES kunde,
                                             interesse varchar(1) NOT NULL CHECK (interesse ~ 'S|L|R'),
  PRIMARY KEY (kunde_fk, interesse)
  );

CREATE INDEX IF NOT EXISTS interesse_kunde_idx ON kunde_interesse (kunde_fk);

CREATE TABLE IF NOT EXISTS custom_user (
                                         id       uuid PRIMARY KEY,
                                         username varchar(40) UNIQUE NOT NULL,
  password varchar(150) NOT NULL
  );

CREATE TABLE IF NOT EXISTS custom_user_rolle (
                                               custom_user_fk uuid NOT NULL REFERENCES custom_user,
                                               rolle          varchar(20) NOT NULL CHECK (rolle ~ 'ADMIN|KUNDE|ACTUATOR'),
  PRIMARY KEY (custom_user_fk, rolle)
  );

CREATE INDEX IF NOT EXISTS custom_user_rolle_idx ON custom_user_rolle (custom_user_fk);
