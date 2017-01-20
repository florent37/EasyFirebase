package com.github.florent37.easyfirebase.db;

import com.google.firebase.database.DataSnapshot;

import rx.Subscriber;

/**
 * Created by florentchampigny on 19/01/2017.
 */

public interface RxFirebaseConverter {

    <T> void convert(DataSnapshot dataSnapshot, Class<? super T> theClass, Subscriber<? super T> subscriber);

}
