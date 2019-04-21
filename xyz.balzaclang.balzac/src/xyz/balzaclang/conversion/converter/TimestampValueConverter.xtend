/*
 * Copyright 2017 Nicola Atzei
 */

package xyz.balzaclang.conversion.converter

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import org.eclipse.xtext.conversion.ValueConverterException
import org.eclipse.xtext.conversion.impl.AbstractLexerBasedConverter
import org.eclipse.xtext.nodemodel.INode

class TimestampValueConverter extends AbstractLexerBasedConverter<Long> {

    override Long toValue(String s, INode node) {

        var OffsetDateTime date = null;

        try {
            date = OffsetDateTime.parse(s, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
        catch (DateTimeParseException e) {}

        try {
            date = LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME).atOffset(ZoneOffset.UTC);
        }
        catch (DateTimeParseException e) {}

        try {
            date = LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay().atOffset(ZoneOffset.UTC);
        }
        catch (DateTimeParseException e) {}

        if (date===null) {
            throw new ValueConverterException(
                '''
                Invalid date '«s»'.

                Some examples of valid formats are:
                    2011-12-03
                    2011-12-03T10:15
                    2011-12-03T10:15:00
                    2011-12-03T10:15:30+01:00''',
                 node, null);
        }
        else
            return Long.valueOf(date.toEpochSecond)
    }
}
