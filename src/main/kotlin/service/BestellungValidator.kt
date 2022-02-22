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

import am.ik.yavi.builder.validator
import am.ik.yavi.core.ViolationMessage
import com.acme.bestellung.entity.Bestellung
import org.springframework.stereotype.Service
import java.time.LocalDate.now

/**
 * Validierung von Objekten der Klasse [Bestellung].
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 */
@Service
class BestellungValidator(bestellpositionValidator: BestellpositionValidator) {
    private val validator = validator {
        Bestellung::bestellpositionen {
            notEmpty().message(
                ViolationMessage.of("bestellung.bestellpositionen.notEmpty", "At least one order item is required."),
            )
        }

        Bestellung::bestellpositionen.forEach(bestellpositionValidator.validator)

        Bestellung::datum {
            beforeOrEqual { now() }.message(
                ViolationMessage.of("bestellung.datum.beforeOrEqual", "The date must be now or in the past."),
            )
        }
    }

    /**
     * Validierung eines Entity-Objekts der Klasse [Bestellung]
     *
     * @param bestellung Das zu validierende Bestellung-Objekt
     * @return Eine Liste mit den Verletzungen der Constraints oder eine leere Liste
     */
    fun validate(bestellung: Bestellung) = validator.validate(bestellung)
}
