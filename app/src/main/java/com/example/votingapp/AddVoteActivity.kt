package com.example.votingapp

import android.app.KeyguardManager
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.votingapp.databinding.ActivityAddVoteBinding
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions


class AddVoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddVoteBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    private var cancellationSignal: CancellationSignal? = null

    private val REQUEST_IMAGE_CAPTURE = 1


    private var candidateId = ""


    private val authenticationCallback: BiometricPrompt.AuthenticationCallback
        @RequiresApi(Build.VERSION_CODES.P)
        get() =

            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                    super.onAuthenticationError(errorCode, errString)
                    Utills.toast(this@AddVoteActivity, "Authentication Error: $errString")
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                    super.onAuthenticationSucceeded(result)
                    Utills.toast(this@AddVoteActivity, "Authenticated Successfully")
                    binding.voteNowTv.visibility = View.VISIBLE
                    binding.voteBtn.visibility = View.VISIBLE
                }
            }

    private companion object {
        private const val TAG = "VOTE_TAG"
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityAddVoteBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        if (::firebaseAuth.isInitialized) {
//            Toast.makeText(this, "Not Initilized", Toast.LENGTH_SHORT).show()
        } else {
            firebaseAuth = FirebaseAuth.getInstance()
        }

        try {
            candidateId = intent.getStringExtra("candidateId")!!
            if (candidateId.isNotEmpty()) {
                Toast.makeText(
                    this@AddVoteActivity,
                    "Candidate Selected Successfully:",
                    Toast.LENGTH_SHORT
                ).show()

            } else {
                Toast.makeText(this, "Candidate Id:Try Again", Toast.LENGTH_SHORT).show()

            }
        } catch (e: Exception) {

        }






        progressDialog = ProgressDialog(this)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.setMessage("Please wait...")

        binding.verifyBtn.setOnClickListener {
            checkVotes()
//            validateData()
        }

        binding.fingerPrintAuth.setOnClickListener {


            checkBiometricSupport()

            val biometricPrompt =
                BiometricPrompt.Builder(this)
                    .setTitle("Authentication")
                    .setSubtitle("Verify Using FingerPrint")
                    .setDescription("Authenticate To cast Vote")
                    .setNegativeButton(
                        "Cancel",
                        this@AddVoteActivity.mainExecutor,
                        DialogInterface.OnClickListener { dialog, which ->
                            Utills.toast(
                                this@AddVoteActivity,
                                "Authentication Cancelled"
                            )

                        }).build()
            biometricPrompt.authenticate(
                getCancellationSignal(),
                mainExecutor,
                authenticationCallback
            )

        }

        binding.voteBtn.setOnClickListener {
            incrementVoteViewCount()
            incrementUserVoteCount()

        }

        binding.faceRecAuth.setOnClickListener {
            requestPermission()
            binding.textView5.text = "Place Your head in The Middle Of Box And Capture Shot"
            dispatchTakePictureIntent()


        }
        binding.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this@AddVoteActivity, LogInActivity::class.java))
            finish()
        }
    }


    //    private var userId = firebaseAuth!!.uid
    private var cnicNumber = ""
    private var userCnicNumber = ""
    private var voteGiven = ""





//        if (voteGiven == "1") {
//            Toast.makeText(this@AddVoteActivity, "$voteGiven", Toast.LENGTH_SHORT).show()
//            Toast.makeText(
//                this@AddVoteActivity,
//                "Vote is Already Casted: You cannot cast Vote",
//                Toast.LENGTH_SHORT
//            ).show()



    private fun checkVotes() {

        firebaseAuth = FirebaseAuth.getInstance()

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}")
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    voteGiven = "${snapshot.child("voteGiven").value}"
                    Log.d(TAG, "onDataChange: $voteGiven")

                    if (voteGiven == "0" || voteGiven == "null") {
                        voteGiven = "0";
                        Log.d(TAG, "checkVotes: $voteGiven")
                        validateData()
                    } else {
                        Toast.makeText(
                            this@AddVoteActivity,
                            "Vote is Already Casted: You cannot cast Vote",
                            Toast.LENGTH_SHORT
                        ).show()
                        startActivity(Intent(this@AddVoteActivity, MainActivity::class.java))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@AddVoteActivity,
                        "Failed due to $error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })


    }


    private fun validateData() {
        cnicNumber = binding.cnicEt.text.toString().trim()

        firebaseAuth = FirebaseAuth.getInstance()


        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    userCnicNumber = "${snapshot.child("cnic").value}"
                    voteGiven = "${snapshot.child("voteGiven").value as? String}"


                }

                override fun onCancelled(error: DatabaseError) {

                }

            })


        if (cnicNumber.isEmpty()) {
            binding.cnicEt.requestFocus()
            binding.cnicEt.error = "Field Required"
        } else if (cnicNumber != userCnicNumber) {
            binding.cnicEt.requestFocus()
            binding.cnicEt.error = "CNIC Mismatch"
            Toast.makeText(this, "Mismatched", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "CNIC Matched", Toast.LENGTH_SHORT).show()
            binding.cnicEt.clearFocus()
//            checkVotes()
            binding.textView5.visibility = View.VISIBLE
            binding.fingerPrintAuth.visibility = View.VISIBLE
            binding.faceRecAuth.visibility = View.VISIBLE
            Toast.makeText(
                this@AddVoteActivity,
                "You are Eligible For Vote Casting:",
                Toast.LENGTH_SHORT
            ).show()


        }
