/*
 * Copyright (c) 2020-2021. Kolvakh Bohdan
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

package com.acmpo6ou.myaccounts.core

import androidx.lifecycle.Observer
import com.acmpo6ou.myaccounts.R

abstract class ViewModelFragment: SuperFragment() {
    abstract val viewModel: SuperViewModel
    abstract val mainActivity: MainActivityInter

    private val errorObserver = Observer<String>{
        val errorTitle = mainActivity.myContext.resources.getString(R.string.error_title)
        mainActivity.showError(errorTitle, it)
    }

    open fun initModel(){
        viewModel.errorMsg_.observe(viewLifecycleOwner, errorObserver)
    }

}