/*
 * Copyright 2024 Nicola Atzei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.balzaclang.web

import com.google.gson.Gson
import jakarta.servlet.ServletException
import jakarta.servlet.annotation.WebServlet
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.io.IOException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Properties
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
        val String javaVersion
        val String version
        val String commit
        val String build
        val String deployedAt = now
    }

    val static Logger logger = LoggerFactory.getLogger(VersionServlet)
    val static inputDateTimeFormatter =  DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
    val static outputDateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    val static now = Instant.now.atZone(ZoneOffset.UTC).withNano(0).format(outputDateTimeFormatter)

    val Gson gson = new Gson
    var Version version

    override init() throws ServletException {

        val properties = new Properties(System.properties)
        val propertiesInput = VersionServlet.classLoader.getResourceAsStream("info.properties")
        properties.load(propertiesInput)

        val javaVersion = properties.getProperty("java.version", "unknown")
        val version = properties.getProperty("build.version", "unknown")
        val commit = properties.getProperty("build.commit", "unknown")
        var buildTime = properties.getProperty("build.timestamp", "unknown")

        if (buildTime != "unknown") {
            try {
                buildTime = LocalDateTime.parse(buildTime, VersionServlet.inputDateTimeFormatter).atZone(ZoneOffset.UTC).format(outputDateTimeFormatter)
            }
            catch(Exception e){
                logger.warn('''Unable to parse the build time from value '«buildTime»' ''', e)
            }
        }

        version = new Version(javaVersion, version, commit, buildTime)

        logger.info('''
        Balzac
         - java:       «this.version.javaVersion»
         - version:    «this.version.version»
         - commit:     «this.version.commit»
         - build:      «this.version.build»
         - deployedAt: «this.version.deployedAt»
        ''')
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
