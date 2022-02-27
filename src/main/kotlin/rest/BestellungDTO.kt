/*
 * Copyright (C) 2016 - present Juergen Zimmermann, Hochschule Karlsruhe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.acme.bestellung.rest

import com.acme.bestellung.entity.Bestellposition
import com.acme.bestellung.entity.Bestellung
import com.acme.bestellung.entity.KundeId
import java.time.LocalDate

/**
 * Daten einer neuen Bestellung.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 *
 * @property kundeId ID des zugehörigen Kunden.
 * @property bestellpositionen Liste von [Bestellposition]
 */

data class BestellungDTO(
    val kundeId: KundeId,

    val bestellpositionen: MutableList<Bestellposition>,
) {
    /**
     * Konvertierung einer HATEOAS-Repräsentation in ein Objekt des Anwendungskerns
     * @return Bestellungsobjekt für den Anwendungskern
     */
    fun toBestellung() = Bestellung(datum = LocalDate.now(), kundeId = kundeId, bestellpositionen = bestellpositionen)
}
