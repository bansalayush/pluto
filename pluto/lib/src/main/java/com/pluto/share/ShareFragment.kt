package com.pluto.share

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.pluto.R
import com.pluto.databinding.PlutoFragmentShareBinding
import com.pluto.plugin.utilities.setDebounceClickListener
import com.pluto.plugin.utilities.sharing.ContentShareViewModel
import com.pluto.plugin.utilities.sharing.ShareAction
import com.pluto.plugin.utilities.sharing.Shareable
import com.pluto.plugin.utilities.viewBinding

internal class ShareFragment : BottomSheetDialogFragment() {

    private val binding by viewBinding(PlutoFragmentShareBinding::bind)
    private val shareViewModel: ContentShareViewModel by activityViewModels()
    private var shareContent: Shareable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.pluto___fragment_share, container, false)

    override fun getTheme(): Int = R.style.PlutoBottomSheetDialogTheme

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        shareContent = arguments?.toShareable()

        binding.title.text = shareContent?.title ?: getString(R.string.pluto___share_as)

        binding.shareCopy.setDebounceClickListener {
            shareContent?.let {
                shareViewModel.performAction(ShareAction.ShareAsCopy(it))
            }
            dismiss()
        }

        binding.shareText.setDebounceClickListener {
            shareContent?.let {
                shareViewModel.performAction(ShareAction.ShareAsText(it))
            }
            dismiss()
        }

        binding.shareFile.setDebounceClickListener {
            shareContent?.let {
                shareViewModel.performAction(ShareAction.ShareAsFile(it))
            }
            dismiss()
        }
    }
}

private fun Bundle?.toShareable(): Shareable? {
    this?.let {
        return if (it.getString("title") != null &&
            it.getString("content") != null &&
            it.getString("fileName") != null
        ) {
            Shareable(
                title = it.getString("title") ?: "",
                content = it.getString("content") ?: "",
                fileName = it.getString("fileName") ?: "",
            )
        } else {
            null
        }
    }
    return null
}
