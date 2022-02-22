/*
 * Copyright (C) 2018 - present Juergen Zimmermann, Hochschule Karlsruhe
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
package com.acme.bestellung.config.dev

import com.acme.bestellung.entity.KundeId
import com.acme.bestellung.service.KundeClient
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean

/**
 * Den Microservice _kunde_ mit WebClient aufrufen.
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 */
interface LogWebClientKunde {
    /**
     * Bean-Definition, um einen CommandLineRunner für das Profil "dev" bereitzustellen, damit der Microservice _kunde_
     * mit WebClient aufgerufen wird.
     * @param kundeClient Client für den Microservice _kunde_
     * @return CommandLineRunner
     */
    @Bean
    fun logWebClientKunde(kundeClient: KundeClient) = CommandLineRunner {
        runBlocking {
            val kundeId = KundeId.fromString("00000000-0000-0000-0000-000000000001")
            val result = kundeClient.findById(kundeId)

            LoggerFactory.getLogger(LogWebClientKunde::class.java).warn("Resultat zu Kunde-ID {}: {}", kundeId, result)
        }
    }

    // Fuer OAuth siehe
    // https://github.com/bclozel/spring-reactive-university/blob/master/src/main/java/com/example/integration/...
    //      ...gitter/GitterClient.java
}
