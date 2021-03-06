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
package com.acme.bestellung.config

import org.springframework.boot.Banner
import org.springframework.boot.SpringBootVersion
import org.springframework.core.SpringVersion
import org.springframework.security.core.SpringSecurityCoreVersion
import java.net.InetAddress
import java.util.Locale

/**
 * Singleton-Klasse, um sinnvolle Konfigurationswerte für den Microservice vorzugeben.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 */
object ProfilesBanner {
    /**
     * Konstante für das Spring-Profile "dev".
     */
    const val DEV = "dev"

    /**
     * Banner für den Start des Microservice in der Konsole.
     */
    val banner = Banner { _, _, out ->
        val jdkVersion = "${Runtime.version()} @ ${System.getProperty("java.version.date")}"
        val osVersion = System.getProperty("os.name")
        val localhost = InetAddress.getLocalHost()
        val username = System.getProperty("user.name")
        val serviceHost = System.getenv("BESTELLUNG_SERVICE_HOST")
        val kundeServiceHost = System.getenv("KUNDE_SERVICE_HOST") ?: "N/A"
        val kubernetes = if (serviceHost == null) {
            "N/A"
        } else {
            "BESTELLUNG_SERVICE_HOST=$serviceHost, KUNDE_SERVICE_HOST=$kundeServiceHost"
        }

        out.println(
            """
            |    __              __       ____
            |   / /_  ___  _____/ /____  / / /_  ______  ____ _
            |  / __ \/ _ \/ ___/ __/ _ \/ / / / / / __ \/ __ `/
            | / /_/ /  __(__  ) /_/  __/ / / /_/ / / / / /_/ /
            |/_.___/\___/____/\__/\___/_/_/\__,_/_/ /_/\__, /
            |                                         /____/
            |
            |(C) Juergen Zimmermann, Hochschule Karlsruhe
            |Version              1.0
            |Spring Boot          ${SpringBootVersion.getVersion()}
            |Spring Security      ${SpringSecurityCoreVersion.getVersion()}
            |Spring Framework     ${SpringVersion.getVersion()}
            |Hibernate            ${org.hibernate.Version.getVersionString()}
            |Kotlin               ${KotlinVersion.CURRENT}
            |OpenJDK              $jdkVersion
            |Betriebssystem       $osVersion
            |Rechnername          ${localhost.hostName}
            |IP-Adresse           ${localhost.hostAddress}
            |Kubernetes           $kubernetes
            |Username             $username
            |JVM Locale           ${Locale.getDefault()}
            |
            """
                .trimMargin("|"),
        )
    }
}
