package com.aemtools.lang.common

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

/**
 * @author Dmytro Troynikov
 */


/**
 * Create info annotation in current annotation holder using given text range.
 *
 * @param range text range to which the highlight should be applied
 * @param textAttributesKey highlight style
 * @param message text message (`null` by default)
 *
 * @receiver [AnnotationHolder]
 */
fun AnnotationHolder.highlight(range: TextRange,
                               textAttributesKey: TextAttributesKey,
                               message: String? = null) : Unit {
    createInfoAnnotation(range, message).textAttributes = textAttributesKey
}

/**
 * Create info annotation in current annotation holder using given psi element.
 *
 * @param element the element to highlight
 * @param textAttributesKey highlight style
 * @param message text message (`null` by default)
 *
 * @receiver [AnnotationHolder]
 */
fun AnnotationHolder.highlight(element: PsiElement,
                                textAttributesKey: TextAttributesKey,
                                message: String? = null) : Unit {
    highlight(element.textRange, textAttributesKey, message)
}

/**
 * Create error annotation in current annotation holder using given psi element.
 *
 * @param element the element that should be marked with error
 * @param message error message (`null` by default)
 *
 * @receiver [AnnotationHolder]
 */
fun AnnotationHolder.error(element: PsiElement,
                           message: String? = null) : Unit{
    createErrorAnnotation(element, message)
}