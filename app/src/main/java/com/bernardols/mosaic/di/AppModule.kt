package com.bernardols.mosaic.di

import com.bernardols.mosaic.data.AppInfoRepository
import com.bernardols.mosaic.data.DefaultAppInfoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    abstract fun bindAppInfoRepository(
        implementation: DefaultAppInfoRepository
    ): AppInfoRepository
}