package com.example.fetchlist.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fetchlist.databinding.ItemGroupBinding
import com.example.fetchlist.databinding.ItemListBinding
import com.example.fetchlist.model.Item

sealed class ListItem {
    data class Group(val listId: Int) : ListItem()
    data class ItemEntry(val item: Item) : ListItem()
}

class ItemAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val listItems = mutableListOf<ListItem>()

    companion object {
        private const val VIEW_TYPE_GROUP = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    fun submitList(groupedData: List<Pair<Int, List<Item>>>) {
        listItems.clear()
        for ((listId, items) in groupedData) {
            listItems.add(ListItem.Group(listId))
            for (item in items) {
                listItems.add(ListItem.ItemEntry(item))
            }
        }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (listItems[position]) {
            is ListItem.Group -> VIEW_TYPE_GROUP
            is ListItem.ItemEntry -> VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_GROUP) {
            val binding = ItemGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return GroupViewHolder(binding)
        } else {
            val binding = ItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ItemViewHolder(binding)
        }
    }

    override fun getItemCount(): Int = listItems.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = listItems[position]) {
            is ListItem.Group -> (holder as GroupViewHolder).bind(item)
            is ListItem.ItemEntry -> (holder as ItemViewHolder).bind(item)
        }
    }

    class GroupViewHolder(private val binding: ItemGroupBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(group: ListItem.Group) {
            binding.groupTitle.text = "List ${group.listId}"
        }
    }

    class ItemViewHolder(private val binding: ItemListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(itemEntry: ListItem.ItemEntry) {
            binding.itemName.text = itemEntry.item.name
        }
    }
}
