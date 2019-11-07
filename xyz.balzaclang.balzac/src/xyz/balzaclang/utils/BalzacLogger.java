/*
 * Copyright 2019 Nicola Atzei
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
package xyz.balzaclang.utils;

import org.slf4j.Logger;
import org.slf4j.Marker;

public class BalzacLogger implements Logger {

    private final Logger logger;

    private String prefix = "  ";
    private int prefixCount = 0;

    public BalzacLogger(Logger logger) {
        this.logger = logger;
    }

    public Logger increasePrefixCount() {
        prefixCount++;
        return this;
    }

    public Logger decreasePrefixCount() {
        prefixCount--;
        return this;
    }

    private String prependPrefix(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<prefixCount; i++)
            sb.append(prefix);
        return sb.append(str).toString();
    }

    @Override
    public void debug(String arg0) {
        logger.debug(prependPrefix(arg0));
    }

    @Override
    public void debug(String arg0, Object arg1) {
        logger.debug(prependPrefix(arg0), arg1);
    }

    @Override
    public void debug(String arg0, Object... arg1) {
        logger.debug(prependPrefix(arg0), arg1);
    }

    @Override
    public void debug(String arg0, Throwable arg1) {
        logger.debug(prependPrefix(arg0), arg1);
    }

    @Override
    public void debug(Marker arg0, String arg1) {
        logger.debug(arg0, prependPrefix(arg1));
    }

    @Override
    public void debug(String arg0, Object arg1, Object arg2) {
        logger.debug(prependPrefix(arg0), arg1, arg2);
    }

    @Override
    public void debug(Marker arg0, String arg1, Object arg2) {
        logger.debug(arg0, prependPrefix(arg1), arg2);
    }

    @Override
    public void debug(Marker arg0, String arg1, Object... arg2) {
        logger.debug(arg0, prependPrefix(arg1), arg2);
    }

    @Override
    public void debug(Marker arg0, String arg1, Throwable arg2) {
        logger.debug(arg0, prependPrefix(arg1), arg2);
    }

    @Override
    public void debug(Marker arg0, String arg1, Object arg2, Object arg3) {
        logger.debug(arg0, prependPrefix(arg1), arg2, arg3);
    }

    @Override
    public void error(String arg0) {
        logger.error(prependPrefix(arg0));
    }

    @Override
    public void error(String arg0, Object arg1) {
        logger.error(prependPrefix(arg0), arg1);
    }

    @Override
    public void error(String arg0, Object... arg1) {
        logger.error(prependPrefix(arg0), arg1);
    }

    @Override
    public void error(String arg0, Throwable arg1) {
        logger.error(prependPrefix(arg0), arg1);
    }

    @Override
    public void error(Marker arg0, String arg1) {
        logger.error(arg0, prependPrefix(arg1));
    }

    @Override
    public void error(String arg0, Object arg1, Object arg2) {
        logger.error(prependPrefix(arg0), arg1, arg2);
    }

    @Override
    public void error(Marker arg0, String arg1, Object arg2) {
        logger.error(arg0, prependPrefix(arg1), arg2);
    }

    @Override
    public void error(Marker arg0, String arg1, Object... arg2) {
        logger.error(arg0, prependPrefix(arg1), arg2);
    }

    @Override
    public void error(Marker arg0, String arg1, Throwable arg2) {
        logger.error(arg0, prependPrefix(arg1), arg2);
    }

    @Override
    public void error(Marker arg0, String arg1, Object arg2, Object arg3) {
        logger.error(arg0, prependPrefix(arg1), arg2, arg3);
    }

    @Override
    public String getName() {
        return logger.getName();
    }

    @Override
    public void info(String arg0) {
        logger.info(prependPrefix(arg0));
    }

    @Override
    public void info(String arg0, Object arg1) {
        logger.info(prependPrefix(arg0), arg1);
    }

    @Override
    public void info(String arg0, Object... arg1) {
        logger.info(prependPrefix(arg0), arg1);
    }

    @Override
    public void info(String arg0, Throwable arg1) {
        logger.info(prependPrefix(arg0), arg1);
    }

    @Override
    public void info(Marker arg0, String arg1) {
        logger.info(arg0, prependPrefix(arg1));
    }

    @Override
    public void info(String arg0, Object arg1, Object arg2) {
        logger.info(prependPrefix(arg0), arg1, arg2);
    }

    @Override
    public void info(Marker arg0, String arg1, Object arg2) {
        logger.info(arg0, prependPrefix(arg1), arg2);
    }

    @Override
    public void info(Marker arg0, String arg1, Object... arg2) {
        logger.info(arg0, prependPrefix(arg1), arg2);
    }

    @Override
    public void info(Marker arg0, String arg1, Throwable arg2) {
        logger.info(arg0, prependPrefix(arg1), arg2);
    }

    @Override
    public void info(Marker arg0, String arg1, Object arg2, Object arg3) {
        logger.info(arg0, prependPrefix(arg1), arg2, arg3);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public boolean isDebugEnabled(Marker arg0) {
        return logger.isDebugEnabled(arg0);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public boolean isErrorEnabled(Marker arg0) {
        return logger.isErrorEnabled(arg0);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public boolean isInfoEnabled(Marker arg0) {
        return logger.isInfoEnabled(arg0);
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public boolean isTraceEnabled(Marker arg0) {
        return logger.isTraceEnabled(arg0);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public boolean isWarnEnabled(Marker arg0) {
        return logger.isWarnEnabled(arg0);
    }

    @Override
    public void trace(String arg0) {
        logger.trace(prependPrefix(arg0));
    }

    @Override
    public void trace(String arg0, Object arg1) {
        logger.trace(prependPrefix(arg0), arg1);
    }

    @Override
    public void trace(String arg0, Object... arg1) {
        logger.trace(prependPrefix(arg0), arg1);
    }

    @Override
    public void trace(String arg0, Throwable arg1) {
        logger.trace(prependPrefix(arg0), arg1);
    }

    @Override
    public void trace(Marker arg0, String arg1) {
        logger.trace(arg0, prependPrefix(arg1));
    }

    @Override
    public void trace(String arg0, Object arg1, Object arg2) {
        logger.trace(prependPrefix(arg0), arg1, arg2);
    }

    @Override
    public void trace(Marker arg0, String arg1, Object arg2) {
        logger.trace(arg0, prependPrefix(arg1), arg2);
    }

    @Override
    public void trace(Marker arg0, String arg1, Object... arg2) {
        logger.trace(arg0, prependPrefix(arg1), arg2);
    }

    @Override
    public void trace(Marker arg0, String arg1, Throwable arg2) {
        logger.trace(arg0, prependPrefix(arg1), arg2);
    }

    @Override
    public void trace(Marker arg0, String arg1, Object arg2, Object arg3) {
        logger.trace(arg0, prependPrefix(arg1), arg2, arg3);
    }

    @Override
    public void warn(String arg0) {
        logger.warn(prependPrefix(arg0));
    }

    @Override
    public void warn(String arg0, Object arg1) {
        logger.warn(prependPrefix(arg0), arg1);
    }

    @Override
    public void warn(String arg0, Object... arg1) {
        logger.warn(prependPrefix(arg0), arg1);
    }

    @Override
    public void warn(String arg0, Throwable arg1) {
        logger.warn(prependPrefix(arg0), arg1);
    }

    @Override
    public void warn(Marker arg0, String arg1) {
        logger.warn(arg0, prependPrefix(arg1));
    }

    @Override
    public void warn(String arg0, Object arg1, Object arg2) {
        logger.warn(prependPrefix(arg0), arg1, arg2);
    }

    @Override
    public void warn(Marker arg0, String arg1, Object arg2) {
        logger.warn(arg0, prependPrefix(arg1), arg2);
    }

    @Override
    public void warn(Marker arg0, String arg1, Object... arg2) {
        logger.warn(arg0, prependPrefix(arg1), arg2);
    }

    @Override
    public void warn(Marker arg0, String arg1, Throwable arg2) {
        logger.warn(arg0, prependPrefix(arg1), arg2);
    }

    @Override
    public void warn(Marker arg0, String arg1, Object arg2, Object arg3) {
        logger.warn(arg0, prependPrefix(arg1), arg2, arg3);
    }
}
