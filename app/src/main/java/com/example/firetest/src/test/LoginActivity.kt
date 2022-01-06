package com.example.firetest.src.test

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.example.firetest.R
import com.example.firetest.config.BaseActivity
import com.example.firetest.databinding.ActivityLoginBinding
import com.example.firetest.src.splash.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : BaseActivity<ActivityLoginBinding>(ActivityLoginBinding::inflate) {

    private lateinit var auth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth


        binding.btnGoogleLogin.setOnClickListener {

            var email = binding.etId.text.toString()
            var name = binding.etPw.text.toString()

            createAccount(email,name)
        }

    }

    private fun createAccount(email: String, password: String) {

        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth?.createUserWithEmailAndPassword(email, password)
                ?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "계정 생성 완료.", Toast.LENGTH_SHORT).show()
                        finish() // 가입창 종료
                    } else {
                        Toast.makeText(this, "계정 생성 실패", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

}