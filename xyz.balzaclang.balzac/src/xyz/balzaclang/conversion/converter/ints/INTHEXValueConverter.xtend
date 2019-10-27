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

package xyz.balzaclang.conversion.converter.ints

import org.eclipse.xtext.conversion.ValueConverterException
import org.eclipse.xtext.conversion.impl.AbstractLexerBasedConverter
import org.eclipse.xtext.nodemodel.INode
import org.eclipse.xtext.util.Strings

class INTHEXValueConverter extends AbstractLexerBasedConverter<Integer> {

    override String toEscapedString(Integer value) {
        return Integer.toString(value, 16);
    }

    override void assertValidValue(Integer value) {
        super.assertValidValue(value);
        if (value < 0)
            throw new ValueConverterException(getRuleName() + "-value may not be negative (value: " + value + ").",
                null, null);
    }

    override Integer toValue(String string, INode node) {
        if (Strings.isEmpty(string))
            throw new ValueConverterException("Couldn't convert empty string to an int value.", node, null);
        try {
            var intValue = Integer.parseInt(string, 16);
            return Integer.valueOf(intValue);
        } catch (NumberFormatException e) {
            throw new ValueConverterException("Couldn't convert '" + string + "' to an int value.", node, e);
        }
    }

}
