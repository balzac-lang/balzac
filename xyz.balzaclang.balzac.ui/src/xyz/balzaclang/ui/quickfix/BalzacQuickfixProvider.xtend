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

package xyz.balzaclang.ui.quickfix

import org.eclipse.xtext.ui.editor.quickfix.DefaultQuickfixProvider
import org.eclipse.xtext.ui.editor.quickfix.Fix
import org.eclipse.xtext.ui.editor.quickfix.IssueResolutionAcceptor
import xyz.balzaclang.validation.BalzacValidatorCodes
import org.eclipse.xtext.validation.Issue
import org.eclipse.xtext.ui.editor.model.edit.ISemanticModification
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext
import xyz.balzaclang.balzac.ScriptParameter
import xyz.balzaclang.balzac.TransactionParameter
import xyz.balzaclang.balzac.Script
import xyz.balzaclang.balzac.Transaction
import xyz.balzaclang.balzac.Versig

class BalzacQuickfixProvider extends DefaultQuickfixProvider {

    @Fix(BalzacValidatorCodes.WARNING_USELESS_BETWEEN)
    def replaceBetween(Issue issue, IssueResolutionAcceptor acceptor) {
        acceptor.accept(issue, "Replace 'between' expression", "Replace 'between' expression", 'upcase.png') [
            context |
            val solution = issue.data.get(0)
            val xtextDocument = context.xtextDocument
            xtextDocument.replace(issue.offset,  issue.length, solution)
        ]
    }

    @Fix(BalzacValidatorCodes.WARNING_UNUSED_PARAM)
    def removeUnusedParam(Issue issue, IssueResolutionAcceptor acceptor) {
        acceptor.accept(issue, "Remove unused parameter", "Remove unused parameter", 'upcase.png', new ISemanticModification(){
            override apply(EObject param, IModificationContext context) throws Exception {
                if (param instanceof ScriptParameter) {
                    val script = param.eContainer as Script
                    script.params.remove(param)
                }
                else if (param instanceof TransactionParameter) {
                    val transaction = param.eContainer as Transaction
                    transaction.params.remove(param)
                }
                else
                    throw new IllegalStateException("Unexpected type "+param.class.name)
            }
        })
    }

    @Fix(BalzacValidatorCodes.WARNING_VERSIG_DUPLICATED_PUBKEY)
    def removeDuplicatedPublicKeys(Issue issue, IssueResolutionAcceptor acceptor) {
        acceptor.accept(issue, "Remove this key", "Remove this key", 'upcase.png', new ISemanticModification(){
            override apply(EObject versig, IModificationContext context) throws Exception {
                if (versig instanceof Versig) {
                    val index = Integer.parseInt(issue.data.get(0))
                    versig.pubkeys.remove(index)
                }
                else
                    throw new IllegalStateException("Unexpected type "+versig.class.name)
            }
        })
    }
}
