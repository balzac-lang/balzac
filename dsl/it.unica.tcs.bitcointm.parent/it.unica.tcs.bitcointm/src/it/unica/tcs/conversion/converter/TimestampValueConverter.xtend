/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.conversion.converter

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import org.eclipse.xtext.conversion.ValueConverterException
import org.eclipse.xtext.conversion.impl.AbstractLexerBasedConverter
import org.eclipse.xtext.nodemodel.INode

class TimestampValueConverter extends AbstractLexerBasedConverter<Integer> {

	override Integer toValue(String s, INode node) {

		var OffsetDateTime date = null;
			
		try {				
			date = OffsetDateTime.parse(s, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
//			System.out.println(1);
		}
		catch (DateTimeParseException e) {}
		
		try {				
			date = LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME).atOffset(ZoneOffset.UTC);
//			System.out.println(2);
		}
		catch (DateTimeParseException e) {}
		
		try {				
			date = LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay().atOffset(ZoneOffset.UTC);
//			System.out.println(3);
		}
		catch (DateTimeParseException e) {}
		
		if (date===null) {
			throw new ValueConverterException(
				"Couldn't parse the input '" + s + "' to a valid data. 
				The format must be like '2011-12-03' '2011-12-03T10:15', '2011-12-03T10:15:00', or '2011-12-03T10:15:30+01:00'", node, null);
		}
		else
			return Long.valueOf(date.toEpochSecond).intValue
	}
}
