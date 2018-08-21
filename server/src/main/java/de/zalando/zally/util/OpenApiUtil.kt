package de.zalando.zally.util

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.responses.ApiResponse

data class HeaderElement(
    val name: String,
    val element: Any
)

fun OpenAPI.getAllHeaders(): Set<HeaderElement> {

    fun Collection<Parameter>?.extractHeaders() = orEmpty()
        .filter { it.`in` == "header" }
        .map { HeaderElement(it.name, it) }
        .toSet()

    fun Collection<ApiResponse>?.extractHeaders() = orEmpty()
        .flatMap { it.headers.orEmpty().entries }
        .map { HeaderElement(it.key, it.value) }
        .toSet()

    val fromParams = components.parameters.orEmpty().values.extractHeaders()

    val fromPaths = paths.orEmpty().flatMap { (_, path) ->
        val fromPathParameters = path.parameters.extractHeaders()
        val fromOperations = path.readOperations().flatMap { operation ->
            val fromOpParams = operation.parameters.extractHeaders()
            val fromOpResponses = operation.responses.orEmpty().values.extractHeaders()
            fromOpParams + fromOpResponses
        }
        fromPathParameters + fromOperations
    }

    return fromParams + fromPaths
}

/**
 * Returns all defined schemas of an API specification
 * @return a collection of schemas
 */
fun OpenAPI.getAllSchemas(): Collection<Schema<Any>> = this.components.schemas.orEmpty().values +
    this.components.responses.values.flatMap { it.content.values.mapNotNull { it.schema } } +
    this.components.requestBodies.values.flatMap { it.content.values.mapNotNull { it.schema } } +
    this.paths.orEmpty().flatMap {
        it.value.readOperations().flatMap { it.parameters.orEmpty().mapNotNull { it.schema } }
    } +
    this.paths.orEmpty().flatMap {
        it.value.readOperations().flatMap {
            it.responses.orEmpty().flatMap { it.value.content.orEmpty().values.mapNotNull { it.schema } }
        }
    } +
    this.paths.orEmpty().flatMap {
        it.value.readOperations().flatMap { it.requestBody?.content.orEmpty().values.mapNotNull { it.schema } }
    }

/**
 * Returns all defined parameters of an API specification
 * @return a collection of parameters
 */
fun OpenAPI.getAllParameters(): Map<String, Parameter> = this.components.parameters.orEmpty() +
    this.paths.orEmpty().values.flatMap { it.parameters.orEmpty().mapNotNull { it.name to it } } +
    this.paths.orEmpty().values.flatMap {
        it.readOperations()
            .flatMap { it.parameters.orEmpty().mapNotNull { it.name to it } }
    }
