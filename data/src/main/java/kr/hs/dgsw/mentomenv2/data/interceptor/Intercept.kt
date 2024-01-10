package kr.hs.dgsw.mentomenv2.data.interceptor

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import kr.hs.dgsw.mentomenv2.data.repository.DataStoreRepositoryImpl
import kr.hs.dgsw.mentomenv2.domain.util.Result
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONException
import retrofit2.HttpException
import javax.inject.Inject

class Intercept
@Inject
constructor(
    private val tokenRepositoryImpl: DataStoreRepositoryImpl,
) : Interceptor {
    private var token: String = ""
    private lateinit var response: Response

    @Synchronized
    override fun intercept(chain: Interceptor.Chain): Response {
        Log.d("intercept: ", "token : $token")
        runBlocking(Dispatchers.IO) {
            tokenRepositoryImpl.getToken().let {
                it.collect {
                    token =
                        when (it) {
                            is Result.Success -> it.data?.accessToken ?: ""
                            is Result.Error -> "Error"
                            is Result.Loading -> "Loading"
                        }
                }
            }
        }
        response = chain.proceedWithToken(chain.request())
        if (response.code == 401 || response.code == 400) {
            runBlocking { tokenRepositoryImpl.clearData() }
            response.close()
            Log.d("intercept:", "Here is first 401")
            chain.makeTokenRefreshCall()
            throw HttpException(retrofit2.Response.error<Any>(401, response.body!!))
        }
        return response
    }

    private fun Interceptor.Chain.makeTokenRefreshCall() {
        Log.d("intercept:", "here is makeTokenRefreshCall")
        runBlocking {
            tokenRepositoryImpl.getToken().let {
                it.collect {
                    token =
                        when (it) {
                            is Result.Success -> it.data?.refreshToken ?: ""
                            is Result.Error -> "Error"
                            is Result.Loading -> "Loading"
                        }
                    Log.d("Intercept", "makeTokenRefreshCall: $token}")
                }
            }
        }
        response = this.proceedWithToken(this.request())

        if (response.code == 401 || response.code == 400) {
            runBlocking { tokenRepositoryImpl.clearData() }
            throw HttpException(retrofit2.Response.error<Any>(401, response.body!!))
        }
    }

    private fun Interceptor.Chain.login(): Response {
        // 로그인으로 토큰 교체
//        getTokenToLogin()

        // request에 토큰을 붙여서 새로운 request 생성 -> 진행
        response = this.proceedWithToken(this.request())

        return if (response.code == 401) {
            Log.d("TokenTest", "Here is Login")
            runBlocking { tokenRepositoryImpl.clearData() }
            Response.Builder()
                .request(this.request())
                .protocol(Protocol.HTTP_1_1)
                .code(401)
                .message("세션이 만료되었습니다.")
                .body("세션이 만료되었습니다.".toResponseBody(null))
                .build()
        } else {
            response
        }
    }

    private fun Interceptor.Chain.proceedWithToken(req: Request): Response =
        req.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
            .let(::proceed)
}
