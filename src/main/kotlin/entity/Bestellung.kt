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

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.util.UUID
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.NamedQuery
import javax.persistence.OneToMany
import javax.persistence.OrderColumn
import javax.persistence.Table
import javax.persistence.Version

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




@Entity
@Table(name = "bestellung")
@NamedQuery(
    name = Bestellung.BY_KUNDEID,
    query = "SELECT b " +
        "FROM  Bestellung b " +
        "WHERE b.kundeId = :${Bestellung.PARAM_KUNDEID}",
)
@Suppress("DataClassShouldBeImmutable")
data class Bestellung(
    @Id
    @GeneratedValue
    val id: BestellungId? = null,

    @Version
    val version: Int = 0,

    var datum: LocalDate?,

    var kundeId: KundeId? = null,

    @OneToMany(
        cascade = [CascadeType.PERSIST, CascadeType.REMOVE],
        fetch = FetchType.EAGER,
    )
    @JoinColumn(name = "bestellung_fk", nullable = false)
    var bestellpositionen: MutableList<Bestellposition>,

    @CreationTimestamp
    @Suppress("UnusedPrivateMember")
    private val erzeugt: LocalDateTime = now(),

    @UpdateTimestamp
    @Suppress("UnusedPrivateMember")
    private val aktualisiert: LocalDateTime = now(),
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

    companion object {
        private const val PREFIX = "Bestellung."

        /**
         * Name für die Named Query, mit der Bestellungen anhand der KundenID gesucht werden
         */
        const val BY_KUNDEID = "${PREFIX}byKundeId"

        /**
        * Parametername für die KundenId
        */
        const val PARAM_KUNDEID = "kundeId"

        }

    }

/**
 * Datentyp für die IDs von Bestellung-Objekten
 */
typealias BestellungId = UUID

/**
 * Datentyp für die IDs von Kunde-Objekten
 */
typealias KundeId = UUID