//        else {
//            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
//        }

    }


    private fun getCancellationSignal(): CancellationSignal {
        cancellationSignal = CancellationSignal()
        cancellationSignal?.setOnCancelListener {
            Utills.toast(this@AddVoteActivity, "Authentication Cancelled by User")
        }

        return cancellationSignal as CancellationSignal
    }


    private fun checkBiometricSupport(): Boolean {

        val keyguardManager: KeyguardManager =
            getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        if (!keyguardManager.isKeyguardSecure) {
            Utills.toast(this@AddVoteActivity, "FingerPrint Authentication is Disabled")
            return false
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.USE_BIOMETRIC
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Utills.toast(this@AddVoteActivity, "FingerPrint Permission is not Granted")
            return false
        }

        return if (packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            true
        } else true

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
            } else {
                Log.d(TAG, "requestCameraPermission: All or either one is denied...")
                Utills.toast(this@AddVoteActivity, "Camera or Storage or both permission denied")
            }

        }

    private fun requestPermission() {
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


    }

    private fun incrementVoteViewCount() {
        //   1):  Get current vote viewCount
        val ref = FirebaseDatabase.getInstance().getReference("Candidates")
        ref.child(candidateId)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    //get views count
                    var voteCount = "${snapshot.child("voteCount").value}"

                    if (voteCount == "" || voteCount == "null") {
                        voteCount = "0";
                    }

                    // 2):Increment ViewsCount

                    val newViewsCount = voteCount.toLong() + 1

                    //setup data to update in db

                    val hashMap = HashMap<String, Any>()

                    hashMap["voteCount"] = newViewsCount

                    //set to db
                    val dbRef = FirebaseDatabase.getInstance().getReference("Candidates")
                    dbRef.child(candidateId)
                        .updateChildren(hashMap).addOnSuccessListener {


                        }


                }

                override fun onCancelled(error: DatabaseError) {

                }


            })
    }

    private fun incrementUserVoteCount() {
        val refUser = FirebaseDatabase.getInstance().getReference("Users")
        refUser.child("${firebaseAuth.uid}")


            .addListenerForSingleValueEvent(object : ValueEventListener {


                override fun onDataChange(snapshot: DataSnapshot) {

                    var voteGiven = "${snapshot.child("voteGiven").value}"

//                    if (voteGiven == "" || voteGiven == "null") {
//                        voteGiven = "0";
//                    }


                    // 2):Increment ViewsCount

                    for (x in 0 until 2) {

                        val newVoteGivenCount = voteGiven.toLong() + 1
                        val hashMapUser = HashMap<String, Any>()

                        hashMapUser["voteGiven"] = newVoteGivenCount

                        val dbRefUser =
                            FirebaseDatabase.getInstance().getReference("Users")
                        dbRefUser.child("${firebaseAuth.uid}")
                            .updateChildren(hashMapUser).addOnSuccessListener {
                                Toast.makeText(
                                    this@AddVoteActivity,
                                    "Your Vote Casted Successfully $newVoteGivenCount",
                                    Toast.LENGTH_SHORT
                                ).show()
                                startActivity(
                                    Intent(
                                        this@AddVoteActivity,
                                        MainActivity::class.java
                                    )
                                )

                            }

                        break;

                    }

                    //setup data to update in db


                }

                override fun onCancelled(error: DatabaseError) {

                }
            })


    }


    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            binding.imageView.setImageBitmap(imageBitmap)
            detectFaces(imageBitmap)
        }
    }

    private fun detectFaces(bitmap: Bitmap) {
        // Configure face detector q
        val options = FirebaseVisionFaceDetectorOptions.Builder()
            .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
            .setMinFaceSize(0.15f)
            .enableTracking()
            .build()


        // Create a FirebaseVisionImage object from a Bitmap object
        val image = FirebaseVisionImage.fromBitmap(bitmap)

        // Get an instance of FirebaseVisionFaceDetector
        val detector = FirebaseVision.getInstance()
            .getVisionFaceDetector(options)

        // Perform face detection
        detector.detectInImage(image)
            .addOnSuccessListener(OnSuccessListener<List<FirebaseVisionFace>> { faces ->
                // Task completed successfully
                // If there are no faces detected, faces will be empty
                for (face in faces) {
                    // Do something with the face
                    val bounds = face.boundingBox
                    val rotY = face.headEulerAngleY
                    val rotZ = face.headEulerAngleZ
                    Toast.makeText(
                        this@AddVoteActivity,
                        "FaceDetected Successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.voteBtn.visibility = View.VISIBLE
                    binding.textView5.visibility = View.VISIBLE

                    // You can also get other information like smile probability, left eye open probability, etc.
                }
            })
            .addOnFailureListener(OnFailureListener { e ->
                // Task failed with an exception
                Toast.makeText(
                    this@AddVoteActivity,
                    "Face detection failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            })
    }

}