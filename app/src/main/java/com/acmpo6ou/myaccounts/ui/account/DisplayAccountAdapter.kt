/*
 * Copyright (c) 2020-2021. Bohdan Kolvakh
 * This file is part of MyAccounts.
 *
 * MyAccounts is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyAccounts is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.acmpo6ou.myaccounts.ui.account

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.acmpo6ou.myaccounts.R
import com.acmpo6ou.myaccounts.account.DisplayAccountFragmentInter

class DisplayAccountAdapter(val view: DisplayAccountFragmentInter) :
    RecyclerView.Adapter<DisplayAccountAdapter.ViewHolder>() {

    val presenter get() = view.presenter
    private val attachedFiles get() = presenter.attachedFilesList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
        DisplayAccountAdapter.ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item, parent, false)
            return ViewHolder(view)
        }

    override fun onBindViewHolder(holder: DisplayAccountAdapter.ViewHolder, position: Int) {
        holder.fileName.text = attachedFiles[position]
        holder.attachImage.setImageResource(R.drawable.ic_attached_file)
        holder.menu.visibility = View.GONE // we don't need the dots menu
    }

    override fun getItemCount(): Int = attachedFiles.size

    /**
     * Represents ViewHolder for item of attached files list.
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var attachImage: ImageView = view.findViewById(R.id.itemIcon)
        var fileName: TextView = view.findViewById(R.id.itemName)
        var menu: TextView = view.findViewById(R.id.dots_menu)

        init {
            view.setOnClickListener {
                presenter.fileSelected(fileName.text.toString())
            }
        }
    }
}