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
package com.acme.bestellung.service

import io.smallrye.mutiny.Uni
import org.hibernate.reactive.mutiny.Mutiny
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Join
import javax.persistence.criteria.Path
import javax.persistence.criteria.Root
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

// Fuer Hibernate gibt es keinen Kotlin-Code und deshalb in dessen build.gradle keine dependencies fuer Kotlin
// https://hibernate.zulipchat.com/#narrow/stream/132094-hibernate-orm-dev

/**
 * Extension Function, damit man `Session.find<E>(id)` _Kotlin-like_ aufrufen kann.
 * @return Uni mit dem gefundenen Object oder leeres Uni
 */
// reified: die Typinformation ist zur Laufzeit vollstaendig vorhanden (vs. "type erasure" in Java)
inline fun <reified E : Any> Mutiny.Session.find(id: Any): Uni<E> = find(E::class.java, id)

/**
 * Extension Function, damit man `Session.createNamedQuery<E>(queryName)` _Kotlin-like_ aufrufen kann.
 * @return Objekt der Java-Klasse Mutiny.Query, um dann die Query auszuführen.
 */
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
inline fun <reified R : Any> Mutiny.Session.createNamedQuery(queryName: String): Mutiny.Query<R> =
    createNamedQuery(queryName, R::class.java)

// Die Ideen fuer nachfolgende Extension Functions sind aus:
// https://lifeinide.com/post/2021-04-29-making-jpa-criteria-api-less-awkward-with-kotlin/#typesafe-entity-fields-access

/**
 * Extension Function, damit man `CriteriaBuilder.create<E>()` _Kotlin-like_ aufrufen kann.
 * @return Erzeugtes Objekt der Java-Klasse `CriteriaQuery`
 */
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
inline fun <reified E : Any> CriteriaBuilder.createQuery(): CriteriaQuery<E> = createQuery(E::class.java)

/**
 * Extension Function, damit man `CriteriaQuery<E>.from(Result::class)` _Kotlin-like_ aufrufen kann.
 * @return Erzeugtes Objekt der Java-Klasse `Root`
 */
// TODO ab Kotlin 1.7 https://youtrack.jetbrains.com/issue/KT-13394
// inline fun <E, reified R : Any> CriteriaQuery<E>.from(): Root<R> = from(R::class.java)
inline fun <E, reified R : Any> CriteriaQuery<E>.from(rootClass: KClass<R>): Root<R> = from(rootClass.java)

/**
 * Extension Function für "type-safe property access" ausgehend von der Entity-Klasse E als Root.
 * V ist die "Value-Klasse" der Property.
 * @param prop Property der Entity-Klasse E
 */
inline fun <reified E, V> Root<E>.get(prop: KProperty1<E, V>): Path<V> = get(prop.name)

/**
 * Extension Function für "type-safe property access" bei "nested properties" der Entity-Klasse E
 * V ist die "Value-Klasse" der Property.
 */
inline fun <reified E, V> Path<E>.get(prop: KProperty1<E, V>): Path<V> = get(prop.name)

/**
 * Extension Function für einen "type-safe join" ausgehend von der Entity-Klasse E als Root.
 * V ist die "Value-Klasse" der Property.
 * @param prop Property der Entity-Klasse E
 */
@Suppress("unused")
inline fun <reified E, V> Root<E>.join(prop: KProperty1<E, V>): Join<E, E> = join(prop.name)
