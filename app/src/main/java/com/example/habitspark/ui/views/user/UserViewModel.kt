package com.example.habitspark.ui.views.user

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitspark.data.models.UserModel
import com.example.habitspark.data.repository.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserViewModel(
    private val userRepository: UserRepository = UserRepository(Firebase.firestore),
): ViewModel() {

    private val _user = mutableStateOf<UserModel?>(null)
    val user: State<UserModel?> = _user

    private val _userListener = MutableStateFlow<UserModel?>(null)
    val userListener: StateFlow<UserModel?> = _userListener

    fun getUserById(userId: String) {
        viewModelScope.launch {
            try {
                val result = userRepository.getUserById(userId).await()
                _user.value = result
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    fun startUser(userId: String) {
        viewModelScope.launch {
            userRepository.listenUser(userId).collect { user ->
                _userListener.value = user
            }
        }
    }

    fun updateUser(user: UserModel) {
        viewModelScope.launch {
            try {
                userRepository.updateUser(user)
                _user.value = user
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}