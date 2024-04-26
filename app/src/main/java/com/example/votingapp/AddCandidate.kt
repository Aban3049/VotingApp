package com.example.votingapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.votingapp.databinding.ActivityAddCandidateBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class AddCandidate : AppCompatActivity() {

    private lateinit var binding: ActivityAddCandidateBinding

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth

    private var imageUri: Uri? = null

    private companion object {
        private const val TAG = "ADD_CANDIDATE_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {


       binding= ActivityAddCandidateBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.setMessage("Please wait...")

        binding.profileImagePickFab.setOnClickListener {
            imagePickDialog()
        }
        binding.submitBtn.setOnClickListener {
            validateData()
        }
        binding.backBtn.setOnClickListener {
          onBackPressedDispatcher.onBackPressed()
        }
        binding.candidateBtn.setOnClickListener {
            startActivity(Intent(this@AddCandidate,AdminActivity::class.java))
        }
        binding.addUserBtn.setOnClickListener{
            startActivity(Intent(this@AddCandidate,SignUpActivity::class.java))
        }
    }

    private var imageUrl = ""
    private var name = ""
    private var partyName = ""


    private fun validateData(){

        name = binding.nameEt.text.toString().trim()
        partyName = binding.partyNameEt.text.toString().trim()

        if (name.isEmpty()){
            binding.nameEt.requestFocus()
            binding.nameEt.error = "Field Required"
        }
        else if (partyName.isEmpty()){
            binding.partyNameEt.requestFocus()
            binding.partyNameEt.error = "Field Required"
        }
        else if (imageUri==null){
            Toast.makeText(this, "Add Image", Toast.LENGTH_SHORT).show()
        }
        else{
            uploadProfileImageStorage()
        }


    }

    private fun imagePickDialog() {
        val popupMenu = PopupMenu(this, binding.profileImagePickFab)
        popupMenu.menu.add(Menu.NONE, 1, 1, "Camera")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Gallery")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->

            val itemId = item.itemId
            if (itemId == 1) {
                Log.d(
                    TAG,
                    "imagePickDialog: Camera Clicked, check if camera permission(s) granted or not"
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestCameraPermissions.launch(arrayOf(android.Manifest.permission.CAMERA))
                } else {
                    requestCameraPermissions.launch(
                        arrayOf(
                            android.Manifest.permission.CAMERA,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    )
                }
            } else if (itemId == 2) {
                Log.d(
                    TAG,
                    "imagePickDialog: Gallery Picked, check if storage permission granted or not"
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pickImageGallery()
                } else {
                    requestStoragePermission.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }


            return@setOnMenuItemClickListener true

        }

    }

    private val requestCameraPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            Log.d(TAG, "requestCameraPermission: result: $result")

            // chk if permission is granted or not

            var areAllGranted = true
            for (isGranted in result.values) {
                areAllGranted = areAllGranted && isGranted
            }
            if (areAllGranted) {
                Log.d(TAG, "requestCameraPermissions: All are Granted e.g. Camera, Storage")
                pickImageCamera()
            } else {
                Log.d(TAG, "requestCameraPermission: All or either one is denied...")
                Toast.makeText(
                    this,
                    "Camera or Storage or both permission denied",
                    Toast.LENGTH_LONG
                ).show()
            }

        }

    private val requestStoragePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            Log.d(TAG, "requestStoragePermission: isGranted $isGranted")

            if (isGranted) {
                pickImageGallery()
            } else {

            }
            Toast.makeText(this, "Storage permission denied...", Toast.LENGTH_LONG).show()

        }

    private fun pickImageCamera() {
        Log.d(TAG, "pickImageCamera: ")
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_image_title")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_image_description")

        imageUri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // chk if Image is captured or Not
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "cameraActivityResultLauncher: Image captured: imageUri: $imageUri")
                try {
                    Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.ic_person_white)
                        .into(binding.imageIv)
                } catch (e: Exception) {
                    Log.e(TAG, "cameraActivityResultLauncher:", e)
                }
            } else {
                Toast.makeText(this, "Cancelled!", Toast.LENGTH_LONG)
            }

        }

    private fun pickImageGallery() {
        Log.d(TAG, "pickImageGallery")
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data

                imageUri = data!!.data

                try {
                    Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.ic_person_white)
                        .into(binding.imageIv)
                } catch (e: java.lang.Exception) {
                    Log.e(TAG, "galleryActivityResultLauncher")
                }
            }
        }

    private fun uploadProfileImageStorage() {
        Log.d(TAG, "uploadProfileImageStorage: ")
        // show progress
        progressDialog.setMessage("Uploading user profile Image")

        val timestamp = System.currentTimeMillis()

        val filePathAndName = "CandidateProfile/candidateProfile_$timestamp"
        val ref = FirebaseStorage.getInstance().reference.child(filePathAndName)

        //compress Image
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream)
        val reducedImage: ByteArray = byteArrayOutputStream.toByteArray()


        ref.putBytes(reducedImage)
            .addOnProgressListener { snapshot ->
                val progress = 100 * snapshot.bytesTransferred / snapshot.totalByteCount
                Log.d(TAG, "uploadProfileImageStorage: progress: $progress")
                progressDialog.setMessage("Uploading profile image. Progress: $progress%")
                progressDialog.show()
            }
            .addOnSuccessListener { taskSnapshot ->
                Log.d(TAG, "uploadProfileImageStorage: Image uploaded...")

                val uriTask = taskSnapshot.storage.downloadUrl

                while (!uriTask.isSuccessful);
                imageUrl = uriTask.result.toString()
                if (uriTask.isSuccessful) {
                    updateDb(imageUrl)
                }

                progressDialog.dismiss()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "uploadProfileImageStorage", e)
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Failed to upload Image due to ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

    }

    private fun updateDb(uploadedImageUrl: String?) {
        Log.d(TAG, "updateProfileDb: uploadedImageUrl: $uploadedImageUrl")

        progressDialog.setMessage("Updating user info")
        progressDialog.show()


        val ref = FirebaseDatabase.getInstance().getReference("Candidates")

        val timestamp = System.currentTimeMillis()
        val Id = ref.push().key

        val hashMap = HashMap<String, Any>()

        hashMap["profileImageURl"] = "$uploadedImageUrl"
        hashMap["timestamp"] = timestamp
        hashMap["name"] = "$name"
        hashMap["partyName"] = "$partyName"
        hashMap["id"] = "$Id"
        hashMap["voteCount"] = 0


        // dataBase reference to update user info



        ref.child("$Id")
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "updateProfileDb: updated...")
                progressDialog.dismiss()
                Toast.makeText(this, "Candidate Added Successfully", Toast.LENGTH_LONG).show()
                val intent = Intent(this,MainActivity::class.java)
                intent.putExtra("candidateId",Id)
                startActivity(intent)

                imageUri = null
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "updateProfileDb:", e)
                progressDialog.dismiss()
                Toast.makeText(this, "Failed due to ${e.message}", Toast.LENGTH_LONG).show()

            }

    }


}

