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

CREATE SCHEMA IF NOT EXISTS AUTHORIZATION bestellung;

ALTER ROLE bestellung SET search_path = 'bestellung';

CREATE TABLE IF NOT EXISTS bestellung (
  id            uuid PRIMARY KEY,
  version       integer NOT NULL DEFAULT 0,
  datum         date,
  kundeId       uuid NOT NULL,
  erzeugt       timestamp NOT NULL,
  aktualisiert  timestamp NOT NULL
  );

CREATE TABLE IF NOT EXISTS bestellposition (
  id              uuid PRIMARY KEY,
  bestellung_fk   uuid REFERENCES bestellung,
  artikelId       uuid NOT NULL,
  einzelpreis     NUMERIC(10,2) NOT NULL,
  anzahl          integer NOT NULL,
  idx             integer NOT NULL DEFAULT 0
  );
