package kr.hs.dgsw.mentomenv2.feature.post

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kr.hs.dgsw.mentomenv2.R
import kr.hs.dgsw.mentomenv2.adapter.ImageAdapter
import kr.hs.dgsw.mentomenv2.base.BaseActivity
import kr.hs.dgsw.mentomenv2.databinding.ActivityPostBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okio.Buffer
import okio.BufferedSource
import java.io.File
import java.io.IOException
import java.nio.charset.Charset

@AndroidEntryPoint
class PostActivity : BaseActivity<ActivityPostBinding, PostViewModel>() {
    override val viewModel: PostViewModel by viewModels()

    private var imageAdapter: ImageAdapter? = null
    private val imageList = MutableLiveData<ArrayList<Uri?>>(arrayListOf())

    private var launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // 이미지 목록 및 파일 목록 초기화
                imageList.value?.clear()
                viewModel.imgFile.value?.clear()

                // 선택한 이미지 처리
                if (result.data?.clipData != null) {
                    val count = result.data?.clipData!!.itemCount
                    if (count > 10) {
                        // 이미지는 10장까지 선택 가능
                        Toast.makeText(this, "사진은 10장까지 선택 가능합니다.", Toast.LENGTH_LONG).show()
                        return@registerForActivityResult
                    }

                    for (i in 0 until count) {
                        val imageUri = result.data?.clipData!!.getItemAt(i).uri

                        // URI를 파일로 변환
                        val file = File(absolutelyPath(imageUri, this))

                        // 파일을 RequestBody로 변환
                        val extension = file.toString().split(".")[1]
                        val requestFile = file.asRequestBody("image/$extension".toMediaTypeOrNull())

                        // MultipartBody.Part 생성
                        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                        // 이미지 및 파일 목록에 추가
                        imageList.value?.add(imageUri)
                        viewModel.imgFile.value?.add(body)
                    }
                }
                imageAdapter?.submitList(imageList.value)
                imageAdapter?.notifyDataSetChanged()
            }
        }

    override fun start() {
        collectStates()
        observerViewModel()
        imageAdapter = ImageAdapter()
        mBinding.rvImage.adapter = imageAdapter
        mBinding.rvImage.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mBinding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun absolutelyPath(path: Uri?, context: Context): String {
        val proj: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
        var cursor: Cursor? = null
        var result: String? = null

        try {
            cursor = context.contentResolver.query(path!!, proj, null, null, null)
            val index = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor?.moveToFirst()
            result = cursor?.getString(index!!)
        } catch (e: Exception) {
            // 예외 처리
        } finally {
            cursor?.close()
        }

        return result ?: ""
    }


    private fun observerViewModel() {
        bindingViewEvent {
            when (it) {
                PostViewModel.ON_CLICK_IMAGE -> {
                    getImageGallery()
                }

                PostViewModel.ON_CLICK_SUBMIT -> {
                    submitPost()
                }

                PostViewModel.SUBMIT_MESSAGE -> {
                    Toast.makeText(this, PostViewModel.SUBMIT_MESSAGE, Toast.LENGTH_SHORT).show()
                    if (PostViewModel.SUBMIT_MESSAGE == "게시글 등록에 성공했습니다.") {
                        finish()
                    }
                }
            }
        }
    }

    private fun collectStates() {
        viewModel.content.observe(this) { content ->
            if (content.isNotEmpty()) {
                viewModel.tagState.observe(this) { tag ->
                    Log.d(
                        "collectStates in PostActivity",
                        "tag: $tag + content: $content image: ${viewModel.imgUrl.value}"
                    )
                    if (tag != "ALL") {
                        mBinding.btnConfirm.setBackgroundResource(R.drawable.bg_btn_enable)
                    } else {
                        mBinding.btnConfirm.setBackgroundResource(R.drawable.bg_btn_disable)
                    }
                }
            } else {
                mBinding.btnConfirm.setBackgroundResource(R.drawable.bg_btn_disable)
            }
        }
    }

    fun submitPost() {
        if (viewModel.content.value.isNullOrBlank()) {
            Toast.makeText(this, "내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        if (viewModel.tagState.value == "ALL") {
            Toast.makeText(this, "태그를 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.submitPost()
    }

    private fun getImageGallery() {
        val chooserIntent = Intent(Intent.ACTION_CHOOSER)
        val intent = Intent(Intent.ACTION_PICK)

        intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        chooserIntent.putExtra(Intent.EXTRA_INTENT, intent)
        chooserIntent.putExtra(Intent.EXTRA_TITLE, "사용할 앱을 선택해주세요.")
        launcher.launch(chooserIntent)
    }
}
