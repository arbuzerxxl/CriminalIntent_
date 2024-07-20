package com.example.criminalintent_

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.ViewModelProvider
import java.io.File
import android.text.format.DateFormat as DF
import java.util.Date
import java.util.UUID
import android.Manifest
import android.app.Activity
import android.provider.Settings
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

private const val TAG = "CrimeFragment"
private const val ARG_CRIME_ID = "crime_id"
private const val REQUEST_DATE = "DialogDate"
private const val REQUEST_TIME = "DialogTime"
private const val DATE_FORMAT = "EEEE, dd MMMM, yyyy, HH:mm"

// controller - C
class CrimeFragment : Fragment(), FragmentResultListener {

    private lateinit var crime: Crime
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var requiresToPoliceCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var suspectPhoneButton: Button
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri


    private val crimeDetailViewModel: CrimeDetailViewModel by lazy { ViewModelProvider(this)[CrimeDetailViewModel::class.java] }
    private val getSuspect = registerForActivityResult(ActivityResultContracts.PickContact()) { uri: Uri? ->
        if (uri != null) {
            getContactName(uri)?.let {
                val (suspect, suspectID) = it
                crime.suspect = suspect
                suspectButton.text = suspect

                getContactPhone(suspectID)?.let { phone ->
                    crime.suspectPhoneNumber = phone
                }
                crimeDetailViewModel.saveCrime(crime)
            }
        }
    }
    private val savePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) { photoSaved: Boolean ->
        if (photoSaved) {
            Log.d(TAG, "Photo was saved")
            updatePhotoView()
            requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            photoView.announceForAccessibility(getString(R.string.crime_photo_added))
            photoView.isEnabled = true
        }
    }

    private val getContactsPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->

        if (result[Manifest.permission.READ_CONTACTS] == true) {
            suspectButton.setOnClickListener {
                getSuspect.launch(null)
            }
            suspectButton.isEnabled = true
        }

        if (result[Manifest.permission.READ_CONTACTS] == false) {

            suspectButton.setOnClickListener {
                Snackbar.make(requireView(), R.string.contacts_permissions, Snackbar.LENGTH_LONG).setAction(
                    "SETTINGS",
                    View.OnClickListener {
                        getApplicationSettings.launch(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:" + requireActivity().packageName)
                            )
                        )
                        Toast.makeText(context, R.string.contacts_permissions_detail, Toast.LENGTH_SHORT).show()

                    }).show()
            }

        }
    }

    private val getApplicationSettings = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requireActivity().checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, R.string.contacts_permissions_granted, Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(context, R.string.contacts_permissions_not_granted, Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        fun newInstance(crimeId: UUID): CrimeFragment {
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)
            }
            return CrimeFragment().apply {
                arguments = args
            }
        }
    }

