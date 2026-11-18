package com.bernardols.mosaic.data

import javax.inject.Inject

class DefaultAppInfoRepository @Inject constructor(): AppInfoRepository {
    override fun getAppName(): String = "Mosaic"
}