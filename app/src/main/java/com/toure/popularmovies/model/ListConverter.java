package com.toure.popularmovies.model;

import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class ListConverter {

    @TypeConverter
    public static List<Integer> toList(String string) {
        if (string != null) {
            Gson gson = new Gson();
            Type collectionType = new TypeToken<List<Integer>>(){}.getType();
            return gson.fromJson(string, collectionType);
        }
        return null;
    }

    @TypeConverter
    public static String toString(List<Integer> integerList) {
        Gson gson = new Gson();
        return integerList == null ? null : gson.toJson(integerList);
    }
}
