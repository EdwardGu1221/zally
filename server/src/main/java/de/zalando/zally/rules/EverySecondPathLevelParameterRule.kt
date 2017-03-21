package de.zalando.zally.rules

import de.zalando.zally.Violation
import de.zalando.zally.ViolationType
import de.zalando.zally.utils.PatternUtil.isPathVariable
import io.swagger.models.Swagger
import org.springframework.stereotype.Component

@Component
class EverySecondPathLevelParameterRule : AbstractRule() {
    override val title = "Every Second Path Level To Be Parameter"
    override val url = null
    override val violationType = ViolationType.MUST
    private val DESCRIPTION = "Every second path level must be a path parameter"

    override fun validate(swagger: Swagger): Violation? {
        val paths = swagger.paths.orEmpty().keys.filterNot {
            val pathSegments = it.split("/").filter { it.isNotEmpty() }
            pathSegments.filterIndexed { i, segment -> isPathVariable(segment) == (i % 2 == 0) }.isEmpty()
        }
        return if (paths.isNotEmpty()) Violation(this, title, DESCRIPTION, violationType, "", paths) else null
    }
}