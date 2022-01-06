package com.example.firetest.src.page

import android.content.Intent
import android.os.Bundle
import com.example.firetest.config.BaseActivity
import com.example.firetest.databinding.ActivityPageBinding
import com.example.firetest.src.splash.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class PageActivity : BaseActivity<ActivityPageBinding>(ActivityPageBinding::inflate) {
    private lateinit var auth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

        binding.btnLogout.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            auth.signOut()
            finish()
        }
    }
}