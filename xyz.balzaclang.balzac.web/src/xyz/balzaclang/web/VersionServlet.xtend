/*
 * Copyright 2019 Nicola Atzei
 */
package xyz.balzaclang.web

import com.google.gson.Gson
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Optional
import javax.servlet.ServletException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.eclipse.xtend.lib.annotations.Accessors
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@WebServlet(
    name = 'Version',
    description = "Provide balzac information about the version, commit, build of the deployed container.",
    urlPatterns = #['/version'],
    loadOnStartup = 1
)
class VersionServlet extends HttpServlet {

    @Accessors
    private static class Version {
        val String container
        val String version
        val String commit
        val String build
        val String deployedAt = now
    }

    val static Logger logger = LoggerFactory.getLogger(WebUtilsServlet)
    val static dateFormat =  DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
    val static now = LocalDateTime.now(ZoneId.of("UTC")).withNano(0).format(DateTimeFormatter.ISO_DATE_TIME)

    val Gson gson = new Gson
    var Version version

    override init() throws ServletException {
        val containerSeparator = Optional.empty() -> Optional.of("##")
        val versionSeparator =  Optional.of("##") -> Optional.of("+")
        val buildSeparator = Optional.of("+") -> Optional.empty
        val commitSeparator = Optional.empty -> Optional.of(".")
        val buildTimeSeparator = Optional.of(".") -> Optional.empty

        val warName = new File(getServletContext().getRealPath("/")).getName()
        logger.info("War name: {}", warName)
        logger.info("1: {}", "WebRoot".indexOf("."))

        val container = warName.substring(containerSeparator, "unknown")
        val version = warName.substring(versionSeparator, "unknown")
        val build = warName.substring(buildSeparator, "unknown")
        val commit = build.substring(commitSeparator, "unknown")
        var buildTime = build.substring(buildTimeSeparator, "unknown")

        if (buildTime != "unknown") {
            try {
                buildTime = LocalDateTime.parse(buildTime, dateFormat).format(DateTimeFormatter.ISO_DATE_TIME)
            }
            catch(Exception e){
                logger.warn("Unable to parse the build time from '{}'", buildTime)
            }
        }

        version = new Version(container, version, commit, buildTime)

        logger.info('''
        Balzac
         - container:  «this.version.container»
         - version:    «this.version.version»
         - commit:     «this.version.commit»
         - build:      «this.version.build»
         - deployedAt: «this.version.deployedAt»
        ''')
    }

    private def String substring(String str, Pair<Optional<String>,Optional<String>> delimiters, String defaultStr) {
        val leftIndex = delimiters.key.map( leftDel | str.indexOf(leftDel)).orElse(0)
        val leftDelLength = delimiters.key.map( leftDel | leftDel.length).orElse(0)
        val rightIndex = delimiters.value.map( rightDel | str.indexOf(rightDel)).orElse(str.length)
        if (leftIndex != -1 && rightIndex != -1 && leftIndex < rightIndex) {
            return str.substring(leftIndex + leftDelLength, rightIndex)
        }
        defaultStr
    }

    override protected doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
        if (req.requestURI.contains("/version")) {
            response.contentType = 'application/json'
            response.status = HttpServletResponse.SC_OK
            gson.toJson(version, response.writer)
            return
        }
        response.status = HttpServletResponse.SC_NOT_FOUND
    }
}
