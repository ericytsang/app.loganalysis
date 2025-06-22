package com.github.ericytsang.app.ui.frame.logviewer

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

interface SelectedItemsViewModel<T>
{
    /**
     * [Flow] of currently selected items.
     */
    val selectedItemsFlow:Flow<SelectedItems<T>>

    /**
     * selects all items.
     */
    fun selectAllItems()

    /**
     * deselects all items.
     */
    fun deselectAllItems()

    /**
     * selects the given range of items, and deselects all other items.
     * adds the given range of items to the already selected items.
     * beginning of the range depends on previous selection actions which
     * are determined by past calls to the view model.
     */
    fun <E> selectRange(
        list:List<E>,
        selector:(E)->T,
        keyOfItemAtEndOfRange:T,
        selectionModifier:SelectionModifier,
    )

    /**
     * selects the given item, and deselects all other items.
     */
    fun selectItem(itemKey:T)

    /**
     * toggles the selection state of the given item.
     * keeps the selection state of other items unchanged.
     */
    fun toggleItemSelection(itemKey:T)

    /**
     * call when the user starts a drag gesture.
     * @param keyOfItemAtDragStart the key of the item at the start of the drag gesture.
     * @param selectionModifier the selection modifier to use for the drag gesture.
     */
    fun beginDragToSelect(
        keyOfItemAtDragStart:T,
        selectionModifier:SelectionModifier,
    )

    /**
     * call when the user moves the pointer during a drag gesture.
     * @param list the list of items to select from.
     * @param selector a function that maps an item to its key.
     * @param keyOfItemAtPointer the key of the item at the pointer.
     */
    fun <E> updateDragToSelect(
        list:List<E>,
        selector:(E)->T,
        keyOfItemAtPointer:T,
    )

    /**
     * call when the user ends a drag gesture.
     */
    fun endDragToSelect()

    enum class SelectionModifier
    {
        /** adds the selected items to the already selected items */
        ADD_TO_SELECTION,

        /** replaces the already selected items with the selected items */
        REPLACE_SELECTION,
    }
}

class SelectedItemsViewModelImpl<T> : SelectedItemsViewModel<T>
{
    override val selectedItemsFlow: Flow<SelectedItems<T>> get() = _selectedItemsFlow
        .combine(_selectedByActiveDragGesture) { a,b -> UnionSelected(a,b) }

    private val _selectedItemsFlow = MutableStateFlow<SelectedItems<T>>(IncludeSelected())

    private val _selectedByActiveDragGesture = MutableStateFlow<IncludeSelected<T>>(IncludeSelected())

    private val startOfRangeSelection = MutableStateFlow<StartOfRangeSelectionState<T>>(ImplicitFirstItem())

    private sealed interface StartOfRangeSelectionState<T>
    private data class Selected<T>(val itemKey:T):StartOfRangeSelectionState<T>
    private data class ImplicitFirstItem<T>(val unit:Unit = Unit):StartOfRangeSelectionState<T>

    override fun selectAllItems()
    {
        _selectedItemsFlow.value = ExcludeSelected()
        startOfRangeSelection.value = ImplicitFirstItem()
    }

    override fun deselectAllItems()
    {
        _selectedItemsFlow.value = IncludeSelected()
        startOfRangeSelection.value = ImplicitFirstItem()
    }

    override fun <E> selectRange(
        list:List<E>,
        selector:(E)->T,
        keyOfItemAtEndOfRange:T,
        selectionModifier:SelectedItemsViewModel.SelectionModifier,
    )
    {
        val allItemKeys = list.asSequence().map(selector)
        val selectedItems = getItemsInSelectedRange(
            sequence = allItemKeys,
            startOfRangeSelectionState = startOfRangeSelection.value,
            endOfRangeItemKey = keyOfItemAtEndOfRange,
        )
        _selectedItemsFlow.update { oldValue ->
            when (selectionModifier)
            {
                SelectedItemsViewModel.SelectionModifier.ADD_TO_SELECTION ->
                    selectedItems.fold(oldValue) { acc, itemKey -> acc + itemKey }
                SelectedItemsViewModel.SelectionModifier.REPLACE_SELECTION ->
                    IncludeSelected(selectedItems.toSet())
            }
        }
    }

    override fun selectItem(itemKey:T)
    {
        _selectedItemsFlow.value = IncludeSelected(setOf(itemKey))
        startOfRangeSelection.value = Selected(itemKey)
    }

    override fun toggleItemSelection(itemKey:T)
    {
        _selectedItemsFlow.update { oldValue -> oldValue.toggle(itemKey) }
        startOfRangeSelection.value = Selected(itemKey)
    }

    override fun beginDragToSelect(
        keyOfItemAtDragStart:T,
        selectionModifier:SelectedItemsViewModel.SelectionModifier,
    )
    {
        // depending on the selection modifier, we either keep the current selection
        // or replace it with an empty selection
        _selectedItemsFlow.update()
        { oldValue ->
            when (selectionModifier)
            {
                SelectedItemsViewModel.SelectionModifier.ADD_TO_SELECTION -> oldValue
                SelectedItemsViewModel.SelectionModifier.REPLACE_SELECTION -> IncludeSelected()
            }
        }

        // we start the range selection from the item at the drag start
        startOfRangeSelection.value = Selected(keyOfItemAtDragStart)
    }

    override fun <E> updateDragToSelect(
        list:List<E>,
        selector:(E)->T,
        keyOfItemAtPointer:T,
    )
    {
        val itemsSelectedByDragGesture = getItemsInSelectedRange(
            sequence = list.asSequence().map(selector),
            startOfRangeSelectionState = startOfRangeSelection.value,
            endOfRangeItemKey = keyOfItemAtPointer,
        )
        _selectedByActiveDragGesture.value = IncludeSelected(itemsSelectedByDragGesture.toSet())
    }

    /**
     * commit the selection made by the drag gesture.
     */
    override fun endDragToSelect()
    {
        val itemsSelectedByDragGesture = _selectedByActiveDragGesture.value.includedItems
        _selectedItemsFlow.update { oldValue ->
            itemsSelectedByDragGesture.fold(oldValue) { acc, itemKey -> acc + itemKey }
        }
        _selectedByActiveDragGesture.value = IncludeSelected()
    }

    private fun getItemsInSelectedRange(
        sequence:Sequence<T>,
        startOfRangeSelectionState:StartOfRangeSelectionState<T>,
        endOfRangeItemKey:T,
    ):Sequence<T> = when (startOfRangeSelectionState)
    {
        is Selected ->
        {
            val conditions = mutableListOf(
                { it:T -> it == startOfRangeSelectionState.itemKey },
                { it:T -> it == endOfRangeItemKey },
            )
            fun isAnyRemainingConditionTrue(item:T):Boolean
            {
                val result = conditions.find { condition -> condition(item) } ?: return false
                conditions -= result
                return true
            }
            sequence
                .dropWhile { !isAnyRemainingConditionTrue(it) }
                .takeWhile { !isAnyRemainingConditionTrue(it) }
        }
        is ImplicitFirstItem -> sequence
            .takeWhile { it != endOfRangeItemKey }
    }
}
















































