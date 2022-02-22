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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.acme.bestellung.graphql

import graphql.GraphQLError
import org.springframework.graphql.execution.ErrorType.NOT_FOUND

/**
 * Exception, falls mit dem Anwendungskern keine Bestellung gefunden wird.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 */
class NotFoundException : BestellungGraphQlException()

/**
 * Fehlerklasse für GraphQL, falls eine [NotFoundException] geworfen wurde. Die Abbildung erfolgt in
 * [ExceptionResolverAdapter].
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 */
class NotFoundError : GraphQLError {
    /**
     * `ErrorType` auf `NOT_FOUND` setzen.
     */
    override fun getErrorType() = NOT_FOUND

    /**
     * Message innerhalb von _Errors_ beim Response für einen GraphQL-Request.
     */
    override fun getMessage() = "Keine Bestellung gefunden"

    /**
     * Keine Angabe von Zeilen- und Spaltennummer der GraphQL-Query, falls keine Bestellung gefunden wurde.
     */
    override fun getLocations() = null
}
