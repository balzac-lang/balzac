package it.unica.tcs.conversion.converter

import com.google.inject.Inject
import it.unica.tcs.utils.ASTUtils
import org.eclipse.xtext.conversion.ValueConverterException
import org.eclipse.xtext.conversion.impl.AbstractLexerBasedConverter
import org.eclipse.xtext.nodemodel.INode

class DelayValueConverter extends AbstractLexerBasedConverter<Integer> {

	@Inject private IntUnderscoreValueConverter intConverter
	@Inject private ASTUtils astUtils
	
	override Integer toValue(String string, INode node) {
		
		if (string.contains("minutes")) {
			try {
				var min = intConverter.toValue(string.replaceAll("\\s*minutes",""),node)
				return astUtils.setRelativeDate(convertMinutes(min));
			}
			catch (NumberFormatException e) {
				throw new ValueConverterException("The given value '"+string+"' is too large.", node, e);
			}
		}
		else if (string.contains("hours")) {
			try {
				var min = intConverter.toValue(string.replaceAll("\\s*hours",""),node)
				return astUtils.setRelativeDate(convertHours(min));
			}
			catch (NumberFormatException e) {
				throw new ValueConverterException("The given value '"+string+"' is too large.", node, e);
			}
		}
		else if (string.contains("days")) {
			try {
				var min = intConverter.toValue(string.replaceAll("\\s*days",""),node)
				return astUtils.setRelativeDate(convertDays(min));
			}
			catch (NumberFormatException e) {
				throw new ValueConverterException("The given value '"+string+"' is too large.", node, e);
			}
		}
		else if (string.contains("blocks")) {
			try {
				var b = Short.valueOf(intConverter.toValue(string.replaceAll("\\s*blocks",""),node).toString)
				return Integer.valueOf(b);
			}
			catch (NumberFormatException e) {
				throw new ValueConverterException("The given value '"+string+"' is too large.", node, e);
			}
		}
		else
			throw new ValueConverterException("Couldn't convert input '" + string + "' to an short value.", node, null);
	}
	
	
	
	private def int convertMinutes(int min) {
		astUtils.safeCastUnsignedShort(((min*60) / 512 + (if (min % 512 == 0) 0 else 1))); 
	}
	
	private def int convertHours(int hours) {
		convertMinutes(hours*60)
	}
	
	private def int convertDays(int days) {
		convertHours(days*24) 
	}
	
}
