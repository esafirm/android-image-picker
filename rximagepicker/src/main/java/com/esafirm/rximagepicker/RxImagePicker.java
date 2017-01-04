package com.esafirm.rximagepicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;

import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;


public class RxImagePicker {

    @SuppressLint("StaticFieldLeak")
    private static RxImagePicker INSTANCE;

    public static RxImagePicker getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RxImagePicker();
        }
        return INSTANCE;
    }

    /* --------------------------------------------------- */
    /* > RxImagePicker */
    /* --------------------------------------------------- */

    private SerializedSubject<List<Image>, List<Image>> subject;

    private RxImagePicker() {
        this.subject = new SerializedSubject<>(PublishSubject.<List<Image>>create());
    }

    public Observable<List<Image>> start(Context context, ImagePicker imagePicker) {
        startImagePicker(context, imagePicker);
        return subject;
    }

    private void startImagePicker(Context context, ImagePicker imagePicker) {
        Bundle bundle = imagePicker.getIntent(context).getExtras();
        Intent intent = ShadowActivity.getStartIntent(context, bundle)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    void onHandleResult(List<Image> images) {
        subject.onNext(images);
    }
}
