package cse403.sp2020.tidy.data;

/**
 * An interface for calling back with results of a Firestore operation
 * @param <T> Any object that needs to be returned via callback
 */
public interface CallbackInterface<T> {
  void callback(T data);
}
