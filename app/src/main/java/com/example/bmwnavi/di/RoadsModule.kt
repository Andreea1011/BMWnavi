package com.example.bmwnavi.di

import com.example.bmwnavi.data.GoogleRoadsApi
import com.example.bmwnavi.repo.GoogleRoadsRepository
import com.example.bmwnavi.repo.RoadsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoadsModule {

    @Provides @Singleton
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("https://roads.googleapis.com/") // Roads API base
        .addConverterFactory(MoshiConverterFactory.create())
        .client(OkHttpClient.Builder().build())
        .build()

    @Provides @Singleton
    fun provideGoogleRoadsApi(retrofit: Retrofit): GoogleRoadsApi =
        retrofit.create(GoogleRoadsApi::class.java)

    @Provides @Singleton
    fun provideRoadsRepository(api: GoogleRoadsApi): RoadsRepository =
        GoogleRoadsRepository(api, apiKey = /* TODO: put from local props/Secrets */ "")
}