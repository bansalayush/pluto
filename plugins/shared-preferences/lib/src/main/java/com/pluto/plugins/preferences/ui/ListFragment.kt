package com.pluto.plugins.preferences.ui

import android.os.Bundle
import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.pluto.plugin.utilities.extensions.hideKeyboard
import com.pluto.plugin.utilities.extensions.linearLayoutManager
import com.pluto.plugin.utilities.extensions.showMoreOptions
import com.pluto.plugin.utilities.list.BaseAdapter
import com.pluto.plugin.utilities.list.CustomItemDecorator
import com.pluto.plugin.utilities.list.DiffAwareAdapter
import com.pluto.plugin.utilities.list.DiffAwareHolder
import com.pluto.plugin.utilities.list.ListItem
import com.pluto.plugin.utilities.setDebounceClickListener
import com.pluto.plugin.utilities.viewBinding
import com.pluto.plugins.preferences.R
import com.pluto.plugins.preferences.Session
import com.pluto.plugins.preferences.databinding.PlutoPrefFragmentListBinding

internal class ListFragment : Fragment(R.layout.pluto_pref___fragment_list) {
    private val binding by viewBinding(PlutoPrefFragmentListBinding::bind)
    private val viewModel: SharedPrefViewModel by activityViewModels()

    private val prefAdapter: BaseAdapter by lazy { SharedPrefAdapter(onActionListener) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.refresh()
        binding.list.apply {
            adapter = prefAdapter
            addItemDecoration(CustomItemDecorator(requireContext()))
        }

        binding.search.doOnTextChanged { text, _, _, _ ->
            viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                text?.toString()?.let {
                    Session.searchText = it
                    prefAdapter.list = filteredPrefs(it)
                    if (it.isEmpty()) {
                        binding.list.linearLayoutManager()?.scrollToPositionWithOffset(0, 0)
                    }
                }
            }
        }
        binding.options.setDebounceClickListener {
            context?.showMoreOptions(it, R.menu.pluto_pref___menu_more_options) { item ->
                when (item.itemId) {
                    R.id.filter -> findNavController().navigate(R.id.openFilterSettings)
                }
            }
        }
        binding.search.setText(Session.searchText)
        viewModel.preferences.removeObserver(sharedPrefObserver)
        viewModel.preferences.observe(viewLifecycleOwner, sharedPrefObserver)

        binding.close.setDebounceClickListener {
            activity?.finish()
        }
    }

    private fun filteredPrefs(search: String): List<SharedPrefKeyValuePair> {
        var list = emptyList<SharedPrefKeyValuePair>()
        viewModel.preferences.value?.let {
            list = it.filter { pref ->
                pref.key.contains(search, true)
            }
        }
        binding.noItemText.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        return list
    }

    private val sharedPrefObserver = Observer<List<SharedPrefKeyValuePair>> {
        prefAdapter.list = filteredPrefs(binding.search.text.toString())
    }

    private val onActionListener = object : DiffAwareAdapter.OnActionListener {
        override fun onAction(action: String, data: ListItem, holder: DiffAwareHolder?) {
            if (data is SharedPrefKeyValuePair) {
                activity?.let {
                    it.hideKeyboard(viewLifecycleOwner.lifecycleScope) {
                        viewModel.updateCurrentPref(data)
                        findNavController().navigate(R.id.openEditView)
                    }
                }
            }
        }
    }
}
