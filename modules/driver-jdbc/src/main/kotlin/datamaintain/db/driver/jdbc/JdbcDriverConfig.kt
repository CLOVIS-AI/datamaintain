package datamaintain.db.driver.jdbc

import datamaintain.core.config.ConfigKey
import datamaintain.core.config.getProperty
import datamaintain.core.db.driver.DatamaintainDriverConfig
import mu.KotlinLogging
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

private val logger = KotlinLogging.logger {}

data class JdbcDriverConfig @JvmOverloads constructor(
        val jdbcUri: String,
        val tmpFilePath: Path = Paths.get(JdbcConfigKey.DB_JDBC_TMP_PATH.default!!),
        val clientPath: Path = Paths.get(JdbcConfigKey.DB_JDBC_CLIENT_PATH.default!!),
        val printOutput: Boolean = JdbcConfigKey.DB_JDBC_PRINT_OUTPUT.default!!.toBoolean(),
        val saveOutput: Boolean = JdbcConfigKey.DB_JDBC_SAVE_OUTPUT.default!!.toBoolean()
) : DatamaintainDriverConfig {
    companion object {
        @JvmStatic
        fun buildConfig(props: Properties): JdbcDriverConfig {
            ConfigKey.overrideBySystemProperties(props, JdbcConfigKey.values().asList())
            return JdbcDriverConfig(
                    props.getProperty(JdbcConfigKey.DB_JDBC_URI),
                    props.getProperty(JdbcConfigKey.DB_JDBC_TMP_PATH).let { Paths.get(it) },
                    props.getProperty(JdbcConfigKey.DB_JDBC_CLIENT_PATH).let { Paths.get(it) },
                    props.getProperty(JdbcConfigKey.DB_JDBC_PRINT_OUTPUT).toBoolean(),
                    props.getProperty(JdbcConfigKey.DB_JDBC_SAVE_OUTPUT).toBoolean())
        }
    }

    override fun toDriver() = JdbcDriver(
            ConnectionString.buildConnectionString(jdbcUri),
            tmpFilePath,
            clientPath,
            printOutput,
            saveOutput)

    override fun log() {
        logger.info { "JDBC driver configuration: " }
        logger.info { "- jdbc uri -> $jdbcUri" }
        logger.info { "- jdbc tmp file -> $tmpFilePath" }
        logger.info { "- jdbc client -> $clientPath" }
        logger.info { "- jdbc print output -> $printOutput" }
        logger.info { "- jdbc save output -> $saveOutput" }
        logger.info { "" }
    }
}

enum class JdbcConfigKey(
        override val key: String,
        override val default: String? = null
) : ConfigKey {
    DB_JDBC_URI("db.jdbc.uri"),
    DB_JDBC_TMP_PATH("db.jdbc.tmp.path", "/tmp/datamaintain.tmp"),
    DB_JDBC_CLIENT_PATH("db.jdbc.client.path"),
    DB_JDBC_PRINT_OUTPUT("db.jdbc.print.output", "false"),
    DB_JDBC_SAVE_OUTPUT("db.jdbc.save.output", "false"),
}