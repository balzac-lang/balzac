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

package xyz.balzaclang.conversion.converter.longs

import org.eclipse.xtext.conversion.ValueConverterException
import org.eclipse.xtext.conversion.impl.AbstractLexerBasedConverter
import org.eclipse.xtext.nodemodel.INode
import org.eclipse.xtext.util.Strings

class LONGHEXValueConverter extends AbstractLexerBasedConverter<Long> {

    override String toEscapedString(Long value) {
        return Long.toString(value, 16);
    }

    override void assertValidValue(Long value) {
        super.assertValidValue(value);
        if (value < 0)
            throw new ValueConverterException(getRuleName() + "-value may not be negative (value: " + value + ").",
                null, null);
    }

    override Long toValue(String string, INode node) {
        if (Strings.isEmpty(string))
            throw new ValueConverterException("Couldn't convert empty string to an int value.", node, null);
        try {
            return Long.parseLong(string, 16);
        } catch (NumberFormatException e) {
            throw new ValueConverterException("Couldn't convert '" + string + "' to an int value.", node, e);
        }
    }

}
