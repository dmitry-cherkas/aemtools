package com.aemtools.completion.htl.model.declaration

import com.aemtools.analysis.htl.callchain.typedescriptor.TypeDescriptor
import com.aemtools.analysis.htl.callchain.typedescriptor.java.JavaPsiClassTypeDescriptor
import com.aemtools.analysis.htl.callchain.typedescriptor.template.TemplateHolderTypeDescriptor
import com.aemtools.completion.util.resolveUseClass
import com.aemtools.index.model.TemplateDefinition
import com.aemtools.index.search.HtlTemplateSearch
import com.aemtools.lang.java.JavaSearch
import com.intellij.psi.PsiClass
import com.intellij.psi.xml.XmlAttribute

/**
 * @author Dmytro Troynikov
 */
class HtlUseVariableDeclaration(
        xmlAttribute: XmlAttribute,
        variableName: String,
        attributeType: DeclarationAttributeType,
        type: DeclarationType = DeclarationType.VARIABLE
) : HtlVariableDeclaration(
        xmlAttribute,
        variableName,
        attributeType,
        type
) {

    fun useClass(): PsiClass? {
        val useClassName = xmlAttribute.resolveUseClass()
                ?: return null
        return JavaSearch.findClass(useClassName, xmlAttribute.project)
    }

    fun template(): List<TemplateDefinition> {
        val name = xmlAttribute.value ?: return listOf()
        return HtlTemplateSearch.resolveUseTemplate(name, xmlAttribute.containingFile)
    }

    fun typeDescriptor(): TypeDescriptor {
        val useClass = useClass()
        if (useClass != null) {
            return JavaPsiClassTypeDescriptor(useClass, null, null)
        }

        val template = template()
        if (template.isNotEmpty()) {
            return TemplateHolderTypeDescriptor(template,
                    xmlAttribute.project)
        }

        return TypeDescriptor.empty()
    }

    val slyUseType: UseType
        get() {
            return when {
                useClass() != null -> UseType.BEAN
                template().isNotEmpty() -> UseType.HTL
                else -> UseType.UNKNOWN
            }
        }

}