package com.aemtools.analysis.htl.callchain.typedescriptor

import com.aemtools.completion.htl.model.ResolutionResult
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.psi.PsiElement

/**
 * Type descriptor compounded from the type descriptors.
 * @author Dmytro Troynikov
 */
class MergedTypeDescriptor(vararg val types: TypeDescriptor) : TypeDescriptor {
    override fun myVariants(): List<LookupElement> =
            types.flatMap { it.myVariants() }

    override fun documentation(): String? =
            types.map { it.documentation() }
                    .filterNotNull()
                    .firstOrNull()

    override fun referencedElement(): PsiElement? =
            types.map { it.referencedElement() }
                    .filterNotNull()
                    .firstOrNull()

    override fun subtype(identifier: String): TypeDescriptor =
            types.map { it.subtype(identifier) }
                    .find { it !is EmptyTypeDescriptor }
                    ?: EmptyTypeDescriptor()

    override fun name(): String = types.map { it.name() }
            .find { it.isNotBlank() }
            ?: ""

    override fun isArray(): Boolean = types.any { it.isArray() }

    override fun isIterable(): Boolean = types.any { it.isIterable() }
    override fun isMap(): Boolean = types.any { it.isMap() }

    override fun asResolutionResult(): ResolutionResult = types
            .map { it.asResolutionResult() }
            .reduce { acc, next -> acc + next }

    operator fun plus(other: TypeDescriptor): TypeDescriptor =
            MergedTypeDescriptor(*this.types, other)

}