package com.github.florent37.easyfirebase.db;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by florentchampigny on 19/01/2017.
 */

public class EasyFirebaseDb {

    private DatabaseReference databaseReference;
    private RxFirebaseHelper rxFirebaseHelper;

    public EasyFirebaseDb(DatabaseReference databaseReference, RxFirebaseConverter converter) {
        this.databaseReference = databaseReference;
        this.rxFirebaseHelper = new RxFirebaseHelper(converter);
    }

    public FirebaseNewBuilder addNew(String name) {
        return new FirebaseNewBuilder(this, name);
    }

    public <T> Observable<T> getObject(String key, Class<T> theClass) {
        return rxFirebaseHelper.getObjects(databaseReference.child(key).orderByKey().limitToFirst(1), theClass, true);
    }

    public Observable<String> getKeys(String keyName){
        return rxFirebaseHelper.getStrings(databaseReference.child(keyName), RxFirebaseHelper.KEY);
    }

    public <T> Observable<T> getRootObjects(String key, Class<T> theClass) {
        return rxFirebaseHelper.getObjects(databaseReference.child(key), theClass, true);
    }

    public <T> Observable<T> getChildsObjects(Class<T> theClass){
        return rxFirebaseHelper.getObjects(databaseReference.orderByKey(), theClass, false);
    }

    public <T> Observable<T> getChildsObjects(String key, Class<T> theClass){
        return rxFirebaseHelper.getObjects(databaseReference.child(key).orderByKey(), theClass, false);
    }

    public static class FirebaseNewBuilder {
        private final Map<String, Object> values;
        private final EasyFirebaseDb easyFirebaseDb;
        private String name;

        public FirebaseNewBuilder(EasyFirebaseDb easyFirebaseDb, String name) {
            this.values = new HashMap<>();
            this.easyFirebaseDb = easyFirebaseDb;
            this.name = name;
        }

        public FirebaseNewBuilder addNew(String child){
            name = name + "/" + child;
            return this;
        }

        public FirebaseNewBuilder withValue(String key, Object value) {
            values.put(key, value);
            return this;
        }

        public Observable<Void> push() {
            final DatabaseReference child = easyFirebaseDb.databaseReference.child(name);
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                child.child(entry.getKey()).setValue(entry.getValue());
            }
            return Observable.create(new Observable.OnSubscribe<Void>() {
                                         @Override
                                         public void call(final Subscriber<? super Void> subscriber) {
                                             child.addListenerForSingleValueEvent(new ValueEventListener() {
                                                 @Override
                                                 public void onDataChange(DataSnapshot dataSnapshot) {
                                                     subscriber.onNext(null);
                                                     subscriber.onCompleted();
                                                 }

                                                 @Override
                                                 public void onCancelled(DatabaseError databaseError) {
                                                     subscriber.onError(databaseError.toException());
                                                 }
                                             });
                                         }
                                     });
        }
    }

}
