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
package com.acme.bestellung.graphql

import com.acme.bestellung.config.ProfilesBanner.DEV
import com.jayway.jsonpath.JsonPath
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.json.shouldContainJsonKey
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.condition.EnabledForJreRange
import org.junit.jupiter.api.condition.JRE.JAVA_17
import org.junit.jupiter.api.condition.JRE.JAVA_18
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.getBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitEntity
import org.springframework.web.reactive.function.client.awaitExchange

@Tag("graphql")
@DisplayName("GraphQL-Schnittstelle fuer Bestellungen testen")
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles(DEV)
@EnabledForJreRange(min = JAVA_17, max = JAVA_18)
@Suppress("ClassName", "JsonStandardCompliance", "UseDataClass")
@ExperimentalCoroutinesApi
class BestellungGraphQLTest(@LocalServerPort private val port: Int, ctx: ApplicationContext) {
    private val baseUrl = "$SCHEMA://$HOST:$port/graphql"
    private val client = WebClient
        .builder()
        .filter(ExchangeFilterFunctions.basicAuthentication(USER_ADMIN, PASSWORD))
        .defaultHeader("Content-type", MediaType.APPLICATION_JSON_VALUE)
        .baseUrl(baseUrl)
        .build()

    init {
        ctx.getBean<BestellungGraphQlController>() shouldNotBe null
    }

    // -------------------------------------------------------------------------
    // L E S E N
    // -------------------------------------------------------------------------
    @Nested
    inner class Query {
        @Nested
        inner class `Suche anhand der ID` {
            @ParameterizedTest
            @CsvSource("$ID_VORHANDEN, $DATUM_VORHANDEN")
            fun `Suche mit vorhandener ID`(id: String, datum: String) = runTest {
                // given
                val query = """
                    {
                        "query": "query {
                            bestellung(id: \"$id\") {
                                version
                                datum
                            }
                        }"
                    }
                """.trimIndent().replace("\n", " ")

                // when
                val response = client.post()
                    .bodyValue(query)
                    .awaitExchange { response -> response.awaitEntity<String>() }

                // then
                assertSoftly(response) {
                    statusCode shouldBe HttpStatus.OK

                    body shouldNotBe null
                    val bestellungPath = "$.data.bestellung"
                    body shouldContainJsonKey bestellungPath

                    val versionPath = "$bestellungPath.version"
                    body shouldContainJsonKey versionPath
                    val version: Int = JsonPath.read(body, versionPath)
                    version shouldBe 0

                    val datumPath = "$bestellungPath.datum"
                    body shouldContainJsonKey datumPath
                    val datumResponse: String = JsonPath.read(body, datumPath)
                    datumResponse shouldBe datum
                }
            }

            @ParameterizedTest
            @ValueSource(strings = [ID_NICHT_VORHANDEN])
            fun `Suche mit nicht-vorhandener ID`(id: String) = runTest {
                // given
                val query = """
                    {
                        "query": "query {
                            bestellung(id: \"$id\") {
                                datum
                            }
                        }"
                    }
                """.trimIndent().replace("\n", " ")

                // when
                val response = client.post()
                    .bodyValue(query)
                    .awaitExchange { response -> response.awaitEntity<String>() }

                // then
                assertSoftly(response) {
                    statusCode shouldBe HttpStatus.OK

                    body shouldNotBe null
                    val classificationPath = "$.errors[0].extensions.classification"
                    body shouldContainJsonKey classificationPath
                    val classification: String = JsonPath.read(body, classificationPath)
                    classification shouldBe "NOT_FOUND"
                }
            }
        }
    }

    private companion object {
        const val SCHEMA = "http"
        const val HOST = "localhost"
        const val USER_ADMIN = "admin"
        const val PASSWORD = "p"

        const val ID_VORHANDEN = "10000000-0000-0000-0000-000000000001"
        const val ID_NICHT_VORHANDEN = "99999999-9999-9999-9999-999999999999"

        const val DATUM_VORHANDEN = "2021-01-01"
    }
}
