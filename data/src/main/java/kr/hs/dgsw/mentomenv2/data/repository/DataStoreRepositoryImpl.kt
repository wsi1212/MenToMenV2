package kr.hs.dgsw.mentomenv2.data.repository

import kotlinx.coroutines.flow.Flow
import kr.hs.dgsw.mentomenv2.data.remote.DataStoreDataSource
import kr.hs.dgsw.mentomenv2.data.repository.base.BaseRepositoryImpl
import kr.hs.dgsw.mentomenv2.domain.model.Token
import kr.hs.dgsw.mentomenv2.domain.repository.DataStoreRepository
import kr.hs.dgsw.mentomenv2.domain.util.Result
import javax.inject.Inject

class DataStoreRepositoryImpl @Inject constructor(
    private val remote: DataStoreDataSource
) : BaseRepositoryImpl(), DataStoreRepository {
    override fun saveData(key: String, value: String): Flow<Result<Unit>> = execute {
        remote.saveData(key, value)
    }

    override fun getData(key: String, defaultValue: String): Flow<Result<String>> = execute {
        remote.getData(key, defaultValue)
    }

    override fun getToken(): Flow<Result<Token>> = execute {
        remote.getToken()
    }

    override fun removeData(key: String): Flow<Result<Unit>> = execute{
        remote.removeData(key)
    }

    override fun clearData(): Flow<Result<Unit>> = execute {
        remote.clearData()
    }
}