package com.example.firetest.src.splash

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.example.firetest.R
import com.example.firetest.config.BaseActivity
import com.example.firetest.databinding.ActivityMainBinding
import android.widget.Toast

import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.example.firetest.config.ApplicationClass
import com.example.firetest.config.ApplicationClass.Companion.sSharedPreferences
import com.example.firetest.src.page.PageActivity
import com.example.firetest.src.test.LoginActivity
import com.example.firetest.src.upload.UploadActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult

import com.google.android.gms.tasks.OnFailureListener

import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate), GoogleApiClient.OnConnectionFailedListener {
    private lateinit var auth : FirebaseAuth
    private lateinit var mDatabase: DatabaseReference


    private var googleSignInClient : GoogleSignInClient? = null
    private var GOOGLE_LOGIN_CODE = 9001
    private var mGoogleApiClient: GoogleApiClient? = null

    @SuppressLint("StringFormatInvalid")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDatabase = FirebaseDatabase.getInstance().reference
        auth = Firebase.auth

        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)

        mGoogleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this, this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()

        binding.btnLogin.setOnClickListener {
            var email = binding.etEmail.text.toString()
            var name = binding.etName.text.toString()

            signIn(name,email)


        }
        binding.btnNext.setOnClickListener {
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)

        }

        binding.btnGoogleLoginPage.setOnClickListener{
            googleLogin()
        }

        binding.btnGoogleLogout.setOnClickListener {
            Log.d("알림", "구글 LOGOUT");
            googleLogout()
        }
        binding.btnImgUploadPage.setOnClickListener {
            val intent = Intent(this,UploadActivity::class.java)
            startActivity(intent)
        }
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result
            binding.tvText.text = token.toString()
            // Log and toast
            val msg = getString(R.string.msg_token_fmt, token)
            Log.d(TAG, msg)
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        })


    }

    private fun signIn(email: String, password: String) {

        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(baseContext, "로그인에 성공 하였습니다.", Toast.LENGTH_SHORT).show()
                        moveMainPage(auth.currentUser)
                    } else {
                        Toast.makeText(baseContext, "로그인에 실패 하였습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun moveMainPage(user: FirebaseUser?){
        if( user!= null){
            startActivity(Intent(this,PageActivity::class.java))
            finish()
        }
    }

    public override fun onStart() {
        super.onStart()
        moveMainPage(auth.currentUser)
    }

    override fun onResume() {
        super.onResume()
        restoreState()
    }

    override fun onStop() {
        super.onStop()
        saveState()
    }

    private fun restoreState(){
        //val pref = getSharedPreferences("pref", MODE_PRIVATE)
        if (sSharedPreferences != null && sSharedPreferences.contains("id")) {
            val id = sSharedPreferences.getString("id", "")
            binding.etName.setText(id)
            val pw = sSharedPreferences.getString("pw", "")
            binding.etEmail.setText(pw)
        }
    }
    private fun saveState(){
        //val pref = getSharedPreferences("pref", MODE_PRIVATE)
        val editor = sSharedPreferences.edit()
        editor.putString("id",binding.etName.text.toString())
        editor.putString("pw",binding.etEmail.text.toString())
        editor.commit()

    }
    private fun clearState() {
        //val pref = getSharedPreferences("pref", MODE_PRIVATE)
        val editor = sSharedPreferences.edit()
        editor.putString("id",null)
        editor.putString("pw",null)
        editor.commit()
    }

    private fun googleLogin(){
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent,GOOGLE_LOGIN_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == GOOGLE_LOGIN_CODE){
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data!!)!!
            // 구글API가 넘겨주는 값 받아옴

            if(result.isSuccess) {
                var accout = result.signInAccount
                firebaseAuthWithGoogle(accout)
                Toast.makeText(this,"성공",Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(this,"실패",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(account : GoogleSignInAccount?){
        var credential = GoogleAuthProvider.getCredential(account?.idToken,null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener{
                    task ->
                if(task.isSuccessful){
                    // 아이디, 비밀번호 맞을 때
                    moveMainPage(task.result?.user)
                }else{
                    // 틀렸을 때
                    Toast.makeText(this,task.exception?.message,Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun googleLogout() {
        mGoogleApiClient!!.connect()
        mGoogleApiClient!!.registerConnectionCallbacks(object :
            GoogleApiClient.ConnectionCallbacks {
            override fun onConnected(@Nullable bundle: Bundle?) {
                auth.signOut()
                if (mGoogleApiClient!!.isConnected) {
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient!!)
                        .setResultCallback { status ->
                            if (status.isSuccess) {
                                Log.d("알림", "로그아웃 성공")
                                setResult(1)
                            } else {
                                setResult(0)
                            }
                            showCustomToast("구글계정이 로그아웃 되었습니다.")
                        }
                }
            }
            override fun onConnectionSuspended(i: Int) {
                Log.d("알림", "Google API Client Connection Suspended")
                setResult(-1)
                finish()
            }
        })
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.d("알림", "onConnectionFailed");
    }

}