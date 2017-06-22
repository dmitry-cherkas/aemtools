package com.aemtools.completion.htl.provider

import com.aemtools.completion.htl.CompletionPriority.CLOSE_CLASS
import com.aemtools.completion.htl.CompletionPriority.CLOSE_TEMPLATE
import com.aemtools.completion.htl.CompletionPriority.FAR_CLASS
import com.aemtools.completion.htl.CompletionPriority.FAR_TEMPLATE
import com.aemtools.completion.util.normalizeToJcrRoot
import com.aemtools.completion.util.relativeTo
import com.aemtools.index.HtlIndexFacade.getTemplates
import com.aemtools.index.model.TemplateDefinition
import com.aemtools.lang.java.JavaSearch
import com.aemtools.util.withPriority
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiClass
import com.intellij.util.ProcessingContext
import org.apache.commons.lang.StringUtils

/**
 * Code completion for __data-sly-use.*__ attribute. (e.g. <div data-sly-use.bean="<caret>")
 *
 * @author Dmytro Troynikov.
 */
object SlyUseCompletionProvider : CompletionProvider<CompletionParameters>() {

    val log = Logger.getInstance(SlyUseCompletionProvider::class.java)

    override fun addCompletions(parameters: CompletionParameters,
                                context: ProcessingContext?,
                                result: CompletionResultSet) {
        result.addAllElements(useSuggestions(parameters))
    }

    fun useSuggestions(parameters: CompletionParameters): List<LookupElement> {
        val project = parameters.position.project
        val currentFileName = normalizedFileName(parameters)

        val useClasses = JavaSearch.findWcmUseClasses(project)
        val slingModelClasses = JavaSearch.findSlingModels(project)

        val useClassesVariants = extractCompletions(useClasses, currentFileName, "Use Class")
        val slingModelVariants = extractCompletions(slingModelClasses, currentFileName, "Sling Model")

        val allClasses = if (parameters.completionType == CompletionType.BASIC) {
            useClassesVariants + slingModelVariants
        } else {
            (useClassesVariants + slingModelVariants)
                    .filter {
                        closeName(normalizedClassName(it.lookupString), currentFileName)
                    }
        }

        val templates = extractTemplates(parameters)

        return allClasses + templates
    }

    private fun closeName(normalizedClassName: String, currentFileName: String): Boolean {
        return StringUtils.getLevenshteinDistance(
                normalizedClassName,
                currentFileName) < (currentFileName.length / 2).inc()
    }

    private fun normalizedClassName(fqn: String): String =
            fqn.substringAfterLast(".").toLowerCase()

    private fun normalizedFileName(parameters: CompletionParameters): String =
            parameters.originalFile.parent?.name?.toLowerCase()
                    ?: parameters.originalFile.name.toLowerCase()
                    .let { it.replace("-", "") }

    private fun extractCompletions(classes: List<PsiClass>, currentFileName: String, type: String): List<LookupElement> {
        return classes.flatMap {
            val qualifiedName = it.qualifiedName as? String
            val name = it.name as? String
            if (qualifiedName == null || name == null) {
                return@flatMap listOf<LookupElement>()
            }

            val result = LookupElementBuilder.create(qualifiedName)
                    .withLookupString(name)
                    .withPresentableText(name)
                    .withIcon(it.getIcon(0))
                    .withTypeText(type)
                    .withTailText("(${qualifiedName.substring(0, qualifiedName.lastIndexOf("."))})", true)
                    .withPriority(classCompletionPrirority(currentFileName, name))

            return@flatMap listOf(result)
        }
    }

    private fun classCompletionPrirority(fileName: String, className: String): Double =
            base(fileName, className) - StringUtils.getLevenshteinDistance(fileName, className) / 100.0

    private fun base(name1: String, name2: String): Double = if (closeName(name1, name1)) {
        CLOSE_CLASS
    } else {
        FAR_CLASS
    }

    private fun extractTemplates(parameters: CompletionParameters): List<LookupElement> {
        val dir = parameters.originalFile.containingDirectory.virtualFile
        val dirPath = dir.path
        val result: List<TemplateDefinition> = if (parameters.completionType == CompletionType.BASIC) {
            getTemplates(parameters.position.project)
        } else {
            val allTemplates = getTemplates(parameters.position.project)
            allTemplates.filter { it.containingDirectory.startsWith(dirPath) }
        }.groupBy { it.normalizedPath }
                .flatMap { it.value }
                .filter {
                    it.fullName != parameters.originalFile.virtualFile.path
                }

        return result.map {
            LookupElementBuilder.create(it.normalizedPath.relativeTo(dirPath.normalizeToJcrRoot()))
                    .withTypeText("HTL Template")
                    .withTailText("(${it.normalizedPath})", true)
                    .withPresentableText(it.fileName)
                    .withIcon(AllIcons.FileTypes.Html)
                    .withPriority(if (it.containingDirectory.startsWith(dirPath)) {
                        CLOSE_TEMPLATE
                    } else {
                        FAR_TEMPLATE
                    })
        }
    }

}