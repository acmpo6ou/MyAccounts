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

package com.acmpo6ou.myaccounts.database_utils

import androidx.test.core.app.ActivityScenario
import com.acmpo6ou.myaccounts.MainActivity
import com.acmpo6ou.myaccounts.R
import com.acmpo6ou.myaccounts.core.GeneratePassword
import com.acmpo6ou.myaccounts.core.hasoneof
import com.google.android.material.textfield.TextInputEditText
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.LooperMode


@RunWith(RobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
class GeneratePasswordInst {
    lateinit var scenario: ActivityScenario<MainActivity>
    lateinit var dialog: GeneratePassword

    val pass1: TextInputEditText = mock()
    val pass2: TextInputEditText = mock()

    @Before
    fun setup(){
        scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity {
            it.myContext.setTheme(R.style.Theme_MyAccounts_NoActionBar)
            dialog = GeneratePassword(it, pass1, pass2)
        }
    }

    @Test
    fun `click on generateButton should generate password of correct length`(){
        scenario.onActivity {
            // default length should be 16
            assertEquals(16, dialog.length.value)

            dialog.generateButton.performClick()
            verify(pass1).setText(argThat<String> { length == 16 })
        }
    }

    @Test
    fun `click on generateButton should generate password using correct characters`(){
        scenario.onActivity {
            // password should contain only upper letters and digits
            dialog.lowerBox.isChecked = false
            dialog.punctBox.isChecked = false

            dialog.generateButton.performClick()
            argumentCaptor<String>{
                verify(pass1).setText(capture())

                assertTrue(firstValue hasoneof dialog.digits)
                assertTrue(firstValue hasoneof dialog.upper)
                assertFalse(firstValue hasoneof dialog.lower)
                assertFalse(firstValue hasoneof dialog.punctuation)
            }
        }
    }

    @Test
    fun `generated password should be set on password fields`(){
        // passwords from both fields
        var text1 = ""
        var text2 = ""

        dialog.generateButton.performClick()

        // get passwords from fields
        argumentCaptor<String>{
            verify(pass1).setText(capture())
            text1 = firstValue
        }
        argumentCaptor<String>{
            verify(pass2).setText(capture())
            text2 = firstValue
        }

        // check passwords
        assertEquals(text1, text2)
    }
}