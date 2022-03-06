/*
 * Copyright (C) 2021 - present Juergen Zimmermann, Hochschule Karlsruhe
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
package com.acme.bestellung.service

import am.ik.yavi.core.ConstraintViolation
import com.acme.bestellung.entity.Bestellung
import com.acme.bestellung.entity.BestellungId

/**
 * Resultat-Typ für [BestellungReadService.findById]
 */
sealed interface FindByIdResult {
    /**
     * Resultat-Typ, wenn eine Bestellung gefunden wurde.
     * @property bestellung Die gefundene Bestellung
     */
    data class Success(val bestellung: Bestellung) : FindByIdResult

    /**
     * Resultat-Typ, wenn keine Bestellung gefunden wurde.
     * @property id Die BestellungsId.
     */
    data class NotFound(val id: BestellungId) : FindByIdResult
}

/**
 * Resultat-Typ für [BestellungReadService.create]
 */
sealed interface CreateResult {
    /**
     * Resultat-Typ, wenn eine neue Bestellung erfolgreich angelegt wurde.
     * @property bestellung Die neu angelegte Bestellung
     */
    data class Success(val bestellung: Bestellung) : CreateResult

    /**
     * Resultat-Typ, wenn eine Bestellung wegen Constraint-Verletzungen nicht angelegt wurde.
     * @property violations Die verletzten Constraints
     */
    data class ConstraintViolations(val violations: Collection<ConstraintViolation>) : CreateResult
}
