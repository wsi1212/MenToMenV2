package kr.hs.dgsw.mentomenv2.feature.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kr.hs.dgsw.mentomenv2.base.BaseViewModel
import kr.hs.dgsw.mentomenv2.domain.model.Post
import kr.hs.dgsw.mentomenv2.domain.model.StdInfo
import kr.hs.dgsw.mentomenv2.domain.repository.PostRepository
import kr.hs.dgsw.mentomenv2.domain.util.Utils
import kr.hs.dgsw.mentomenv2.state.PostState
import javax.inject.Inject

@HiltViewModel
class HomeFragmentViewModel @Inject constructor(
    private val postRepository: PostRepository,
) : BaseViewModel() {
    val postState = MutableStateFlow<PostState>(PostState())
    private val _errorFlow = MutableSharedFlow<String?>()
    val errorFlow = _errorFlow.asSharedFlow()

    private val isLoading: MutableLiveData<Boolean> = MutableLiveData(false)

    init {
        getAllPost()
    }

    fun getAllPost() {
        postRepository.getAllPost().safeApiCall(
            isLoading,
            successAction = {
                if (postState.value.tag != "ALL") {
                    postState.value = PostState(
                        postList = it,
                        tag = "ALL"
                    )
                } else {
                    postState.value = PostState(
                        postList = it,
                        tag = "ALL"
                    )
                }
            },
            errorAction = {
                _errorFlow.tryEmit(Utils.NETWORK_ERROR_MESSAGE)
            }
        )
            .launchIn(viewModelScope)
    }

    fun onClickDesignBtn() {
        postRepository.getPostByTag("DESIGN").safeApiCall(
            isLoading,
            successAction = {
                if (postState.value.tag != "DESIGN") {
                    postState.value = PostState(
                        postList = it,
                        tag = "DESIGN"
                    )
                } else {
                    postState.value = PostState(
                        postList = it,
                        tag = "ALL"
                    )
                }
            },
            errorAction = {
                _errorFlow.tryEmit(Utils.NETWORK_ERROR_MESSAGE)
            }
        )
            .launchIn(viewModelScope)
    }

    fun onClickWebBtn() {
        postRepository.getPostByTag("WEB").safeApiCall(
            isLoading,
            successAction = {
                if (postState.value.tag != "WEB") {
                    postState.value = PostState(
                        postList = it,
                        tag = "WEB"
                    )
                } else {
                    postState.value = PostState(
                        postList = it,
                        tag = "ALL"
                    )
                }
            },
            errorAction = {
                _errorFlow.tryEmit(Utils.NETWORK_ERROR_MESSAGE)
            }
        )
            .launchIn(viewModelScope)
    }

    fun onClickAndroidBtn() {
        postRepository.getPostByTag("ANDROID").safeApiCall(
            isLoading,
            successAction = {
                if (postState.value.tag != "ANDROID") {
                    postState.value = PostState(
                        postList = it,
                        tag = "ANDROID"
                    )
                } else {
                    postState.value = PostState(
                        postList = it,
                        tag = "ALL"
                    )
                }
            },
            errorAction = {
                _errorFlow.tryEmit(Utils.NETWORK_ERROR_MESSAGE)
            }
        )
            .launchIn(viewModelScope)
    }

    fun onClickServerBtn() {
        postRepository.getPostByTag("SERVER").safeApiCall(
            isLoading,
            successAction = {
                if (postState.value.tag != "SERVER") {
                    postState.value = PostState(
                        postList = it,
                        tag = "SERVER"
                    )
                } else {
                    postState.value = PostState(
                        postList = it,
                        tag = "ALL"
                    )
                }
            },
            errorAction = {
                _errorFlow.tryEmit(Utils.NETWORK_ERROR_MESSAGE)
            }
        )
            .launchIn(viewModelScope)
    }

    fun onClickIOSBtn() {
        postRepository.getPostByTag("IOS").safeApiCall(
            isLoading,
            successAction = {
                if (postState.value.tag != "IOS") {
                    postState.value = PostState(
                        postList = it,
                        tag = "IOS"
                    )
                } else {
                    postState.value = PostState(
                        postList = it,
                        tag = "ALL"
                    )
                }
            },
            errorAction = {
                _errorFlow.tryEmit(Utils.NETWORK_ERROR_MESSAGE)
            }
        )
            .launchIn(viewModelScope)
    }
}
