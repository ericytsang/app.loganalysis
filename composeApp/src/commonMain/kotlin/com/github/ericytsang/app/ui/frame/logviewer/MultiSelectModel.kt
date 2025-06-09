package com.github.ericytsang.app.ui.frame.logviewer

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

sealed class SelectedItems<T>
{
    fun isSelected(subject:T):Boolean = when (this)
    {
        is SelectedSome -> subject in selectedItems
        is DeselectedSome -> subject !in deselectedItems
    }

    data class SelectedSome<T>(val selectedItems:Set<T>):SelectedItems<T>()
    data class DeselectedSome<T>(val deselectedItems:Set<T>):SelectedItems<T>()
}

interface MutableHasSelectableItems<T>:HasSelectableItems<T>
{
    fun addToSelection(item:T)
    fun removeFromSelection(item:T)
    fun deselectAll()
    fun selectAll()
}

interface HasSelectableItems<T>
{
    val selectedItems:Flow<SelectedItems<T>>
}

class HasSelectableItemsImpl<T>(
    initialSelection:SelectedItems<T> = SelectedItems.SelectedSome(emptySet()),
):MutableHasSelectableItems<T>
{
    private val _selectedItems = MutableStateFlow<SelectedItems<T>>(initialSelection)
    override val selectedItems:Flow<SelectedItems<T>> get() = _selectedItems
    override fun addToSelection(item:T)
    {
        _selectedItems.update { old ->
            when (old)
            {
                is SelectedItems.SelectedSome -> SelectedItems.SelectedSome(old.selectedItems+item)
                is SelectedItems.DeselectedSome -> SelectedItems.DeselectedSome(old.deselectedItems-item)
            }
        }
    }

    override fun removeFromSelection(item:T)
    {
        _selectedItems.update { old ->
            when (old)
            {
                is SelectedItems.SelectedSome -> SelectedItems.SelectedSome(old.selectedItems-item)
                is SelectedItems.DeselectedSome -> SelectedItems.DeselectedSome(old.deselectedItems+item)
            }
        }
    }

    override fun deselectAll()
    {
        _selectedItems.value = SelectedItems.SelectedSome(emptySet())
    }

    override fun selectAll()
    {
        _selectedItems.value = SelectedItems.DeselectedSome(emptySet())
    }
}
