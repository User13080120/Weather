package com.example.weatherapi.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.weatherapi.databinding.FragmentErrorBinding
import com.example.weatherapi.interfaces.IHideLocationSearchButton
import com.example.weatherapi.interfaces.IHideSearchView
import com.example.weatherapi.interfaces.IHideToolbar
import com.example.weatherapi.interfaces.IHideTopNavigationLayout

class ErrorFragment : BaseFragment(), IHideToolbar, IHideTopNavigationLayout, IHideSearchView, IHideLocationSearchButton{

    companion object{
        const val BUNDLE_ERROR_MESSAGE = "errorMessage"
    }

    private val TAG = "ErrorFragment"
    private lateinit var binding: FragmentErrorBinding
    private lateinit var errorMessage: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        accessActivityUI.hideLoadingDialog()
        errorMessage = arguments?.getString(BUNDLE_ERROR_MESSAGE).toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentErrorBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.errorTv.text = errorMessage
        binding.returnBtn.setOnClickListener {
            replaceFragmentListener.replaceFragment(LocationsFragment())
        }
    }

}