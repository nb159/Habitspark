package com.example.habitspark.ui.views.user

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitspark.data.models.UserModel
import com.example.habitspark.data.repository.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserViewModel(
    private val userRepository: UserRepository = UserRepository(Firebase.firestore),
): ViewModel() {

    private val _user = mutableStateOf<UserModel?>(null)
    val user: State<UserModel?> = _user

    fun fetchUser(userId: String) {
        viewModelScope.launch {
            try {
                val result = userRepository.getUser(userId).await()
                _user.value = result.toObject(UserModel::class.java)?.copy(id = result.id)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateUser(userId: String, user: UserModel) {
        viewModelScope.launch {
            try {
                userRepository.updateUser(userId, user)
                _user.value = user
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}