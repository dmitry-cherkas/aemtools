package com.aemtools.analysis.htl.callchain.elements

import com.aemtools.analysis.htl.callchain.typedescriptor.TypeDescriptor
import com.intellij.psi.PsiElement

/**
 * @author Dmytro_Troynikov
 */
class CallChain(val callChainSegments: List<CallChainSegment>) {

    /**
     * Find call chain element containing given [PsiElement].
     * @param element the psi element to look for
     * @return call chain element which contains given psi element, _null_ if no such element found
     */
    fun findChainElement(element: PsiElement): CallChainElement? {
        callChainSegments.forEach {
            it.chainElements().forEach {
                if (it.element == element) {
                    return it
                }
            }
        }
        return null
    }

    /**
     * Return last output type of this call chain.
     *
     * @return last output type descriptor
     */
    fun getLastOutputType(): TypeDescriptor? =
            callChainSegments.lastOrNull()?.outputType()

    companion object {
        private val EMPTY_CHAIN = CallChain(listOf())
        fun empty() = EMPTY_CHAIN
    }
}