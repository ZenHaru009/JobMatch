package com.example.jobmatch.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.jobmatch.model.User
import com.example.jobmatch.model.UserRole
import com.example.jobmatch.model.Perusahaan
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    private var userListener: com.google.firebase.firestore.ListenerRegistration? = null

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            userListener?.remove()
            if (firebaseUser != null) {
                userListener = firestore.collection("users").document(firebaseUser.uid)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            _currentUser.value = null
                            return@addSnapshotListener
                        }
                        if (snapshot != null && snapshot.exists()) {
                            _currentUser.value = snapshot.toObject(User::class.java)
                        } else {
                            _currentUser.value = null
                        }
                    }
            } else {
                _currentUser.value = null
            }
        }
    }

    suspend fun register(email: String, password: String, name: String, role: UserRole): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("Registrasi gagal")
            val user = User(uid = uid, email = email, name = name, role = role)
            firestore.collection("users").document(uid).set(user).await()
            if (role == UserRole.COMPANY) {
                val perusahaan = Perusahaan(
                    uid = uid,
                    nama = name,
                    email = email,
                    verificationStatus = "pending",
                    verified = false
                )
                firestore.collection("perusahaan").document(uid).set(perusahaan).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Mengembalikan User? setelah refresh, dan juga mengupdate _currentUser
    suspend fun refreshCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        return try {
            val doc = firestore.collection("users").document(firebaseUser.uid).get().await()
            val user = doc.toObject(User::class.java)
            _currentUser.value = user
            user
        } catch (e: Exception) {
            _currentUser.value = null
            null
        }
    }

    fun logout() {
        auth.signOut()
    }
}