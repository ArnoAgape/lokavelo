package com.arnoagape.lokavelo.di

import com.arnoagape.lokavelo.data.service.bike.BikeApi
import com.arnoagape.lokavelo.data.service.bike.FirebaseBikeApi
import com.arnoagape.lokavelo.data.service.conversation.ConversationApi
import com.arnoagape.lokavelo.data.service.conversation.FirebaseConversationApi
import com.arnoagape.lokavelo.data.service.rental.FirebaseRentalApi
import com.arnoagape.lokavelo.data.service.rental.RentalApi
import com.arnoagape.lokavelo.data.service.user.FirebaseUserApi
import com.arnoagape.lokavelo.data.service.user.UserApi
import dagger.Binds
import dagger.Module
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
abstract class ApiBindingModule {

    @Binds
    @Singleton
    abstract fun bindBikeApi(impl: FirebaseBikeApi): BikeApi

    @Binds
    @Singleton
    abstract fun bindUserApi(impl: FirebaseUserApi): UserApi

    @Binds
    @Singleton
    abstract fun bindConversationApi(impl: FirebaseConversationApi): ConversationApi

    @Binds
    @Singleton
    abstract fun bindRentalApi(impl: FirebaseRentalApi): RentalApi
}