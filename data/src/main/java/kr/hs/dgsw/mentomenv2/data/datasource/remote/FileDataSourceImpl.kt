package kr.hs.dgsw.mentomenv2.data.datasource.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kr.hs.dgsw.mentomenv2.data.remote.FileDataSource
import kr.hs.dgsw.mentomenv2.data.response.ImgUrlResponseDto
import kr.hs.dgsw.mentomenv2.data.service.FileService
import okhttp3.MultipartBody
import javax.inject.Inject

class FileDataSourceImpl
    @Inject
    constructor(
        private val api: FileService,
    ) : FileDataSource {
        override fun postFile(files: List<MultipartBody.Part>): Flow<List<ImgUrlResponseDto>> {
            return flow {
                val response = api.postFile(files).execute()

                if (response.isSuccessful) {
                    emit(response.body()?.data ?: emptyList())
                } else {
                    emit(emptyList())
                }
            }.flowOn(Dispatchers.IO)
        }
    }
