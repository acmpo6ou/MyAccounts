/*
 * Copyright (c) 2020-2023. Bohdan Kolvakh
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

package com.acmpo6ou.myaccounts.main_activity

import android.content.SharedPreferences
import android.net.Uri
import com.acmpo6ou.myaccounts.MyApp
import com.acmpo6ou.myaccounts.SRC_DIR
import com.acmpo6ou.myaccounts.accountsDir
import com.acmpo6ou.myaccounts.database.databases_list.Database
import com.acmpo6ou.myaccounts.database.main_activity.MainActivityI
import com.acmpo6ou.myaccounts.database.main_activity.MainModelI
import com.acmpo6ou.myaccounts.database.main_activity.MainPresenter
import com.nhaarman.mockitokotlin2.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.spy
import java.io.File

class MainPresenterTests {
    lateinit var presenter: MainPresenter
    lateinit var spyPresenter: MainPresenter

    lateinit var app: MyApp
    lateinit var view: MainActivityI
    lateinit var model: MainModelI

    lateinit var mockPrefs: SharedPreferences
    private val locationUri: Uri = mock()

    @Before
    fun setup() {
        app = mock {
            on { SRC_DIR } doReturn SRC_DIR
            on { databases } doReturn mutableListOf(Database("test", "123"))
        }

        view = mock()
        model = mock {
            on { getSize(locationUri) } doReturn 116
        }

        presenter = MainPresenter({ view }, model, app)
        spyPresenter = spy(presenter)
    }

    @Test
    fun `fixSrcFolder should create SRC_DIR if it doesn't exist`() {
        // here we delete accounts folder if it already exists to ensure that it will
        // be empty as is needed for our test
        val accountsFolder = File(accountsDir)
        accountsFolder.deleteRecursively()

        presenter.fixSrcFolder()

        // the src folder should be created
        val srcDir = File(SRC_DIR)
        assertTrue(srcDir.exists())
    }

    @Test
    fun `importSelected should call view importDialog`() {
        presenter.importSelected()
        verify(view).importDialog()
    }

    @Test
    fun `checkDbaFile should call importDatabase if there are no errors`() {
        spyPresenter.model = model
        doNothing().`when`(spyPresenter).importDatabase(locationUri)

        spyPresenter.checkDbaFile(locationUri)
        verify(spyPresenter).importDatabase(locationUri)
        verify(view, never()).showError(anyString(), anyString())
    }

    private fun importMainDatabase() {
        doReturn("main").whenever(model).importDatabase(locationUri)
        presenter.model = model
        presenter.importDatabase(locationUri)
    }

    @Test
    fun `importDatabase should add imported database to the list`() {
        importMainDatabase()
        assertTrue(Database("main") in presenter.databases)
    }

    @Test
    fun `backPressed should call confirmBack when there are unsaved databases`() {
        doReturn(false).whenever(model).isDatabaseSaved(any())
        presenter.backPressed()

        verify(view).confirmBack()
        verify(view, never()).showExitTip()
        verify(view, never()).goBack()
    }

    @Test
    fun `backPressed should call showExitTip when there are opened databases`() {
        doReturn(true).whenever(model).isDatabaseSaved(any())
        presenter.backPressed()

        verify(view).showExitTip()
        verify(view, never()).confirmBack()
        verify(view, never()).goBack()
    }

    @Test
    fun `backPressed should call goBack when there are no opened databases`() {
        whenever(app.databases).thenReturn(mutableListOf(Database("main")))
        presenter.backPressed()

        verify(view).goBack()
        verify(view, never()).showExitTip()
        verify(view, never()).confirmBack()
    }

    @Test
    fun `saveSelected should save all unsaved databases`() {
        whenever(app.databases).doReturn(
            mutableListOf(
                Database("main"),
                Database("test", "123"), // unsaved
                Database("saved", "123")
            )
        )
        whenever(model.isDatabaseSaved(app.databases[1])).thenReturn(false)
        whenever(model.isDatabaseSaved(app.databases[2])).thenReturn(true)

        presenter.saveSelected()
        verify(model).saveDatabase("test", app.databases[1])
        verify(model, never()).saveDatabase("main", app.databases[0])
        verify(model, never()).saveDatabase("saved", app.databases[2])
    }
}
