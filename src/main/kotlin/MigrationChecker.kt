import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import model.RuleDefinition
import org.w3c.dom.Window
import org.w3c.fetch.Request

object MigrationChecker {

    private lateinit var ruleDefinitions: RuleDefinition
    private var isChecking = false

    // rule_definitions.jsonを読み込む
    fun loadRuleDefinitions() {
        window.fetch(Request("https://raw.githubusercontent.com/itsu-dev/scs-migration-checker/master/src/main/resources/rule_definitions.json"))
            .then(onFulfilled = {
                it.text().then { json ->
                    onLoadFinished(json)
                }
            })
    }

    private fun onLoadFinished(json: String) {
        ruleDefinitions = Json.decodeFromString(RuleDefinition.serializer(), json)
        check(listOf("地球環境学1", "地球進化学2", "数学リテラシー1"))
    }

    // 移行要件をチェックする
    // subjects: ユーザの登録済み講義（講義名）
    fun check(subjects: List<String>) {

        if (isChecking) {
            window.alert("判定中です")
            return
        }

        isChecking = true

        ruleDefinitions.faculties.forEach { faculty ->
            var passedRequiredSubjects: Boolean? = null // 応募要件を満足したか
            var passedImportantSubjects: Boolean? = null // 重点科目上限単位数を満たしたか

            val tr = document.createElement("tr")
            document.getElementById("result")!!.appendChild(tr)

            val facultyName = document.createElement("td").also {
                it.innerHTML = faculty.facultyName
            }
            tr.appendChild(facultyName)

            val comments = document.createElement("td")
            tr.appendChild(comments)

            faculty.rules.forEach { rule ->
                when (rule.type) {
                    // 応募要件
                    "required_subjects" -> {
                        passedRequiredSubjects ?: run {
                            passedRequiredSubjects = true
                        }

                        var count = 0
                        rule.subjects.forEach { if (subjects.contains(it)) count++ }
                        if (count < rule.minimum) passedRequiredSubjects = false
                    }

                    // 重点科目上限
                    "important_subjects" -> {
                        passedImportantSubjects ?: run {
                            passedImportantSubjects = true
                        }

                        var count = 0
                        rule.subjects.forEach { if (subjects.contains(it)) count++ }
                        if (count < rule.minimum) passedImportantSubjects = false
                    }
                }
            }

            // メッセージを表示
            if (passedRequiredSubjects == false) comments.innerHTML += "応募要件を満たしていません<br />"
            if (passedImportantSubjects == false) comments.innerHTML += "重点科目上限を超えていません<br />"

            // 応募要件の〇×
            tr.appendChild(
                document.createElement("td").also {
                    it.innerHTML = when (passedRequiredSubjects) {
                        true -> "<span class=\"passed\">〇</span>"
                        false -> "<span class=\"missed\">×</span>"
                        else -> "<span>-</span>"
                    }
                }
            )

            // 重点科目上限の〇×
            tr.appendChild(
                document.createElement("td").also {
                    it.innerHTML = when (passedImportantSubjects) {
                        true -> "<span class=\"passed\">〇</span>"
                        false -> "<span class=\"missed\">×</span>"
                        else -> "<span>-</span>"
                    }
                }
            )

            // 移行要件の適合度によって学類の色を変える
            when {
                // 応募要件と重点科目上限を満足
                passedRequiredSubjects != false && passedImportantSubjects != false ->
                    facultyName.classList.add("faculty-name-passed")

                // 応募要件のみ満足
                passedRequiredSubjects != false && passedImportantSubjects == false ->
                    facultyName.classList.add("faculty-name-ok")

                // 何も満足していない
                else -> facultyName.classList.add("faculty-name-missed")
            }

        }

        isChecking = false
    }
}