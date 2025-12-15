package com.wardrobemanager

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class WardrobeManagerApplication : Application(), ImageLoaderFactory {
    
    @Inject
    lateinit var imageLoader: ImageLoader
    
    override fun newImageLoader(): ImageLoader = imageLoader
}