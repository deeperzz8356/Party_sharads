package com.dumbcharades.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
)

@Singleton
class AuthRepository @Inject constructor(private val auth: FirebaseAuth) {
    private val mutableUser = MutableStateFlow(auth.currentUser?.toAuthUser())
    val user: StateFlow<AuthUser?> = mutableUser.asStateFlow()

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        mutableUser.value = firebaseAuth.currentUser?.toAuthUser()
    }

    init {
        auth.addAuthStateListener(authListener)
    }

    suspend fun createAccount(email: String, password: String): AuthUser {
        require(email.isNotBlank()) { "Email is required" }
        require(password.length >= 6) { "Password must be at least 6 characters" }
        return auth.createUserWithEmailAndPassword(email.trim(), password).await().user.requireUser()
    }

    suspend fun signIn(email: String, password: String): AuthUser {
        require(email.isNotBlank()) { "Email is required" }
        require(password.isNotBlank()) { "Password is required" }
        return auth.signInWithEmailAndPassword(email.trim(), password).await().user.requireUser()
    }

    suspend fun signInWithGoogle(idToken: String): AuthUser {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return auth.signInWithCredential(credential).await().user.requireUser()
    }

    fun signOut() = auth.signOut()

    private fun FirebaseUser?.requireUser(): AuthUser = this?.toAuthUser()
        ?: error("Firebase did not return an authenticated user")

    private fun FirebaseUser.toAuthUser() = AuthUser(uid, email, displayName)
}
