/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.generator

import com.google.inject.Inject
import com.google.inject.Singleton
import it.unica.tcs.bitcoinTM.Expression
import it.unica.tcs.utils.ASTUtils
import org.eclipse.xtext.nodemodel.util.NodeModelUtils

@Singleton
class ExpressionGenerator {
	
	@Inject private extension ASTUtils
	
	def String compileExpression(Expression exp) {
		compileExpressionInternal(exp/*.interpretSafe*/)
	}

	def private dispatch String compileExpressionInternal(Expression exp) {
		'''/* ERROR: «NodeModelUtils.findActualNodeFor(exp).text.trim» */'''
	}
	
//	def private dispatch String compileExpressionInternal(OrExpression exp) {
//		'''(«exp.left.compileExpressionInternal»||«exp.right.compileExpressionInternal»)'''
//	}
//	
//	def private dispatch String compileExpressionInternal(AndExpression exp) {
//		'''(«exp.left.compileExpressionInternal»&&«exp.right.compileExpressionInternal»)'''
//	}
//	
//	def private dispatch String compileExpressionInternal(Comparison exp) {
//		'''(«exp.left.compileExpressionInternal»«exp.op»«exp.right.compileExpressionInternal»)'''
//	}
//	
//	def private dispatch String compileExpressionInternal(Equals exp) {
//		'''(«exp.left.compileExpressionInternal».equals(«exp.right.compileExpressionInternal»))'''
//	}
//	
//	def private dispatch String compileExpressionInternal(Plus exp) {
//		'''(«exp.left.compileExpressionInternal»+«exp.right.compileExpressionInternal»)'''
//	}
//	
//	def private dispatch String compileExpressionInternal(Minus exp) {
//		'''(«exp.left.compileExpressionInternal»-«exp.right.compileExpressionInternal»)'''
//	}
//	
//	def private dispatch String compileExpressionInternal(Times exp) {
//		'''(«exp.left.compileExpressionInternal»*«exp.right.compileExpressionInternal»)'''
//	}
//	
//	def private dispatch String compileExpressionInternal(Div exp) {
//		'''(«exp.left.compileExpressionInternal»/«exp.right.compileExpressionInternal»)'''
//	}
//	
//	def private dispatch String compileExpressionInternal(BooleanNegation exp) {
//		'''!«exp.exp.compileExpressionInternal»'''
//	}
//	
//	def private dispatch String compileExpressionInternal(ArithmeticSigned exp) {
//		'''-«exp.exp.compileExpressionInternal»'''
//	}
//	
//	def private dispatch String compileExpressionInternal(NumberLiteral exp) {
//		exp.value.toString
//	}
//	
//	def private dispatch String compileExpressionInternal(StringLiteral exp) {
//		'''"«exp.value»"'''
//	}
//	
//	def private dispatch String compileExpressionInternal(BooleanLiteral exp) {
//		exp.^true.toString
//	}
//	
//	def private dispatch String compileExpressionInternal(HashLiteral exp) {
//		'''Utils.HEX.decode("«BitcoinUtils.encode(exp.value)»")'''
//	}
//	
//	def private dispatch String compileExpressionInternal(DeclarationReference exp) {
//		exp.ref.name
//	}
}