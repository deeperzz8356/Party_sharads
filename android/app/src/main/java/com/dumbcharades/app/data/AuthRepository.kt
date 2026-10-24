package com.dumbcharades.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.AuthResult
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class AuthUser(
    val uid: String,
)

@Singleton
class AuthRepository @Inject constructor(private val auth: FirebaseAuth) {
    private val mutableUser = MutableStateFlow(auth.currentUser?.takeIf { it.isAnonymous }?.toAuthUser())
    val user: StateFlow<AuthUser?> = mutableUser.asStateFlow()
    private val identityMutex = Mutex()
    private var pendingIdentity: Task<AuthResult>? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        mutableUser.value = firebaseAuth.currentUser?.takeIf { it.isAnonymous }?.toAuthUser()
    }

    init {
        auth.addAuthStateListener(authListener)
        // Gameplay never waits for a connection. Firebase persists this device's
        // anonymous identity; the same uid is available after an offline restart.
        scope.launch {
            var retryDelay = 5_000L
            while (isActive && mutableUser.value == null) {
                try {
                    ensureDeviceIdentity()
                } catch (cancelled: CancellationException) {
                    if (cancelled !is kotlinx.coroutines.TimeoutCancellationException) throw cancelled
                } catch (_: Exception) {
                    // Offline first launch stays local and retries quietly.
                }
                if (mutableUser.value == null) {
                    delay(retryDelay)
                    retryDelay = (retryDelay * 2).coerceAtMost(300_000L)
                }
            }
        }
    }

    suspend fun ensureDeviceIdentity(): AuthUser = identityMutex.withLock {
        auth.currentUser?.takeIf { it.isAnonymous }?.toAuthUser()?.let { return@withLock it }
        // Account data remains under its original uid. Device mode only adopts
        // local guest rows; it never repurposes a former account's identity.
        if (auth.currentUser != null) auth.signOut()
        val task = pendingIdentity ?: auth.signInAnonymously().also { pendingIdentity = it }
        val device = try {
            withTimeout(20_000) { task.await().user.requireUser() }
        } finally {
            // A coroutine timeout does not cancel Firebase's request. Reuse it
            // on retry rather than accidentally minting several device identities.
            if (task.isComplete) pendingIdentity = null
        }
        mutableUser.value = device
        device
    }

    private fun FirebaseUser?.requireUser(): AuthUser = this?.toAuthUser()
        ?: error("Firebase did not return an authenticated user")

    private fun FirebaseUser.toAuthUser() = AuthUser(uid)
}
