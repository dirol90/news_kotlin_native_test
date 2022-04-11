package com.example.v_jet_test.data.local

import android.content.Context
import com.example.v_jet_test.SHARED_PREFERENCES_FILE_NAME
import com.example.v_jet_test.data.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LocalData @Inject constructor() {

    fun getAllCachedFavourites(@ApplicationContext context: Context): Resource<MutableList<String>> {
        val returnList = mutableListOf<String>()
        val allEntries: Map<String, *> = context.getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, 0).all
        for ((_, value) in allEntries) {
            returnList.add(value.toString())
        }
        return Resource.Success(data = returnList)
    }

    fun isFavourite(id: String, @ApplicationContext context: Context): Resource<Boolean> {
        val sharedPref = context.getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, 0)
        val cache = sharedPref.getString(id, "")
        return Resource.Success(cache!!.contains(id))
    }

    fun cacheFavourites(
        id: String,
        value: String,
        @ApplicationContext context: Context
    ): Resource<Boolean> {
        val isSuccess = context.getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, 0).edit()
            .putString(id, value).commit()
        return Resource.Success(isSuccess)
    }

    fun removeFromFavourites(id: String, @ApplicationContext context: Context): Resource<Boolean> {
        val isSuccess =
            context.getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, 0).edit().remove(id).commit()
        return Resource.Success(isSuccess)
    }
}

