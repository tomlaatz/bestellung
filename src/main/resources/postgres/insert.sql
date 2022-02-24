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
-- psql --dbname=kunde --username=kunde --file=/scripts/insert.sql

-- https://www.postgresql.org/docs/current/app-psql.html

INSERT INTO bestellung (id, version, datum, kundeId, erzeugt, aktualisiert) VALUES ('10000000-0000-0000-0000-000000000000',0,'2019-12-23','00000000-0000-0000-0000-000000000000', '2022-01-01 00:00:00', '2022-01-01 00:00:00');
INSERT INTO bestellung (id, version, datum, kundeId, erzeugt, aktualisiert) VALUES ('10000000-0000-0000-0000-000000000001',0,'2020-03-14','00000000-0000-0000-0000-000000000001', '2022-01-02 00:00:00', '2022-01-02 00:00:00');
INSERT INTO bestellung (id, version, datum, kundeId, erzeugt, aktualisiert) VALUES ('10000000-0000-0000-0000-000000000002',0,'2021-02-15','00000000-0000-0000-0000-000000000002', '2022-01-03 00:00:00', '2022-01-03 00:00:00');
INSERT INTO bestellung (id, version, datum, kundeId, erzeugt, aktualisiert) VALUES ('10000000-0000-0000-0000-000000000003',0,'2022-11-29','00000000-0000-0000-0000-000000000030', '2022-01-04 00:00:00', '2022-01-04 00:00:00');
INSERT INTO bestellung (id, version, datum, kundeId, erzeugt, aktualisiert) VALUES ('10000000-0000-0000-0000-000000000004',0,'2017-05-14','00000000-0000-0000-0000-000000000040', '2022-01-05 00:00:00', '2022-01-05 00:00:00');

INSERT INTO bestellposition (id, artikelId, einzelpreis, anzahl, bestellung_fk, idx) VALUES ('20000000-0000-0000-0000-000000000000', '30000000-0000-0000-0000-000000000000', 10, 1, '10000000-0000-0000-0000-000000000000', 0);
INSERT INTO bestellposition (id, artikelId, einzelpreis, anzahl, bestellung_fk, idx) VALUES ('20000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 34, 1, '10000000-0000-0000-0000-000000000001', 0);
INSERT INTO bestellposition (id, artikelId, einzelpreis, anzahl, bestellung_fk, idx) VALUES ('20000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002', 999, 2, '10000000-0000-0000-0000-000000000002', 0);
INSERT INTO bestellposition (id, artikelId, einzelpreis, anzahl, bestellung_fk, idx) VALUES ('20000000-0000-0000-0000-000000000030', '30000000-0000-0000-0000-000000000003', 500, 3, '10000000-0000-0000-0000-000000000003',0);
INSERT INTO bestellposition (id, artikelId, einzelpreis, anzahl, bestellung_fk, idx) VALUES ('20000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000004', 1000, 1, '10000000-0000-0000-0000-000000000004',0);
INSERT INTO bestellposition (id, artikelId, einzelpreis, anzahl, bestellung_fk, idx) VALUES ('20000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-000000000005', 123478, 1, '10000000-0000-0000-0000-000000000001', 1);
INSERT INTO bestellposition (id, artikelId, einzelpreis, anzahl, bestellung_fk, idx) VALUES ('20000000-0000-0000-0000-000000000060', '30000000-0000-0000-0000-000000000006', 245, 2, '10000000-0000-0000-0000-000000000001', 2);

