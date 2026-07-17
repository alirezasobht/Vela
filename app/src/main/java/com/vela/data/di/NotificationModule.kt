package com.vela.data.di

import com.vela.data.platform.AlertNotificationSender
import com.vela.domain.notification.AlertNotifier
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {

    @Binds
    @Singleton
    abstract fun bindAlertNotifier(impl: AlertNotificationSender): AlertNotifier
}
