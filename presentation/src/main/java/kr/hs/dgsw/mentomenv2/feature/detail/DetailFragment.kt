package kr.hs.dgsw.mentomenv2.feature.detail

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kr.hs.dgsw.mentomenv2.R
import kr.hs.dgsw.mentomenv2.adapter.CommentAdapter
import kr.hs.dgsw.mentomenv2.adapter.DetailImageAdapter
import kr.hs.dgsw.mentomenv2.adapter.callback.CommentAdapterCallback
import kr.hs.dgsw.mentomenv2.base.BaseFragment
import kr.hs.dgsw.mentomenv2.databinding.CommentSettingFragmentBinding
import kr.hs.dgsw.mentomenv2.databinding.FragmentDetailBinding
import kr.hs.dgsw.mentomenv2.feature.detail.comment.CommentViewModel
import kr.hs.dgsw.mentomenv2.feature.main.MainActivity


@AndroidEntryPoint
class DetailFragment :
    BaseFragment<FragmentDetailBinding, DetailViewModel>(),
    CommentAdapterCallback {
    private val commentViewModel: CommentViewModel by viewModels()
    override val viewModel: DetailViewModel by viewModels()

    private val args: DetailFragmentArgs by navArgs()

    private val commentAdapter = CommentAdapter(this)
    private var isEdit: MutableLiveData<Boolean> = MutableLiveData(false)
    private var commentId: MutableLiveData<Int> = MutableLiveData(0)

    override fun setupViews() {
        collectState()
        observeEvent()
        observeViewModel()
        settingDefaultValue()
        (activity as MainActivity).hasBottomBar(false)
        viewModel.getUserInfo()

        mBinding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        mBinding.cvComment.setOnClickListener {
            hideKeyboard()
        }

        mBinding.main.setOnClickListener {
            hideKeyboard()
        }

        mBinding.viewpagerFrame.setOnClickListener {
            hideKeyboard()
        }

        mBinding.root.setOnClickListener {
            hideKeyboard()
        }

        mBinding.srlPost.setOnRefreshListener {
            viewModel.getPostInfo()
            commentViewModel.getComment()
            mBinding.srlPost.isRefreshing = false
        }

        isEdit.observe(viewLifecycleOwner) { isEdit ->
            if (!isEdit) {
                hideKeyboard()
            }
        }

        mBinding.ivSend.setOnClickListener {
            if (isEdit.value == false) {
                commentViewModel.postComment()
            } else {
                commentViewModel.updateComment(
                    commentId = commentId.value ?: 0,
                    mBinding.etComment.text.toString()
                )
            }
        }

        val bottomSheetBinding = CommentSettingFragmentBinding.inflate(layoutInflater)
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.window?.attributes?.windowAnimations = R.style.AnimationPopupStyle
        bottomSheetDialog.setContentView(bottomSheetBinding.root)
        mBinding.btnMore.setOnClickListener {
            bottomSheetDialog.show()
        }

        bottomSheetBinding.tvCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetBinding.tvDelete.setOnClickListener {
            viewModel.deletePost()
            bottomSheetDialog.dismiss()
        }

        bottomSheetBinding.tvEdit.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
    }

    private fun settingDefaultValue() {
        mBinding.detailViewModel = viewModel
        mBinding.commentViewModel = commentViewModel
        mBinding.rvComment.layoutManager = LinearLayoutManager(requireContext())
        mBinding.rvComment.adapter = commentAdapter
        viewModel.author.value = args.item.author
        viewModel.imgUrls.value = args.item.imgUrls
        viewModel.userName.value = args.item.userName
        viewModel.content.value = args.item.content
        viewModel.createDateTime.value = args.item.createDateTime
        viewModel.stdInfo.value = args.item.stdInfo
        viewModel.postId.value = args.item.postId
        viewModel.profileImg.value = args.item.profileUrl
        commentViewModel.postId.value = args.item.postId
        commentViewModel.getComment()
    }

    private fun hideKeyboard() {
        mBinding.etComment.clearFocus()
        val imm =
            requireView().context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    private fun showKeyboard() {
        mBinding.etComment.requestFocus()
        if (!mBinding.etComment.text.isNullOrEmpty()) {
            mBinding.etComment.setSelection(mBinding.etComment.text.length)
        }
        val window: Window = requireActivity().window
        WindowCompat.getInsetsController(window, mBinding.etComment)
            .show(WindowInsetsCompat.Type.ime())
    }

    private fun observeViewModel() {
        viewModel.myProfileImg.observe(this) { profileImage ->
            if (!profileImage.isNullOrBlank()) {
                Glide.with(requireContext())
                    .load(profileImage)
                    .into(mBinding.ivCommentProfile)
            } else {
                Glide.with(requireContext())
                    .load(R.drawable.ic_default_user)
                    .into(mBinding.ivCommentProfile)
            }
        }

        viewModel.myUserId.observe(this) {
            if (args.item.author == it) {
                mBinding.btnMore.visibility = View.VISIBLE
            } else {
                mBinding.btnMore.visibility = View.GONE
            }
            commentAdapter.setMyUserId(it)
        }

        viewModel.profileImg.observe(this) {
            if (!it.isNullOrBlank()) {
                Glide.with(requireContext())
                    .load(it)
                    .into(mBinding.ivProfile)
            } else {
                Glide.with(requireContext())
                    .load(R.drawable.ic_default_user)
                    .into(mBinding.ivProfile)
            }
        }

        viewModel.imgUrls.observe(this) {
            if (!it.isNullOrEmpty()) {
                mBinding.viewpagerFrame.visibility = View.VISIBLE
                val imageAdapter = DetailImageAdapter(it) {}
                mBinding.viewpager.adapter = imageAdapter
                mBinding.wormDotsIndicator.attachTo(mBinding.viewpager)
                mBinding.wormDotsIndicator.visibility = View.VISIBLE
            } else {
                mBinding.viewpagerFrame.visibility = View.GONE
                mBinding.wormDotsIndicator.visibility = View.GONE
            }
        }

        viewModel.createDateTime.observe(this) {
            mBinding.datetime.text = it
        }

        commentViewModel.errorMessage.observe(this) {
            if (it.isNotBlank()) {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun collectState() {
        lifecycleScope.launch {
            commentViewModel.commentState.collect { state ->
                Log.d("collectCommentState: ", "collectCommentState: ${state.commentList}")
                commentAdapter.submitList(state.commentList)
            }
        }
    }

    private fun observeEvent() {
        bindingViewEvent {
            when (it) {
                CommentViewModel.UPDATE_COMMENT -> {
                    hideKeyboard()
                    Toast.makeText(requireContext(), "댓글 수정에 성공했습니다.", Toast.LENGTH_SHORT).show()
                }

                CommentViewModel.DELETE_COMMENT -> {
                    Toast.makeText(requireContext(), "댓글 삭제에 성공했습니다.", Toast.LENGTH_SHORT).show()
                }

                CommentViewModel.UPLOAD_COMMENT -> {
                    hideKeyboard()
                    Toast.makeText(requireContext(), "댓글 작성에 성공했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun deleteComment(commentId: Int) {
        commentViewModel.deleteComment(commentId)
    }

    override fun updateIsEdit(isEdit: Boolean, commentId: Int, value: String) {
        mBinding.etComment.setText(value)
        showKeyboard()
        this.isEdit.value = isEdit
        this.commentId.value = commentId
    }
}
