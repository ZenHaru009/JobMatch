package com.example.jobmatch.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.jobmatch.model.User
import kotlinx.coroutines.tasks.await

class ProfileRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getUserProfile(): Result<User> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val doc = firestore.collection("users").document(uid).get().await()
            val user = doc.toObject(User::class.java) ?: throw Exception("User data not found")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(
        name: String,
        pendidikan: String,
        keahlian: List<String>,
        pengalamanTahun: Int,
        lokasi: String
    ): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val updates = mapOf(
                "name" to name,
                "pendidikan" to pendidikan,
                "keahlian" to keahlian,
                "pengalamanTahun" to pengalamanTahun,
                "lokasi" to lokasi,
                "profileCompleted" to true
            )
            firestore.collection("users").document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}