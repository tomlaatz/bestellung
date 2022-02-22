/*
 * Copyright (C) 2016 - 2018 Juergen Zimmermann, Hochschule Karlsruhe
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

import com.acme.bestellung.config.dev.TestDaten.bestellungen
import com.acme.bestellung.entity.Bestellung
import com.mongodb.reactivestreams.client.MongoCollection
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.bson.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.CollectionOptions
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.createCollection
import org.springframework.data.mongodb.core.dropCollection
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.indexOps
import org.springframework.data.mongodb.core.insert
import org.springframework.data.mongodb.core.oneAndAwait
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty.array
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty.date
import org.springframework.data.mongodb.core.schema.MongoJsonSchema

/**
 * Interface, um im Profil _dev_ die (Test-) DB neu zu laden.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 */
interface DbPopulate {
    /**
     * Bean-Definition, um einen CommandLineRunner für das Profil "dev" bereitzustellen, damit die (Test-) DB neu
     * geladen wird.
     * @param mongo Template für MongoDB
     * @return CommandLineRunner
     */
    @Bean
    fun dbPopulate(mongo: ReactiveMongoOperations) = CommandLineRunner {
        runBlocking {
            mongo.dropCollection<Bestellung>().awaitSingleOrNull()

            val logger = LoggerFactory.getLogger(DbPopulate::class.java)

            logger.warn("Neuladen der Collection 'Bestellung'")
            createCollectionAndSchema(mongo, logger)
            createIndex(mongo, logger)

            bestellungen.onEach { bestellung ->
                mongo.insert<Bestellung>().oneAndAwait(bestellung)
                logger.warn("{}", bestellung)
            }.collect()
        }
    }

    private suspend fun createCollectionAndSchema(
        mongoOps: ReactiveMongoOperations,
        logger: Logger,
    ): MongoCollection<Document> {
        val schema = createSchema()
        logger.info("JSON Schema fuer Bestellung: {}", schema.toDocument().toJson())
        return mongoOps.createCollection<Bestellung>(CollectionOptions.empty().schema(schema)).awaitSingle()
    }

    private fun createSchema() = MongoJsonSchema.builder()
        .required(datum, kundeId, bestellpositionen)
        .properties(
            date(datum),
            array(bestellpositionen).uniqueItems(true),
        )
        .build()

    private suspend fun createIndex(mongoOps: ReactiveMongoOperations, logger: Logger): String {
        logger.warn("Index fuer '$kundeId'")
        val idx = Index(kundeId, Sort.Direction.ASC).named(kundeId)
        return mongoOps.indexOps<Bestellung>().ensureIndex(idx).awaitSingle()
    }

    private companion object {
        val datum = Bestellung::datum.name
        val kundeId = Bestellung::kundeId.name
        val bestellpositionen = Bestellung::bestellpositionen.name
    }
}
