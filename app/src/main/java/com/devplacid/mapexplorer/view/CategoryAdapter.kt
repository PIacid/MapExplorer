package com.devplacid.mapexplorer.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devplacid.mapexplorer.R
import com.devplacid.mapexplorer.api.Category
import kotlinx.android.synthetic.main.category_item.view.*

class CategoryAdapter(private val name: String?): RecyclerView.Adapter<CategoryAdapter.ItemHolder>() {

    private val list = Category.values().toList()

    class ItemHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        fun setData(category: Category, name: String?) {
            itemView.categoryNameText.text = category.displayedName
            itemView.selectedCategoryRadioButton.isChecked = category.apiName == name

            itemView.setOnClickListener {
                (it.context as SelectionActivity).setItemSelected(category.apiName)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.category_item, parent, false)
        return ItemHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.setData(list[position], name)
    }

    override fun getItemCount(): Int {
        return list.size
    }


}