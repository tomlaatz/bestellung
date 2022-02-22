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
import com.acme.bestellung.entity.Bestellposition
import org.springframework.stereotype.Service
import java.math.BigDecimal

/**
 * Validierung von Objekten der Klasse [Bestellposition].
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 */
@Service
class BestellpositionValidator {
    /**
     * `Validator` für _Yavi_
     */
    val validator = validator {
        Bestellposition::einzelpreis {
            greaterThanOrEqual(BigDecimal.ZERO).message(
                ViolationMessage.of("bestellposition.einzelpreis.min", "The unit price must be at least {1}."),
            )
        }

        Bestellposition::anzahl {
            greaterThanOrEqual(1).message(
                ViolationMessage.of("bestellposition.anzahl.min", "The minimum quantity is {1}."),
            )
        }
    }
}
