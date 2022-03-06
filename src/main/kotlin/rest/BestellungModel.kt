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
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation
import java.time.LocalDate

/**
 * "Model"-Daten einer Bestellung für HATEOAS.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 *
 * @property datum Bestelldatum.
 * @property kundeId ID des zugehörigen Kunden.
 * @property kundeNachname Nachname des Kunden.
 * @property bestellpositionen Liste von [Bestellposition]
 */

@Relation(collectionRelation = "bestellungen", itemRelation = "bestellung")
@JsonPropertyOrder("datum", "kundeId", "kundeNachname", "bestellpositionen")
data class BestellungModel(
    val datum: LocalDate? = LocalDate.now(),

    val kundeId: KundeId,

    val kundeNachname: String,

    val bestellpositionen: List<Bestellposition>,
) : RepresentationModel<BestellungModel>() {
    constructor(bestellung: Bestellung) : this(
        bestellung.datum,
        bestellung.kundeId,
        bestellung.kundeNachname,
        bestellung.bestellpositionen,
    )
}
