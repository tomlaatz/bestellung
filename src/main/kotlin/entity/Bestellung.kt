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
package com.acme.bestellung.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Transient
import org.springframework.data.annotation.Version
import java.time.LocalDate
import java.time.LocalDate.now
import java.time.LocalDateTime
import java.util.UUID

/**
 * Unveränderliche Daten einer Bestellung. In DDD ist Bestellung ist ein _Aggregate Root_.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 *
 * @property id ID einer Bestellung als UUID.
 * @property version Versionsnummber in der DB.
 * @property datum Bestelldatum.
 * @property kundeId ID des zugehörigen Kunden.
 * @property bestellpositionen Liste von [Bestellposition]
 */
@Suppress("UnusedPrivateMember")
data class Bestellung(
    val id: BestellungId? = null,

    @Version
    val version: Int? = null,

    val datum: LocalDate = now(),

    val kundeId: KundeId,

    val bestellpositionen: List<Bestellposition> = emptyList(),

    @CreatedDate
    private val erzeugt: LocalDateTime? = null,

    @LastModifiedDate
    private val aktualisiert: LocalDateTime? = null,
) {
    /**
     * @property kundeNachname Nachname des Kunden. Der Nachname wird nicht in der DB gespeichert und ist nicht
     * Bestandteil der copy-Funktion.
     */
    @Transient
    @Suppress("DataClassShouldBeImmutable")
    var kundeNachname: String = "N/A"

    /**
     * Vergleich mit einem anderen Objekt oder null.
     * @param other Das zu vergleichende Objekt oder null
     * @return True, falls das zu vergleichende (Bestellung-) Objekt die gleiche ID hat.
     */
    @Suppress("ReturnCount")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Bestellung
        return id != null && id == other.id
    }

    /**
     * Hashwert aufgrund der ID.
     * @return Der Hashwert.
     */
    override fun hashCode() = id?.hashCode() ?: this::class.hashCode()
}

/**
 * Datentyp für die IDs von Bestellung-Objekten
 */
typealias BestellungId = UUID

/**
 * Datentyp für die IDs von Kunde-Objekten
 */
typealias KundeId = UUID
