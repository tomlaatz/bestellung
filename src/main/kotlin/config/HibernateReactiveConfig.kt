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
package com.acme.bestellung.config

import com.acme.bestellung.entity.Bestellung
import org.hibernate.reactive.mutiny.Mutiny
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import reactor.kotlin.core.publisher.toMono
import java.util.UUID.randomUUID
import javax.persistence.Persistence

/**
 * Spring-Konfiguration für Enum-Konvertierungen beim Zugriff auf _MongoDB_.
 *
 * @author Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 */
interface HibernateReactiveConfig {
    /**
     * Factory-Funktion zum Erzeugen von `SessionFactory` für _Hibernate Reactive_. Dabei wird META-INF/persistence.xml
     * aus dem (Windows-) Verzeichnis src\main\resources eingelesen.
     * https://hibernate.org/reactive/documentation/1.1/reference/html_single/#_obtaining_a_reactive_session_factory
     * https://itnext.io/integrating-hibernate-reactive-with-spring-5427440607fe
     *
     * @return SessionFactory für _Hibernate Reactive_.
     */
    @Bean
    fun sessionFactory(): Mutiny.SessionFactory = Persistence.createEntityManagerFactory("kundePU")
        .unwrap(Mutiny.SessionFactory::class.java)

    /**
     * Initialisierung der `SessionFactory` für _Hibernate Reactive_.
     * @param factory SessionFactory für _Hibernate Reactive_, damit sie durch `persistence.xml` initialisiert wird,
     *      wobei die deklarierten SQL-Skripte aufgerufen werden.
     * @return CommandLineRunner mit der Protokollierung, dass die Initialisierung stattgefunden hat.
     */
    @Bean
    fun initSessionFactory(factory: Mutiny.SessionFactory) = CommandLineRunner {
        LoggerFactory.getLogger(HibernateReactiveConfig::class.java)
            .debug("SessionFactory wurde initialisiert")
    }
}
