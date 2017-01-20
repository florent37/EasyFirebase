package com.github.florent37.easyfirebase.auth;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by florentchampigny on 18/01/2017.
 */

public class EasyFirebaseAuth {

    public interface Callback {
        void onUserConnected(FirebaseUser user);
    }

    public static final int RC_SIGN_IN = 10021;

    @Nullable
    private Subscriber<? super FirebaseUser> firebaseUserSubscriber;

    private FirebaseAuth mAuth;

    private Callback callback;

    private FirebaseAuth.AuthStateListener mAuthListener;

    private final GoogleApiClient googleApiClient;

    public EasyFirebaseAuth(GoogleApiClient googleApiClient) {
        mAuth = FirebaseAuth.getInstance();
        this.googleApiClient = googleApiClient;

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    if (firebaseUserSubscriber != null) {
                        firebaseUserSubscriber.onNext(user);
                        firebaseUserSubscriber.onCompleted();
                    }
                    if (callback != null) {
                        callback.onUserConnected(user);
                    }
                    // User is signed in
                    Log.d("TAG", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("TAG", "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void onStart() {
        mAuth.addAuthStateListener(mAuthListener);
    }

    public void onStop() {
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void onActivityResult(int requestCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d("TAG", "signInWithCredential:onComplete:" + task.isSuccessful());

                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Log.w("TAG", "signInWithCredential", task.getException());
                                }
                                // ...
                            }
                        });
            } else {
                if (firebaseUserSubscriber != null) {
                    firebaseUserSubscriber.onError(new Throwable("error while signing with google"));
                }
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }

    public Observable<FirebaseUser> signInWithGoogle(Activity activity) {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        activity.startActivityForResult(signInIntent, RC_SIGN_IN);

        return Observable.create(new Observable.OnSubscribe<FirebaseUser>() {
            @Override
            public void call(Subscriber<? super FirebaseUser> s) {
                firebaseUserSubscriber = s;
            }
        });

    }
}
