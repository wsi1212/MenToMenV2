package kr.hs.dgsw.mentomenv2.data.interceptor

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import kr.hs.dgsw.mentomenv2.domain.repository.TokenRepository
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONException
import retrofit2.HttpException
import javax.inject.Inject

class Intercept @Inject constructor(
    private val tokenRepository: TokenRepository
) : Interceptor {

    private lateinit var token: String
    private lateinit var response: Response

    @Synchronized
    override fun intercept(chain: Interceptor.Chain): Response {
        runBlocking(Dispatchers.IO) {
            tokenRepository.getToken().let {
                it.collect {
                    token = it.data?.accessToken ?: ""
                }
            }
        }

        response = chain.proceedWithToken(chain.request())

        if (response.code == 401) {
            tokenRepository.deleteToken()
            chain.makeTokenRefreshCall()
            throw HttpException(retrofit2.Response.error<Any>(401, response.body!!))
        }

        return response
    }

    private fun Interceptor.Chain.makeTokenRefreshCall() {
        runBlocking(Dispatchers.IO) {
            tokenRepository.getToken().let {
                it.collect {
                    token = it.data?.accessToken ?: ""
                }
            }
        }
        response = this.proceedWithToken(this.request())

        if (response.code == 401) {
            tokenRepository.deleteToken()
            response = login()
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
            tokenRepository.deleteToken()
            Response.Builder()
                .request(this.request())
                .protocol(Protocol.HTTP_1_1)
                .code(401)
                .message("세션이 만료되었습니다.")
                .body("세션이 만료되었습니다.".toResponseBody(null))
                .build()
        } else response
    }

    private fun Interceptor.Chain.proceedWithToken(req: Request): Response =
        req.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
            .let(::proceed)
}
