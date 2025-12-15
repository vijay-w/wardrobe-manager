package com.wardrobemanager.di

import com.wardrobemanager.data.repository.ClothingRepository
import com.wardrobemanager.data.repository.ClothingRepositoryImpl
import com.wardrobemanager.data.repository.ImageRepository
import com.wardrobemanager.data.repository.ImageRepositoryImpl
import com.wardrobemanager.data.repository.OutfitRepository
import com.wardrobemanager.data.repository.OutfitRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindClothingRepository(
        clothingRepositoryImpl: ClothingRepositoryImpl
    ): ClothingRepository

    @Binds
    @Singleton
    abstract fun bindOutfitRepository(
        outfitRepositoryImpl: OutfitRepositoryImpl
    ): OutfitRepository

    @Binds
    @Singleton
    abstract fun bindImageRepository(
        imageRepositoryImpl: ImageRepositoryImpl
    ): ImageRepository
}