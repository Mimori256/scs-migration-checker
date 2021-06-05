import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.json.JsonConfiguration
import model.RuleDefinition
import org.w3c.fetch.Body
import org.w3c.fetch.Request

var isUnder1100px = window.matchMedia("screen and (max-width: 1100px)").matches

fun main() {
    MigrationChecker.loadRuleDefinitions()
}