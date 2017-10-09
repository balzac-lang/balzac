/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.generator

import com.google.inject.Singleton
import it.unica.tcs.bitcoinTM.AndProtocolExpression
import it.unica.tcs.bitcoinTM.BooleanLiteral
import it.unica.tcs.bitcoinTM.HashLiteral
import it.unica.tcs.bitcoinTM.NumberLiteral
import it.unica.tcs.bitcoinTM.OrProtocolExpression
import it.unica.tcs.bitcoinTM.ProtocolArithmeticSigned
import it.unica.tcs.bitcoinTM.ProtocolBooleanNegation
import it.unica.tcs.bitcoinTM.ProtocolComparison
import it.unica.tcs.bitcoinTM.ProtocolEquals
import it.unica.tcs.bitcoinTM.ProtocolExpression
import it.unica.tcs.bitcoinTM.ProtocolMinus
import it.unica.tcs.bitcoinTM.ProtocolPlus
import it.unica.tcs.bitcoinTM.ProtocolTimes
import it.unica.tcs.bitcoinTM.StringLiteral
import it.unica.tcs.bitcoinTM.VariableReference
import it.unica.tcs.lib.utils.BitcoinUtils
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import it.unica.tcs.bitcoinTM.ProtocolDiv

@Singleton
class ProtocolExpressionGenerator {
	
//	@Inject private extension BitcoinTMTypeSystem
	
	def String compileProtocolExpression(ProtocolExpression exp) {
		compileProtocolExpressionInternal(exp/*.interpretSafe*/)
	}

	def private dispatch String compileProtocolExpressionInternal(ProtocolExpression exp) {
		'''/* ERROR: «NodeModelUtils.findActualNodeFor(exp).text.trim» */'''
	}
	
	def private dispatch String compileProtocolExpressionInternal(OrProtocolExpression exp) {
		'''(«exp.left.compileProtocolExpressionInternal»||«exp.right.compileProtocolExpressionInternal»)'''
	}
	
	def private dispatch String compileProtocolExpressionInternal(AndProtocolExpression exp) {
		'''(«exp.left.compileProtocolExpressionInternal»&&«exp.right.compileProtocolExpressionInternal»)'''
	}
	
	def private dispatch String compileProtocolExpressionInternal(ProtocolComparison exp) {
		'''(«exp.left.compileProtocolExpressionInternal»«exp.op»«exp.right.compileProtocolExpressionInternal»)'''
	}
	
	def private dispatch String compileProtocolExpressionInternal(ProtocolEquals exp) {
		'''(«exp.left.compileProtocolExpressionInternal».equals(«exp.right.compileProtocolExpressionInternal»))'''
	}
	
	def private dispatch String compileProtocolExpressionInternal(ProtocolPlus exp) {
		'''(«exp.left.compileProtocolExpressionInternal»+«exp.right.compileProtocolExpressionInternal»)'''
	}
	
	def private dispatch String compileProtocolExpressionInternal(ProtocolMinus exp) {
		'''(«exp.left.compileProtocolExpressionInternal»-«exp.right.compileProtocolExpressionInternal»)'''
	}
	
	def private dispatch String compileProtocolExpressionInternal(ProtocolTimes exp) {
		'''(«exp.left.compileProtocolExpressionInternal»*«exp.right.compileProtocolExpressionInternal»)'''
	}
	
	def private dispatch String compileProtocolExpressionInternal(ProtocolDiv exp) {
		'''(«exp.left.compileProtocolExpressionInternal»/«exp.right.compileProtocolExpressionInternal»)'''
	}
	
	def private dispatch String compileProtocolExpressionInternal(ProtocolBooleanNegation exp) {
		'''!«exp.exp.compileProtocolExpressionInternal»'''
	}
	
	def private dispatch String compileProtocolExpressionInternal(ProtocolArithmeticSigned exp) {
		'''-«exp.exp.compileProtocolExpressionInternal»'''
	}
	
	def private dispatch String compileProtocolExpressionInternal(NumberLiteral exp) {
		exp.value.toString
	}
	
	def private dispatch String compileProtocolExpressionInternal(StringLiteral exp) {
		'''"«exp.value»"'''
	}
	
	def private dispatch String compileProtocolExpressionInternal(BooleanLiteral exp) {
		exp.^true.toString
	}
	
	def private dispatch String compileProtocolExpressionInternal(HashLiteral exp) {
		'''Utils.HEX.decode("«BitcoinUtils.encode(exp.value)»")'''
	}
	
	def private dispatch String compileProtocolExpressionInternal(VariableReference exp) {
		exp.ref.name
	}
}