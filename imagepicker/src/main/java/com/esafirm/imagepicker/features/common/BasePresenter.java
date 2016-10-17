package com.esafirm.imagepicker.features.common;

public class BasePresenter<T extends MvpView> {

    private T view;

    public void attachView(T view) {
        this.view = view;
    }

    public T getView() {
        return view;
    }

    public void detachView() {
        view = null;
    }

    protected boolean isViewAttached() {
        return view != null;
    }
}
