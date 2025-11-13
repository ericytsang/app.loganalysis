package com.github.ericytsang.app.ui.frame.logviewer

import com.github.ericytsang.domain.objects.FilterInterpretationMode
import com.github.ericytsang.domain.objects.FilterModel
import com.github.ericytsang.domain.objects.FilterType
import com.github.ericytsang.logcatfilterparser.LeafNode
import com.github.ericytsang.logcatfilterparser.LogcatFilterParser
import com.github.ericytsang.logcatfilterparser.Node
import com.github.ericytsang.logcatfilterparser.NotNode

class FilterNodeFactory(
    private val logcatFilterParser:LogcatFilterParser,
)
{
    fun toFilterNode(filterModel:FilterModel):FilterModelWithNode?
    {
        val filterNode = when (filterModel.filterInterpretationMode)
        {
            FilterInterpretationMode.STRING_LITERAL -> LeafNode(
                key = "",
                value = filterModel.filterString,
                regex = false,
                caseSensitive = filterModel.isCaseSensitive,
            )
            FilterInterpretationMode.REGULAR_EXPRESSION -> LeafNode(
                key = "",
                value = filterModel.filterString,
                regex = true,
                caseSensitive = filterModel.isCaseSensitive,
            )
            FilterInterpretationMode.LOGCAT_FILTER -> try
            {
                logcatFilterParser.parse(
                    input = filterModel.filterString,
                    forceIsCaseSensitive = filterModel.isCaseSensitive,
                )
            }
            catch (e:Exception)
            {
                println("Error parsing logcat filter: ${filterModel.filterString}, error: ${e.message}")
                null
            }
        }
        return when
        {
            filterNode == null -> null
            else -> when (filterModel.filterType)
            {
                FilterType.INCLUDE,FilterType.BOOKMARK -> FilterModelWithNode(
                    filterType = filterModel.filterType,
                    filterNode = filterNode,
                )
                FilterType.EXCLUDE -> FilterModelWithNode(
                    filterType = filterModel.filterType,
                    filterNode = NotNode(filterNode),
                )
            }
        }
    }

    data class FilterModelWithNode(
        val filterType:FilterType,
        val filterNode:Node,
    )

    companion object
    {
        fun create(
            logcatFilterParser:LogcatFilterParser = LogcatFilterParser(),
        ) = FilterNodeFactory(
            logcatFilterParser = logcatFilterParser,
        )
    }
}