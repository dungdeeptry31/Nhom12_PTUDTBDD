package com.educenter.app.repository;

/**
 * Generic callback interface for asynchronous Repository operations.
 *
 * @param <T> Type of expected result
 */
public interface DataCallback<T> {
    void onSuccess(T result);
    void onError(String message);
}
