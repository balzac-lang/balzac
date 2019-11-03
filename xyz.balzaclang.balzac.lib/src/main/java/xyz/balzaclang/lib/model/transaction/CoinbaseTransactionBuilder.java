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

package xyz.balzaclang.lib.model.transaction;

import xyz.balzaclang.lib.model.NetworkType;
import xyz.balzaclang.lib.model.script.InputScript;

public class CoinbaseTransactionBuilder extends TransactionBuilder {

    private static final long serialVersionUID = 1L;

    public CoinbaseTransactionBuilder(NetworkType params) {
        super(params);
    }

    /*
     * This override is changing the method visibility.
     */
    @Override
    public TransactionBuilder addInput(InputScript inputScript) {
        return super.addInput(inputScript);
    }
}
