package com.example.weatherapi.interfaces

import androidx.fragment.app.Fragment

interface IReplaceFragment {
    fun replaceFragment(fragment: Fragment, tag: String = "")
}