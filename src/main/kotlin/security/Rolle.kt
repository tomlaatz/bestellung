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
package com.acme.bestellung.security

/**
 * Singleton für verfügbare Rollen als Strings als Objekte der Spring-Interface `GrantedAuthority`.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 */
object Rolle {
    /**
     * Die Rolle _ADMIN_
     */
    const val admin = "ADMIN"

    /**
     * Die Rolle _KUNDE_
     */
    const val kunde = "KUNDE"

    /**
     * Die Rolle _ACTUATOR_
     */
    const val actuator = "ACTUATOR"
}
