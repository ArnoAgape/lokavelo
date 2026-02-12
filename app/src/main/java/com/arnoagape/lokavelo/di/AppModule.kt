package com.arnoagape.lokavelo.di

import com.arnoagape.lokavelo.data.service.bike.BikeApi
import com.arnoagape.lokavelo.data.service.bike.FirebaseBikeApi
import com.arnoagape.lokavelo.data.service.user.FirebaseUserApi
import com.arnoagape.lokavelo.data.service.user.UserApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing application-wide dependencies.
 * Installed in [SingletonComponent] to ensure single instances
 * across the whole app lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideBikeApi(firebaseBikeApi: FirebaseBikeApi): BikeApi = firebaseBikeApi

    @Provides
    @Singleton
    fun provideUserApi(): UserApi = FirebaseUserApi()
}