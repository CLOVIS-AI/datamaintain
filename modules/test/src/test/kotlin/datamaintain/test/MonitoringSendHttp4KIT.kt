package datamaintain.test

import datamaintain.core.Datamaintain
import datamaintain.core.config.DatamaintainConfig
import datamaintain.core.config.DatamaintainScannerConfig
import datamaintain.core.config.MonitoringConfiguration
import datamaintain.core.script.TagMatcher
import datamaintain.domain.report.ExecutionId
import datamaintain.domain.script.Tag
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.StringBody.subString
import strikt.api.expectCatching
import strikt.assertions.isSuccess
import java.nio.file.Paths
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * When monitoring configuration is given an url for monitoring, Executor should send information about
 * the current batch to the remote monitoring server
 */
class MonitoringSendHttp4KIT : AbstractMonitoringSendWithHttpTest() {
    @Nested
    inner class MonitoringIsUnreachable {
        @Test
        internal fun should_pursue_execution_when_start_does_not_answer() {
            expectCatching { buildDatamaintainWithMonitoringConfiguration().updateDatabase() }
                .isSuccess()
        }
    }

    @Nested
    inner class MonitoringIsReachable {
        @Test
        internal fun should_send_POST_start_message_to_monitoring_on_correct_URL() {
            // When
            setupMockStartAnswer()
            buildDatamaintainWithMonitoringConfiguration().updateDatabase()

            // Then
            mockServerClient.verify(request().withPath("/v1/executions/start")
                .withMethod("POST"))
        }

        @Nested
        inner class ScriptExecutionStartMessage {
            @Test
            internal fun should_send_script_execution_start_with_execution_id() {
                // When
                val executionId = 12
                setupMockStartAnswer(executionId)
                buildDatamaintainWithMonitoringConfiguration("src/test/resources/integration/ok").updateDatabase()

                // Then
                mockServerClient.verify(request().withPath("/v1/executions/$executionId/script/start").withMethod("PUT"))
            }

            @Test
            internal fun should_send_script_name_in_body() {
                checkStartMessageBodyContains("\"name\":\"01_file.js\"")
            }

            @Test
            internal fun should_send_script_checksum_in_body() {
                checkStartMessageBodyContains("\"checksum\":\"24403a1ac36cc57c3cf9857bd8c5f676\"")
            }

            @Test
            internal fun should_send_script_content_in_body() {
                checkStartMessageBodyContains("\"content\":\"db.simple.insert({ find: \\\"1\\\", data: 'inserted'});\\n\\nprint(\\\"01 OK\\\");\"")
            }

            @Test
            internal fun should_send_script_start_date_in_body() {
                checkStartMessageBodyContains("\"startDate\":\"2023-03-21T11:17:33.337Z\"")
            }

            @Test
            internal fun should_send_script_tags_in_body() {
                checkStartMessageBodyContains("\"tags\":[\"myTag\"]")
            }

            private fun checkStartMessageBodyContains(subStringExpectedInBody: String) {
                val executionId = 12
                setupMockStartAnswer(executionId)
                buildDatamaintainWithMonitoringConfiguration(
                    scriptsPath = "src/test/resources/integration/ok",
                    tagsMatchers = setOf(TagMatcher(Tag("myTag"), listOf("src/test/resources/integration/ok/*"))),
                    clock = Clock.fixed(Instant.parse("2023-03-21T11:17:33.337Z"), ZoneId.systemDefault())
                ).updateDatabase()

                mockServerClient.verify(request()
                    .withPath("/v1/executions/$executionId/script/start")
                    .withMethod("PUT")
                    .withBody(subString(subStringExpectedInBody)))
            }
        }

        @Nested
        inner class ScriptExecutionStopMessage {
            @Test
            internal fun should_send_script_execution_stop_with_execution_id() {
                // When
                val executionId = 12
                setupMockStartAnswer(executionId)
                buildDatamaintainWithMonitoringConfiguration("src/test/resources/integration/ok").updateDatabase()

                // Then
                mockServerClient.verify(request().withPath("/v1/executions/$executionId/script/stop").withMethod("PUT"))
            }

            @Test
            internal fun should_send_script_checksum_in_body() {
                checkStopMessageBodyContains("\"checksum\":\"24403a1ac36cc57c3cf9857bd8c5f676\"")
            }

            @Test
            internal fun should_send_script_execution_duration_in_millis_in_body() {
                // When
                val executionId = 12
                setupMockStartAnswer(executionId)
                val report = buildDatamaintainWithMonitoringConfiguration(
                    scriptsPath = "src/test/resources/integration/ok"
                ).updateDatabase()
                val script1ExecutionDurationInMillis = report.executedScripts[0].executionDurationInMillis

                mockServerClient.verify(request()
                    .withPath("/v1/executions/$executionId/script/stop")
                    .withMethod("PUT")
                    .withBody(subString("\"executionDurationInMillis\":$script1ExecutionDurationInMillis")))
            }

            @Test
            internal fun should_send_script_execution_status_in_body() {
                checkStopMessageBodyContains("\"executionStatus\":\"OK\"")
            }

            @Test
            internal fun should_send_script_execution_output_in_body() {
                checkStopMessageBodyContains("\"executionOutput\":\"$fakeDriverScriptExecutionOutput\"")
            }

            private fun checkStopMessageBodyContains(subStringExpectedInBody: String) {
                val executionId = 12
                setupMockStartAnswer(executionId)
                buildDatamaintainWithMonitoringConfiguration(
                    scriptsPath = "src/test/resources/integration/ok"
                ).updateDatabase()

                mockServerClient.verify(request()
                    .withPath("/v1/executions/$executionId/script/stop")
                    .withMethod("PUT")
                    .withBody(subString(subStringExpectedInBody)))
            }
        }

        @Nested
        inner class ExecutionStopMessage {
            @Test
            internal fun should_send_script_execution_stop_with_execution_id() {
                // When
                val executionId = 12
                setupMockStartAnswer(executionId)
                buildDatamaintainWithMonitoringConfiguration("src/test/resources/integration/ok").updateDatabase()

                // Then
                mockServerClient.verify(request().withPath("/v1/executions/stop/$executionId").withMethod("PUT"))
            }

            @Test
            internal fun should_send_scripts_executed_number() {
                checkExecutionStopMessageBodyContains("\"scriptsExecutedNumber\":3")
            }

            private fun checkExecutionStopMessageBodyContains(subStringExpectedInBody: String) {
                val executionId = 12
                setupMockStartAnswer(executionId)
                buildDatamaintainWithMonitoringConfiguration(
                    scriptsPath = "src/test/resources/integration/ok"
                ).updateDatabase()

                mockServerClient.verify(request()
                    .withPath("/v1/executions/stop/$executionId")
                    .withMethod("PUT")
                    .withBody(subString(subStringExpectedInBody)))
            }
        }
    }

    private fun setupMockStartAnswer(executionId: ExecutionId = 42) {
        mockServerClient.`when`(
            request()
                .withMethod("POST")
                .withPath("/v1/executions/start")
        ).respond(response().withBody("{\"executionId\": $executionId}"))
    }

    private fun buildDatamaintainWithMonitoringConfiguration(
        scriptsPath: String = "",
        tagsMatchers: Set<TagMatcher> = setOf(),
        clock: Clock = Clock.system(ZoneId.systemDefault())
    ) = Datamaintain(
        DatamaintainConfig(
            scanner = DatamaintainScannerConfig(
                path = Paths.get(scriptsPath),
                tagsMatchers = tagsMatchers
            ),
            driverConfig = FakeDriverConfig(),
            monitoringConfiguration = MonitoringConfiguration(mockServerUrl)
        ),
        clock
    )
}