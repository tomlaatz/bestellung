/*
 * Copyright (C) 2017 - present Juergen Zimmermann, Hochschule Karlsruhe
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
package com.acme.bestellung.entity

import net.minidev.json.annotate.JsonIgnore
import java.math.BigDecimal
import java.util.UUID
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.Table

/**
 * Unveränderliche Daten einer Bestellungposition.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 *
 * @property artikelId ID der bestellten Artikels.
 * @property einzelpreis Einzelpreis.
 * @property anzahl Anzahl des bestellten Artikels.a
 */
@Entity
@Table(name = "bestellposition")
data class Bestellposition(

    @Id
    @GeneratedValue
    @JsonIgnore
    val id: UUID? = null,

    val artikelId: ArtikelId? = null,

    val einzelpreis: BigDecimal = BigDecimal("0"),

    val anzahl: Int = 1,

    val idx: Int = 0
)

/**
 * Datentyp für die IDs von Artikel-Objekten
 */
typealias ArtikelId = UUID
