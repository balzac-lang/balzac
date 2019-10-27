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
import org.eclipse.xtext.conversion.impl.INTValueConverter
import org.eclipse.xtext.nodemodel.INode
import org.eclipse.xtext.util.Strings

class IntUnderscoreValueConverter extends INTValueConverter {

    override Integer toValue(String string, INode node) {
        if (Strings.isEmpty(string))
            throw new ValueConverterException("Couldn't convert empty string to an int value.", node, null);
        var withoutUnderscore = string.replace("_", "");
        if (Strings.isEmpty(withoutUnderscore))
            throw new ValueConverterException("Couldn't convert input '" + string + "' to an int value.", node, null);
        return super.toValue(withoutUnderscore, node);
    }
}
