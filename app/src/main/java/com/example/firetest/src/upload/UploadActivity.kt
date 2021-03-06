package com.example.firetest.src.upload

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_PICK
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.firetest.R
import com.example.firetest.config.BaseActivity
import com.example.firetest.databinding.ActivityUploadBinding
import com.google.android.gms.auth.api.signin.internal.Storage
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.io.InputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.FileDownloadTask

import com.google.firebase.storage.OnProgressListener

import androidx.annotation.NonNull
import java.io.IOException


data class User(val userName : String, val email : String)

class UploadActivity : BaseActivity<ActivityUploadBinding>(ActivityUploadBinding::inflate) {
    var userData = arrayListOf<User>()
    var count = 0
    private lateinit var mDatabase: DatabaseReference

    private var pickImageFromAlbum = 10
    private lateinit var fbStorage : FirebaseStorage
    private var uriPhoto : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDatabase = FirebaseDatabase.getInstance().reference
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)
        binding.btnUploadLogin.setOnClickListener {
            val id = binding.etUploadId.text.toString()
            val pw = binding.etUploadPw.text.toString()

            userData.add(User(id,pw))
            writeNewUser(count.toString(),userData[count].email,userData[count].userName)
            count++
        }

        fbStorage = FirebaseStorage.getInstance()

        binding.btnImgUpload.setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent,pickImageFromAlbum)
        }
        binding.btnImgDownload.setOnClickListener {
            imageDownload()
        }
    }

    private fun writeNewUser(userId: String, name: String, email: String) {
        val user = User(name, email)
        mDatabase.child("users").child(userId).setValue(user)
            .addOnSuccessListener(OnSuccessListener<Void?> { // Write was successful!
                Toast.makeText(this, "????????? ??????????????????.", Toast.LENGTH_SHORT).show()
            })
            .addOnFailureListener(OnFailureListener { // Write failed
                Toast.makeText(this, "????????? ??????????????????.", Toast.LENGTH_SHORT).show()
            })
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("????????? ?????????","onActivityResult????????? ?????????")
        if(requestCode == pickImageFromAlbum) {
            Log.d("????????? ?????????", "?????? ???????????? ??????")
            if (resultCode == Activity.RESULT_OK) {
                Log.d("????????? ?????????", "RESULT_OK ?????????")
                uriPhoto = data?.data
                binding.ivUploadImg.setImageURI(uriPhoto)

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                    imageUpload()
                }
            }
            else{
                Log.d("????????? ?????????","???????????? ????????? ????????????")
            }
        }
    }
    @SuppressLint("SimpleDateFormat")
    private fun imageUpload(){
        var timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imgFileName = "IMAGE_" + timeStamp + "_.png"
        var storageRef = fbStorage.reference.child("images").child((imgFileName))

        storageRef.putFile(uriPhoto!!).addOnSuccessListener {
            showCustomToast("image Uploaded")
        }.addOnFailureListener{
            showCustomToast("image Uploaded Failed")
        }
    }

    private fun imageDownload(){
        var storage = FirebaseStorage.getInstance()
        var storageRef = storage.reference
        var pathReference = storageRef.child("images/IMAGE_20220104_201607_.png")

        try {
            //????????? ????????? ????????? ??????
            val path = File(filesDir.absolutePath + "/Pictures")
            //???????????? ????????? ??????
            val file = File(path, "download_image")
            try {
                if (!path.exists()) {
                    //????????? ????????? ????????? ??????
                    path.mkdir()
                }
                file.createNewFile()

                //????????? ?????????????????? Task ??????, ?????????????????? ??????
                val fileDownloadTask = pathReference.getFile(file)
                fileDownloadTask.addOnSuccessListener {
                    showCustomToast("????????? ???????????? ??????")
                    binding.ivDownloadImg.setImageURI(Uri.fromFile(file))
                }.addOnFailureListener {
                    showCustomToast("????????? ???????????? ??????")
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}