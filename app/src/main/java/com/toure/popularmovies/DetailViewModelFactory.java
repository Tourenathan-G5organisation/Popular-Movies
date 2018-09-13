package com.toure.popularmovies;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.toure.popularmovies.model.AppDatabase;

public class DetailViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private AppDatabase mDb;
    private int mItemId;

    public DetailViewModelFactory(AppDatabase mDb, int itemId) {
        this.mDb = mDb;
        this.mItemId = itemId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new DetailViewModel(mDb, mItemId);
    }
}
