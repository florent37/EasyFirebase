package com.github.florent37.easyfirebase.db;

import android.support.annotation.IntDef;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by florentchampigny on 18/01/2017.
 */

public class RxFirebaseHelper {

    private RxFirebaseConverter converter;

    public static final int KEY = 0;
    public static final int VALUE = 1;

    public @IntDef({KEY, VALUE}) @interface KeyOfValue{};

    public RxFirebaseHelper(RxFirebaseConverter converter) {
        this.converter = converter;
    }

    public <T> Observable<T> push(final DatabaseReference databaseReference) {
        final Observable<T> observable = Observable.create(new Observable.OnSubscribe<T>() {
                                                               @Override
                                                               public void call(final Subscriber<? super T> subscriber) {
                                                                   databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                       @Override
                                                                       public void onDataChange(DataSnapshot dataSnapshot) {
                                                                           subscriber.onNext(null);
                                                                           subscriber.onCompleted();
                                                                       }

                                                                       @Override
                                                                       public void onCancelled(DatabaseError databaseError) {

                                                                       }
                                                                   });
                                                               }
                                                           });

        databaseReference.push();
        return observable;
    }

    public Observable<String> getStrings(final DatabaseReference databaseReference, @KeyOfValue final int keyOfValue) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                databaseReference.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            if (keyOfValue == KEY) {
                                subscriber.onNext(child.getKey());
                            } else {
                                subscriber.onNext(child.getValue(String.class));
                            }
                        }

                        subscriber.onCompleted();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    public <T> Observable<T> getObjects(final Query query, final Class<T> objectClass, final boolean useRootElement) {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(final Subscriber<? super T> subscriber) {
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(useRootElement) {
                            converter.convert(dataSnapshot, objectClass, subscriber);
                        } else {
                            for (DataSnapshot entry : dataSnapshot.getChildren()) {
                                converter.convert(entry, objectClass, subscriber);
                            }
                        }
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }
}
