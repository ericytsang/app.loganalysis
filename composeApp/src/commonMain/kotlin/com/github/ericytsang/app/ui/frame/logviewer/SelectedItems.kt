package com.github.ericytsang.app.ui.frame.logviewer

interface SelectedItems<T>
{
    fun isSelected(item:T):Boolean
    fun add(item:T):SelectedItems<T>
    fun remove(item:T):SelectedItems<T>
}

operator fun <T> SelectedItems<T>.contains(item:T):Boolean = isSelected(item)

operator fun <T> SelectedItems<T>.plus(item:T):SelectedItems<T> = add(item)

operator fun <T> SelectedItems<T>.minus(item:T):SelectedItems<T> = remove(item)

fun  <T> SelectedItems<T>.toggle(item:T):SelectedItems<T> = when (item in this)
{
    true -> remove(item)
    false -> add(item)
}

/**
 * selected no items except for the ones in a set of included items.
 */
data class IncludeSelected<T>(val includedItems:Set<T> = emptySet()):SelectedItems<T>
{
    override fun isSelected(item:T):Boolean = item in includedItems
    override fun add(item:T):SelectedItems<T> = IncludeSelected(includedItems + item)
    override fun remove(item:T):SelectedItems<T> = IncludeSelected(includedItems - item)
}

/**
 * selected all items except for the ones in a set of excluded items.
 */
data class ExcludeSelected<T>(val excludedItems:Set<T> = emptySet()):SelectedItems<T>
{
    override fun isSelected(item:T):Boolean = item !in excludedItems
    override fun add(item:T):SelectedItems<T> = ExcludeSelected(excludedItems-item)
    override fun remove(item:T):SelectedItems<T> = ExcludeSelected(excludedItems+item)
}

/**
 * selected all items in a union of two sets of selected items.
 */
data class UnionSelected<T>(
    val a:SelectedItems<T>,
    val b:SelectedItems<T>,
):SelectedItems<T>
{
    override fun isSelected(item:T):Boolean = item in a || item in b
    override fun add(item:T):SelectedItems<T> = UnionSelected(a + item, b)
    override fun remove(item:T):SelectedItems<T> = UnionSelected(a - item, b - item)
}
