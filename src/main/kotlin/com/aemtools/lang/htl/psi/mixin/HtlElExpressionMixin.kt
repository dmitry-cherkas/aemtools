package com.aemtools.lang.htl.psi.mixin

import com.aemtools.completion.util.findChildrenByType
import com.aemtools.lang.htl.psi.HtlContextExpression
import com.aemtools.lang.htl.psi.mixin.model.HtlOptionModel
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

/**
 * @author Dmytro Troynikov
 */
abstract class HtlElExpressionMixin(node: ASTNode) : ASTWrapperPsiElement(node) {

    fun getOptions() : List<HtlOptionModel> =
            this.findChildrenByClass(HtlContextExpression::class.java)
                    .map(::HtlOptionModel)

    fun getMainPropertyAccess() : PropertyAccessMixin? =
            this.findChildrenByType(PropertyAccessMixin::class.java)
                    .firstOrNull()

}