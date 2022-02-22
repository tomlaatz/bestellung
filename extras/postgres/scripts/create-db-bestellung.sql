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
-- psql --dbname=postgres --username=postgres --file=/scripts/create-db-bestellung.sql

-- https://www.postgresql.org/docs/14/sql-createrole.html
CREATE ROLE bestellung LOGIN PASSWORD 'p';

-- https://www.postgresql.org/docs/14/sql-createdatabase.html
CREATE DATABASE bestellung;

GRANT ALL ON DATABASE bestellung TO bestellung;