package datamaintain.test

import datamaintain.core.db.driver.DatamaintainDriver
import datamaintain.core.step.executor.Execution
import datamaintain.domain.script.ExecutedScript
import datamaintain.domain.script.ExecutionStatus
import datamaintain.domain.script.ScriptAction
import datamaintain.domain.script.ScriptWithContent

/**
 * Duplicated, needs refactoring
 * TODO: https://github.com/4sh/datamaintain/issues/213
 */
class FakeDatamaintainDriver : DatamaintainDriver("") {
    override fun executeScript(script: ScriptWithContent): Execution {
        return Execution(
            executionStatus = ExecutionStatus.OK
        )
    }

    override fun listExecutedScripts(): Sequence<ExecutedScript> = sequenceOf()

    override fun markAsExecuted(executedScript: ExecutedScript): ExecutedScript {
        return executedScript.copy(executionStatus = ExecutionStatus.OK, action = ScriptAction.MARK_AS_EXECUTED)
    }

    override fun overrideScript(executedScript: ExecutedScript): ExecutedScript {
        return executedScript.copy(executionStatus = ExecutionStatus.OK, action = ScriptAction.OVERRIDE_EXECUTED)
    }
}