package com.detection.detectto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView

class ModelAdapter(
    private var items: List<ModelFile>,
    private val onClick: (ModelFile) -> Unit
) : RecyclerView.Adapter<ModelAdapter.ModelViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_buttons, parent, false)
        return ModelViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ModelViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ModelViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val btn: Button = view.findViewById(R.id.button)
        fun bind(model: ModelFile) {
            btn.text = model.name
            btn.setOnClickListener { onClick(model) }
        }
    }

    fun updateData(newItems: List<ModelFile>) {
        items = newItems
        notifyDataSetChanged()
    }
}