// Bundle - entity for save and load State of App

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeId: UUID? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable(ARG_CRIME_ID, UUID::class.java)
        } else {
            arguments?.getSerializable(ARG_CRIME_ID) as UUID
        }
        Log.d(TAG, "args bundle crime ID: $crimeId")

        crimeId?.let { crimeDetailViewModel.loadCrime(crimeId) }

    }

    // fill layout fragment_crime.xml
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)
        titleField = view.findViewById(R.id.crime_title)
        dateButton = view.findViewById(R.id.crime_date)
        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(crime.date, REQUEST_DATE).apply {
                show(this@CrimeFragment.parentFragmentManager, TAG)
            }
        }
        timeButton = view.findViewById(R.id.crime_time)
        timeButton.setOnClickListener {
            TimePickerFragment.newInstance(crime.date, REQUEST_TIME).apply {
                show(this@CrimeFragment.parentFragmentManager, TAG)
            }
        }
        solvedCheckBox = view.findViewById(R.id.crime_solved)
        requiresToPoliceCheckBox = view.findViewById(R.id.crime_requires_to_police)
        solvedCheckBox.apply {
            isEnabled = !crime.requiresPolice
            setOnCheckedChangeListener { _, isChecked ->
                requiresToPoliceCheckBox.isEnabled = !isChecked
                crime.isSolved = isChecked
                jumpDrawablesToCurrentState()
            }
        }
        requiresToPoliceCheckBox.apply {
            isEnabled = !crime.isSolved
            setOnCheckedChangeListener { _, isChecked ->
                solvedCheckBox.isEnabled = !isChecked
                crime.requiresPolice = isChecked
                jumpDrawablesToCurrentState()
            }
        }
        reportButton = view.findViewById(R.id.crime_report)
        suspectButton = view.findViewById(R.id.crime_suspect)
        suspectPhoneButton = view.findViewById(R.id.crime_suspect_phone)
        photoButton = view.findViewById(R.id.crime_camera)
        photoView = view.findViewById(R.id.crime_photo)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeDetailViewModel.crimeLiveData.observe(viewLifecycleOwner) { crime ->
            crime?.let {
                this.crime = crime
                photoFile = crimeDetailViewModel.getPhotoFile(crime)
                photoUri = FileProvider.getUriForFile(
                    requireActivity(),
                    "com.example.criminalintent_.fileprovider", photoFile
                )
                updateUI()
            }
        }

        parentFragmentManager.setFragmentResultListener(REQUEST_DATE, viewLifecycleOwner, this@CrimeFragment)
        parentFragmentManager.setFragmentResultListener(REQUEST_TIME, viewLifecycleOwner, this@CrimeFragment)
    }

    override fun onStart() {

        super.onStart()
        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // method is empty
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                crime.title = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
                // method is empty
            }
        }
        titleField.addTextChangedListener(titleWatcher)
        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))
            }.also { intent ->
                val chooserIntent = Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }
        suspectButton.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (context.checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    setOnClickListener {
                        getSuspect.launch(null)
                    }
                } else {
                    getContactsPermissions.launch(arrayOf(Manifest.permission.READ_CONTACTS))
                }
            } else {
                setOnClickListener {
                    getSuspect.launch(null)
                }
            }

        }
        suspectPhoneButton.setOnClickListener {
            val callSuspectIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${crime.suspectPhoneNumber}"))
            startActivity(callSuspectIntent)

            if (crime.suspectPhoneNumber.isBlank()) it.isEnabled = false
        }
        photoButton.apply {
            val packageManager: PackageManager = requireActivity().packageManager
            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            setOnClickListener {
                val cameraActivities: List<ResolveInfo> = packageManager.queryIntentActivities(captureImage, PackageManager.MATCH_DEFAULT_ONLY)
                for (cameraActivity in cameraActivities) {
                    requireActivity().grantUriPermission(
                        cameraActivity.activityInfo.packageName,
                        photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }
                savePhoto.launch(photoUri)
            }
        }

    }


    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        when (requestKey) {

            REQUEST_DATE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    crime.date = result.getSerializable(REQUEST_DATE, Date::class.java)!!
                } else {
                    crime.date = result.getSerializable(REQUEST_DATE)!! as Date
                }
            }

            REQUEST_TIME -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    crime.date = result.getSerializable(REQUEST_TIME, Date::class.java)!!
                } else {
                    crime.date = result.getSerializable(REQUEST_TIME)!! as Date
                }
            }

        }
        updateUI()
    }

    private fun updateUI() {
        titleField.setText(crime.title)

        val date = DF.getMediumDateFormat(context).format(crime.date)
        val time = DF.getTimeFormat(context).format(crime.date)
        dateButton.text = date
        timeButton.text = time

        solvedCheckBox.isChecked = crime.isSolved
        requiresToPoliceCheckBox.isChecked = crime.requiresPolice
        if (crime.suspect.isNotEmpty()) {
            suspectButton.text = crime.suspect
        }
        suspectPhoneButton.apply {
            if (crime.suspectPhoneNumber.isNotEmpty()) {
                text = crime.suspectPhoneNumber
                isEnabled = true
            } else {
                isEnabled = false
                contentDescription = getString(R.string.crime_call_suspect_is_disabled)
            }
        }

        photoView.setOnClickListener {
            val fragment = PhotoFragment.newInstance(photoUri)
            requireActivity().supportFragmentManager.beginTransaction().replace(
                R.id.fragment_container,
                fragment
            ).addToBackStack(null).commit()
        }
        updatePhotoView()
    }

    private fun getCrimeReport(): String {
        val solved = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }
        val requirePolice = if (crime.requiresPolice) {
            getString(R.string.crime_report_required_police)
        } else {
            getString(R.string.crime_report_unrequired_police)
        }
        val date = DF.format(DATE_FORMAT, crime.date).toString()
        var suspect = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }
        return getString(R.string.crime_report, crime.title, date, solved, suspect, requirePolice)
    }

    private fun getContactPhone(suspectID: String): String? {
        val cursorPhone = requireActivity().contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + suspectID,
            null,
            null
        )

        return cursorPhone?.use { phoneCursor ->
            if (phoneCursor.count == 0) {
                null
            }
            phoneCursor.moveToFirst()
            val column = cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            if (column != -1) {
                phoneCursor.getString(column)
            } else null
        }

    }

    private fun getContactName(uri: Uri): Array<String>? {

        val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID)

        val cursor = requireActivity().contentResolver.query(uri, queryFields, null, null, null)
        return cursor?.use {
            if (it.count == 0) {
                return null
            }
            it.moveToFirst()
            val suspect = it.getString(0)
            val suspectId = it.getString(1)
            arrayOf(suspect, suspectId)
        }
    }

    private fun updatePhotoView() {

        if (photoFile.exists()) {
            val bitmap = getScaledBitmap(photoFile.path, photoView.width, photoView.height)
            photoView.setImageBitmap(bitmap)
            photoView.contentDescription =
                getString(R.string.crime_photo_image_description)
            photoView.rotation = 90.0F  //need create getCameraPhotoOrientation()
        } else {
            photoView.setImageDrawable(null)
            photoView.apply { isEnabled = false }
            photoView.contentDescription =
                getString(R.string.crime_photo_no_image_description)
        }
    }

}