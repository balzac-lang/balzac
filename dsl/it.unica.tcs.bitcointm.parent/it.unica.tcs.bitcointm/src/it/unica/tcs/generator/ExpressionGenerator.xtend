/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.generator

import com.google.inject.Inject
import com.google.inject.Singleton
import it.unica.tcs.bitcoinTM.AndExpression
import it.unica.tcs.bitcoinTM.ArithmeticSigned
import it.unica.tcs.bitcoinTM.Between
import it.unica.tcs.bitcoinTM.BooleanLiteral
import it.unica.tcs.bitcoinTM.BooleanNegation
import it.unica.tcs.bitcoinTM.Comparison
import it.unica.tcs.bitcoinTM.Equals
import it.unica.tcs.bitcoinTM.Expression
import it.unica.tcs.bitcoinTM.HashLiteral
import it.unica.tcs.bitcoinTM.Max
import it.unica.tcs.bitcoinTM.Min
import it.unica.tcs.bitcoinTM.Minus
import it.unica.tcs.bitcoinTM.NumberLiteral
import it.unica.tcs.bitcoinTM.OrExpression
import it.unica.tcs.bitcoinTM.Plus
import it.unica.tcs.bitcoinTM.StringLiteral
import it.unica.tcs.bitcoinTM.VariableReference
import it.unica.tcs.bitcointm.lib.utils.BitcoinJUtils
import it.unica.tcs.xsemantics.BitcoinTMTypeSystem
import org.eclipse.xtext.nodemodel.util.NodeModelUtils

@Singleton
class ExpressionGenerator {
	
	@Inject private extension BitcoinTMTypeSystem

	def String compileExpression(Expression exp) {
		compileExpressionInternal(exp.interpretSafe)
	}

	def private dispatch String compileExpressionInternal(Expression exp) {
		'''/* ERROR: «NodeModelUtils.findActualNodeFor(exp).text.trim» */'''
	}
	
	def private dispatch String compileExpressionInternal(OrExpression exp) {
		'''(«exp.left.interpretSafe.compileExpressionInternal»||«exp.right.interpretSafe.compileExpressionInternal»)'''
	}
	
	def private dispatch String compileExpressionInternal(AndExpression exp) {
		'''(«exp.left.interpretSafe.compileExpressionInternal»&&«exp.right.interpretSafe.compileExpressionInternal»)'''
	}
	
	def private dispatch String compileExpressionInternal(Comparison exp) {
		'''(«exp.left.interpretSafe.compileExpressionInternal»«exp.op»«exp.right.interpretSafe.compileExpressionInternal»)'''
	}
	
	def private dispatch String compileExpressionInternal(Equals exp) {
		'''(«exp.left.interpretSafe.compileExpressionInternal».equals(«exp.right.interpretSafe.compileExpressionInternal»))'''
	}
	
	def private dispatch String compileExpressionInternal(Plus exp) {
		'''(«exp.left.interpretSafe.compileExpressionInternal»+«exp.right.interpretSafe.compileExpressionInternal»)'''
	}
	
	def private dispatch String compileExpressionInternal(Minus exp) {
		'''(«exp.left.interpretSafe.compileExpressionInternal»-«exp.right.interpretSafe.compileExpressionInternal»)'''
	}
	
	def private dispatch String compileExpressionInternal(Min exp) {
		'''(«exp.left.interpretSafe.compileExpressionInternal»<«exp.left.interpretSafe.compileExpressionInternal»? «exp.left.interpretSafe.compileExpressionInternal»: «exp.right.interpretSafe.compileExpressionInternal»)'''
	}

	def private dispatch String compileExpressionInternal(Max exp) {
		'''(«exp.left.interpretSafe.compileExpressionInternal»>«exp.left.interpretSafe.compileExpressionInternal»? «exp.left.interpretSafe.compileExpressionInternal»: «exp.right.interpretSafe.compileExpressionInternal»)'''
	}

	def private dispatch String compileExpressionInternal(Between exp) {
		'''(«exp.left.interpretSafe.compileExpressionInternal» <= «exp.value.interpretSafe.compileExpressionInternal» && «exp.value.interpretSafe.compileExpressionInternal» < «exp.right.interpretSafe.compileExpressionInternal»)'''
	}
	
	def private dispatch String compileExpressionInternal(BooleanNegation exp) {
		'''!«exp.exp.interpretSafe.compileExpressionInternal»'''
	}
	
	def private dispatch String compileExpressionInternal(ArithmeticSigned exp) {
		'''-«exp.exp.interpretSafe.compileExpressionInternal»'''
	}
	
	def private dispatch String compileExpressionInternal(NumberLiteral exp) {
		exp.value.toString
	}
	
	def private dispatch String compileExpressionInternal(StringLiteral exp) {
		'''"«exp.value»"'''
	}
	
	def private dispatch String compileExpressionInternal(BooleanLiteral exp) {
		exp.^true.toString
	}
	
	def private dispatch String compileExpressionInternal(HashLiteral exp) {
		'''Utils.HEX.decode("«BitcoinJUtils.encode(exp.value)»")'''
	}
	
	def private dispatch String compileExpressionInternal(VariableReference exp) {
		exp.ref.name
	}
}