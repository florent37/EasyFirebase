# EasyFirebase

```gradle
compile 'com.github.florent37:easyfirebase:1.0.0'
```

# Google Login

## Initialize

```java
private EasyFirebaseAuth easyFirebaseAuth;


@Override
protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ...
        GoogleApiClient googleApiClient= ...

        this.easyFirebaseAuth = new EasyFirebaseAuth(googleApiClient);
}

@Override
public void onStart() {
    super.onStart();
    easyFirebaseAuth.onStart();
}

@Override
public void onStop() {
    easyFirebaseAuth.onStop();
    super.onStop();
}

@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    easyFirebaseAuth.onActivityResult(requestCode, data);
}
```

## Start Login

```java
easyFirebaseAuth.signInWithGoogle((Activity)this)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(Pair<googleSignInAccount, firebaseUser> -> {
                    //user is connected
                    final String id = firebaseUser.getUid();
                    final String name = firebaseUser.getDisplayName();
                    final String email = firebaseUser.getEmail();
                    final String photoUrl = firebaseUser.getPhotoUrl().toString();
                    //update your UI
                });
```

# Database

## Initialize

```java
DatabaseReference database = FirebaseDatabase.getInstance().getReference();

EasyFirebaseDb userDb = new EasyFirebaseDb(database.child("users"), converter);
```

## Create

```
userDb.addNew(name)
      .withValue("registerDate", System.currentTimeMillis())
      .withValue("id", id)
      .withValue("name", name)
      .withValue("email", email)
      .withValue("photoUrl", photoUrl)
      .push()
      .subscribe(v -> {
            //the user has been created
      })
```

## Query

```java
Observable<User> = userDb.getObject(userName, User.class);

Observable<List<User>> = userDb.getChildsObjects(User.class).toList();
```

### Join

```java
Observable<List<User>> = Observable.zip(
        userDb.getChildsObjects(User.class).toList(),
        followersDb.getKeys(username), //only retrieve the keys
        (users, followers) -> {
             for (User user : users) {
                 final String userName = user.getName();
                 if (followers.contains(userName)) {
                     user.setFollower(true);
                 }
             }
             return users;
});
```

## Converter

```java
public class MyConverterImpl implements RxFirebaseConverter {

    @Override
    public <T> void convert(DataSnapshot dataSnapshot, Class<? super T> theClass, Subscriber<? super T> subscriber){
            if(User.class.equals(theClass)){
                userFromDataSnapshot(dataSnapshot, subscriber);
            } else {
                throw new UnsupportedOperationException("unknown :" + theClass.getCanonicalName().toString());
            }
    }

    public <T> void userFromDataSnapshot(DataSnapshot dataSnapshot, Subscriber<? super T> subscriber) {
            final String userName = dataSnapshot.getKey();
            final String email = dataSnapshot.child("email").getValue(String.class);
            final String id = dataSnapshot.child("id").getValue(String.class);
            final String photoUrl = dataSnapshot.child("photoUrl").getValue(String.class);

            final User user = new User(userName, email, id, photoUrl);

            subscriber.onNext((T)user);
    }

```


#Credits

Author: Florent Champigny [http://www.florentchampigny.com/](http://www.florentchampigny.com/)

<a href="https://plus.google.com/+florentchampigny">
  <img alt="Follow me on Google+"
       src="https://raw.githubusercontent.com/florent37/DaVinci/master/mobile/src/main/res/drawable-hdpi/gplus.png" />
</a>
<a href="https://twitter.com/florent_champ">
  <img alt="Follow me on Twitter"
       src="https://raw.githubusercontent.com/florent37/DaVinci/master/mobile/src/main/res/drawable-hdpi/twitter.png" />
</a>
<a href="https://www.linkedin.com/in/florentchampigny">
  <img alt="Follow me on LinkedIn"
       src="https://raw.githubusercontent.com/florent37/DaVinci/master/mobile/src/main/res/drawable-hdpi/linkedin.png" />
</a>


License
--------

    Copyright 2016 florent37, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
