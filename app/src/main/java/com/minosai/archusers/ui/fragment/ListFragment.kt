package com.minosai.archusers.ui.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.minosai.archusers.R
import com.minosai.archusers.adapter.CryptoAdapter
import com.minosai.archusers.di.Injectable
import com.minosai.archusers.ui.viewmodel.CryptoViewModel
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_list.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.bundleOf
import javax.inject.Inject

class ListFragment() : Fragment(), Injectable {
    private var listener: OnFragmentInteractionListener? = null

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var cryptoViewModel: CryptoViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        listener?.onFragmentInteraction("Cryptocurreny status")
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        AndroidSupportInjection.inject(this)
        super.onViewCreated(view, savedInstanceState)

        cryptoViewModel = ViewModelProviders.of(this, viewModelFactory).get(CryptoViewModel::class.java)

        val adapter = CryptoAdapter{ currencyData ->
            var bundle = bundleOf("cryptoid" to currencyData.id)
            findNavController().navigate(R.id.action_listFragment_to_infoFragment, bundle)
        }
        initRecyclerView(adapter)

        val listData = async {
            cryptoViewModel.getCryptoList()
        }
        launch {
            var cryptoList = listData.await()
            activity?.let {
                cryptoList.observe(it, Observer {
                    launch(UI) {
                        adapter.submitList(it)
                    }
                })
            }
        }
    }

    private fun initRecyclerView(adapter: CryptoAdapter) {
        val linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        rv_crypto.layoutManager = linearLayoutManager
        rv_crypto.adapter = adapter
    }

    fun onButtonPressed(string: String) {
        listener?.onFragmentInteraction(string)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(string: String)
    }

    companion object {
        @JvmStatic
        fun newInstance() = ListFragment()
    }
}
