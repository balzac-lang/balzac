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

package xyz.balzaclang.conversion.converter

import com.google.inject.Inject
import xyz.balzaclang.conversion.converter.longs.LONGHEXValueConverter
import xyz.balzaclang.conversion.converter.longs.LongUnderscoreValueConverter
import org.eclipse.xtext.conversion.ValueConverterException
import org.eclipse.xtext.conversion.impl.AbstractValueConverter
import org.eclipse.xtext.nodemodel.INode

class NumberValueConverter extends AbstractValueConverter<Long> {

    @Inject LongUnderscoreValueConverter intConverter
    @Inject LONGHEXValueConverter hexConverter

    override Long toValue(String string, INode node) {
        if (string.startsWith("0x") || string.startsWith("0X")) {
            return hexConverter.toValue(string.substring(2), node)
        }
        else {
            return intConverter.toValue(string, node)
        }
    }

    override toString(Long value) throws ValueConverterException {
        return value.toString
    }

}
