package com.aemtools.documentation.widget

import com.aemtools.completion.model.WidgetDoc
import com.aemtools.completion.model.WidgetMember
import com.aemtools.completion.model.psi.SelectedAttribute
import com.aemtools.completion.util.WidgetDefinitionUtil
import com.aemtools.constant.const
import com.aemtools.service.ServiceFacade
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement

/**
 * Documentation provider for `dialog.xml` files.
 *
 * @author Dmytro_Troynikov
 */
open class WidgetDocumentationProvider : AbstractDocumentationProvider() {

    val widgetRepository = ServiceFacade.getWidgetRepository()

    fun acceptGenerateDoc(element: PsiElement): Boolean {
        if (element.containingFile
                .originalFile
                .name != const.DIALOG_XML) {
            return false
        }
        return true
    }

    override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
        if (originalElement == null || !acceptGenerateDoc(originalElement)) {
            return null
        }

        val widgetDefinition = WidgetDefinitionUtil.extract(originalElement) ?: return null

        val widgetXType = widgetDefinition.getFieldValue(const.XTYPE) ?: return null

        val widgetDocumentation = widgetRepository.findByXType(widgetXType) ?: return null

        if (widgetDefinition.isXtypeValueSelected()) {
            return xtypeDocumentation(widgetDocumentation)
        }

        val selectedAttribute: SelectedAttribute = widgetDefinition.selectedAttribute ?: return null

        val widgetMember = widgetDocumentation.getMember(selectedAttribute.name) ?: return null

        val documentation = fieldDocumentation(widgetDocumentation, widgetMember)

        return documentation
    }

    /**
     * Generate documentation for `xtype`.
     *
     * @param widgetDoc the xtype data holder
     *
     * @return string with xtype documentation
     */
    fun xtypeDocumentation(widgetDoc: WidgetDoc): String = """
            <h2>${widgetDoc.className}</h2>
            <p>
                ${widgetDoc.description}
            </p>
        """.trimIndent().replace("\n", "")

    /**
     * Generate documentation for specific member of `xtype` (field, method).
     *
     * @param widgetDoc the xtype descriptor
     * @param widgetMember the specific widget member
     *
     * @return string with documentation
     */
    fun fieldDocumentation(widgetDoc: WidgetDoc, widgetMember: WidgetMember): String = """
            <h2>${widgetDoc.fullClassName}</h2>
            <p>
                Field name: <b>${widgetMember.name}</b>
            </p>
            <p>
                Defined by: <b>${widgetMember.definedBy}</b>
            </p>
            <p>
                ${widgetMember.description}
            </p>
        """.trimIndent().replace("\n", "")

}