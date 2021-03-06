package com.aemtools.index.model

import com.aemtools.completion.util.findChildrenByType
import com.aemtools.completion.util.normalizeToJcrRoot
import com.aemtools.completion.util.toPsiFile
import com.aemtools.util.OpenApiUtil
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import java.io.Serializable

/**
 * @author Dmytro Troynikov
 */
data class AemComponentTouchUIDialogDefinition(
        /**
         * Full path to current `.content.xml` file.
         */
        val fullPath: String,

        /**
         * The resourceType of component holder of dialog.
         */
        val resourceType: String,

        val myParameters: List<TouchUIDialogParameterDeclaration>
) : Serializable {

    fun declarationElement(name: String, project: Project): PsiElement? {
        val file = OpenApiUtil
                .findFileByRelativePath(
                        fullPath.normalizeToJcrRoot(),
                        project
                )
                ?.toPsiFile(project) as? XmlFile
                ?: return null

        val rootTag = file.rootTag
                ?: return null

        return rootTag.findChildrenByType(XmlTag::class.java)
                .find {
                    it.attributes.any {
                        it.value in listOf(name, "./$name")
                    }
                }
    }

    companion object {
        @JvmStatic
        val serialVersionUID: Long = 1L
    }

    data class TouchUIDialogParameterDeclaration(
            /**
             * `sling:resourceType` value of current parameter.
             */
            val slingResourceType: String,

            /**
             * Name of current parameter.
             */
            val name: String

    ) : Serializable {

        companion object {
            @JvmStatic
            val serialVersionUID: Long = 1L
        }

        fun toLookupElement(): LookupElement =
                LookupElementBuilder.create(name.normalize())
                        .withIcon(AllIcons.Nodes.Parameter)
                        .withTailText("($slingResourceType)", true)
                        .withTypeText("Dialog")

        private fun String.normalize() = if (this.startsWith("./")) {
            this.substringAfter("./")
        } else {
            this
        }

    }
}