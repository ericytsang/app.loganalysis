package com.github.ericytsang.app.ui.frame.logviewer

/**
 * sealed class representing a set of selected items.
 * there are 2 implementations:
 * - one representing "selected all items except for the ones in a set of excluded items"
 * - another representing "selected no items except for the ones in a set of included items"
 */
sealed class SelectedItems<T>
{
    operator fun contains(item:T):Boolean = isSelected(item)

    fun isSelected(item:T):Boolean = when (this)
    {
        is IncludeSelected -> item in includedItems
        is ExcludeSelected -> item !in excludedItems
    }

    fun add(item:T):SelectedItems<T> = when (this)
    {
        is IncludeSelected -> IncludeSelected(includedItems + item)
        is ExcludeSelected -> ExcludeSelected(excludedItems - item)
    }

    fun remove(item:T):SelectedItems<T> = when (this)
    {
        is IncludeSelected -> IncludeSelected(includedItems - item)
        is ExcludeSelected -> ExcludeSelected(excludedItems + item)
    }

    fun toggle(item:T):SelectedItems<T> = when (item in this)
    {
        true -> remove(item)
        false -> add(item)
    }

    /**
     * selected no items except for the ones in a set of included items.
     */
    data class IncludeSelected<T>(val includedItems:Set<T> = emptySet()):SelectedItems<T>()

    /**
     * selected all items except for the ones in a set of excluded items.
     */
    data class ExcludeSelected<T>(val excludedItems:Set<T> = emptySet()):SelectedItems<T>()
}
