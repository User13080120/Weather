package com.example.weatherapi.interfaces

import androidx.appcompat.widget.SearchView

interface IAccessActivityUI {
    fun hideLocationSearchButton()
    fun showLocationSearchButton()
    fun hideTopNavigationLayout()
    fun showTopNavigationLayout()
    fun hideSearchView()
    fun showSearchView()
    fun hideToolbar()
    fun showToolbar()
    fun getToolbar(): androidx.appcompat.widget.Toolbar
    fun getSearchView(): SearchView
    fun showLoadingDialog()
    fun hideLoadingDialog()
}