package gun0912.tedbottompicker

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.gun0912.tedonactivityresult.TedOnActivityResult
import gun0912.tedbottompicker.adapter.GalleryAdapter
import gun0912.tedbottompicker.util.RealPathUtil
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class TedBottomPicker : BottomSheetDialogFragment() {
    private lateinit var imageGalleryAdapter: GalleryAdapter
    private lateinit var view_title_container: View
    private lateinit var tv_title: TextView
    private lateinit var btn_done: Button

    private lateinit var selected_photos_container_frame: FrameLayout
    private lateinit var hsv_selected_photos: HorizontalScrollView
    private lateinit var selected_photos_container: LinearLayout

    private lateinit var selected_photos_empty: TextView
    private lateinit var contentView: View
    private lateinit var rc_gallery: RecyclerView


    private val selectedUriList = ArrayList<Uri>()
    private val tempUriList = ArrayList<Uri>()
    private var cameraImageUri: Uri? = null
    private var mContext: Context? = null


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        this.mContext = context
    }

    override fun onDetach() {
        super.onDetach()
        this.mContext = null
    }

    private val mBottomSheetBehaviorCallback = object : BottomSheetBehavior.BottomSheetCallback() {

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            Log.d(TAG, "onStateChanged() newState: $newState")
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismissAllowingStateLoss()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            Log.d(TAG, "onSlide() slideOffset: $slideOffset")
        }
    }

    private// Create an image file name
            /* prefix *//* suffix *//* directory */// Save a file: path for use with ACTION_VIEW intents
    val imageFile: File?
        get() {
            var imageFile: File? = null
            try {
                val timeStamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
                val imageFileName = "JPEG_" + timeStamp + "_"
                val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

                if (!storageDir.exists())
                    storageDir.mkdirs()

                imageFile = File.createTempFile(
                        imageFileName,
                        ".jpg",
                        storageDir
                )
                cameraImageUri = Uri.fromFile(imageFile)
                tempUriList.add(cameraImageUri!!)
            } catch (e: IOException) {
                e.printStackTrace()
                errorMessage("Could not create imageFile for camera")
            }


            return imageFile
        }

    private// Create an image file name
            /* prefix *//* suffix *//* directory */// Save a file: path for use with ACTION_VIEW intents
    val videoFile: File?
        get() {
            var videoFile: File? = null
            try {
                val timeStamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
                val imageFileName = "VIDEO_" + timeStamp + "_"
                val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)

                if (!storageDir.exists())
                    storageDir.mkdirs()

                videoFile = File.createTempFile(
                        imageFileName,
                        ".mp4",
                        storageDir
                )
                cameraImageUri = Uri.fromFile(videoFile)
                tempUriList.add(cameraImageUri!!)
            } catch (e: IOException) {
                e.printStackTrace()
                errorMessage("Could not create imageFile for camera")
            }


            return videoFile
        }


    private val builder: Builder? by lazy { arguments?.getParcelable<Builder>("builder") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupSavedInstanceState(savedInstanceState)

        //  setRetainInstance(true);
    }

    private fun setupSavedInstanceState(savedInstanceState: Bundle?) {


        if (savedInstanceState == null) {
            cameraImageUri = builder!!.selectedUri
            tempUriList.clear()
            tempUriList.addAll(builder!!.selectedUriList)
        } else {
            cameraImageUri = savedInstanceState.getParcelable(EXTRA_CAMERA_IMAGE_URI)
            tempUriList.clear()
            savedInstanceState.getParcelableArrayList<Uri>(EXTRA_CAMERA_SELECTED_IMAGE_URI)?.also {
                tempUriList.addAll(it)
            }
        }


    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(EXTRA_CAMERA_IMAGE_URI, cameraImageUri)
        outState.putParcelableArrayList(EXTRA_CAMERA_SELECTED_IMAGE_URI, tempUriList)
        super.onSaveInstanceState(outState)

    }

    fun show(fragmentManager: FragmentManager) {

        val ft = fragmentManager.beginTransaction()
        ft.add(this, tag)
        ft.commitAllowingStateLoss()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        contentView = inflater.inflate(R.layout.tedbottompicker_content_view, container, false)

        initView(contentView)
        setTitle()
        setRecyclerView()
        setSelectionView()

        if (!builder!!.isMultiSelect) {
            cameraImageUri?.also { addUri(it) }
        } else {
            tempUriList.forEach {
                addUri(it)
            }
        }

        setDoneButton()
        checkMultiMode()

        return contentView
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { it ->
            val d = it as BottomSheetDialog
            val bottomSheetInternal = d.findViewById<View>(android.support.design.R.id.design_bottom_sheet)

            val behavior = BottomSheetBehavior.from<View>(bottomSheetInternal)
            behavior.setBottomSheetCallback(mBottomSheetBehaviorCallback)
            if (builder != null && builder!!.peekHeight > 0) {
                behavior.peekHeight = builder!!.peekHeight
            }
        }
        return dialog
    }

    private fun setSelectionView() {

        if (builder!!.emptySelectionText != null) {
            selected_photos_empty.text = builder!!.emptySelectionText
        }
    }

    private fun setDoneButton() {

        if (builder!!.completeButtonText != null) {
            btn_done.text = builder!!.completeButtonText
        }

        btn_done.setOnClickListener { onMultiSelectComplete() }
    }

    private fun onMultiSelectComplete() {

        if (selectedUriList.size < builder!!.selectMinCount) {
            val message: String?
            if (builder!!.selectMinCountErrorText != null) {
                message = builder!!.selectMinCountErrorText
            } else {
                message = String.format(resources.getString(R.string.select_min_count), builder!!.selectMinCount)
            }

            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
            return
        }


        (mContext as? Builder.OnMultiImageSelectedListener)?.onImagesSelected(selectedUriList)
        dismissAllowingStateLoss()
    }

    private fun checkMultiMode() {
        if (!builder!!.isMultiSelect) {
            btn_done.visibility = View.GONE
            selected_photos_container_frame.visibility = View.GONE
        }

    }

    private fun initView(contentView: View) {

        view_title_container = contentView.findViewById(R.id.view_title_container)
        rc_gallery = contentView.findViewById<View>(R.id.rc_gallery) as RecyclerView
        tv_title = contentView.findViewById<View>(R.id.tv_title) as TextView
        btn_done = contentView.findViewById<View>(R.id.btn_done) as Button

        selected_photos_container_frame = contentView.findViewById<View>(R.id.selected_photos_container_frame) as FrameLayout
        hsv_selected_photos = contentView.findViewById<View>(R.id.hsv_selected_photos) as HorizontalScrollView
        selected_photos_container = contentView.findViewById<View>(R.id.selected_photos_container) as LinearLayout
        selected_photos_empty = contentView.findViewById<View>(R.id.selected_photos_empty) as TextView
    }

    private fun setRecyclerView() {

        val gridLayoutManager = GridLayoutManager(mContext, 3)
        rc_gallery.layoutManager = gridLayoutManager
        rc_gallery.addItemDecoration(GridSpacingItemDecoration(gridLayoutManager.spanCount, builder!!.spacing, builder!!.includeEdgeSpacing))
        updateAdapter()
    }

    private fun updateAdapter() {

        imageGalleryAdapter = GalleryAdapter(mContext!!, builder!!)
        rc_gallery.adapter = imageGalleryAdapter
        imageGalleryAdapter.setOnItemClickListener(object : GalleryAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {

                val pickerTile = imageGalleryAdapter.getItem(position)

                when (pickerTile.tileType) {
                    GalleryAdapter.PickerTile.CAMERA -> startCameraIntent()
                    GalleryAdapter.PickerTile.GALLERY -> startGalleryIntent()
                    GalleryAdapter.PickerTile.IMAGE -> complete(pickerTile.imageUri!!)

                    else -> errorMessage()
                }

            }
        })
    }

    private fun complete(uri: Uri) {
        Log.d(TAG, "selected uri: " + uri.toString())
        //uri = Uri.parse(uri.toString());
        if (builder!!.isMultiSelect) {

            if (selectedUriList.contains(uri)) {
                removeImage(uri)
            } else {
                addUri(uri)
            }


        } else {
            (mContext as? Builder.OnImageSelectedListener)?.onImageSelected(uri)
            dismissAllowingStateLoss()
        }

    }

    private fun addUri(uri: Uri): Boolean {


        if (selectedUriList.size == builder!!.selectMaxCount) {
            val message: String?
            if (builder!!.selectMaxCountErrorText != null) {
                message = builder!!.selectMaxCountErrorText
            } else {
                message = String.format(resources.getString(R.string.select_max_count), builder!!.selectMaxCount)
            }

            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
            return false
        }


        selectedUriList.add(uri)

        val rootView = LayoutInflater.from(mContext).inflate(R.layout.tedbottompicker_selected_item, null)
        val thumbnail = rootView.findViewById<View>(R.id.selected_photo) as ImageView
        val iv_close = rootView.findViewById<View>(R.id.iv_close) as ImageView
        rootView.tag = uri

        selected_photos_container.addView(rootView, 0)


        val px = resources.getDimension(R.dimen.tedbottompicker_selected_image_height).toInt()
        thumbnail.layoutParams = FrameLayout.LayoutParams(px, px)
        Glide.with(mContext!!)
                .load(uri)
                .thumbnail(0.1f)
                .apply(RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.ic_gallery)
                        .error(R.drawable.img_error))
                .into(thumbnail)



        builder!!.deSelectIconResId?.also {
            iv_close.setImageDrawable(ContextCompat.getDrawable(mContext!!, it))
        }

        iv_close.setOnClickListener { removeImage(uri) }


        updateSelectedView()
        imageGalleryAdapter.setSelectedUriList(selectedUriList, uri)
        return true

    }

    private fun removeImage(uri: Uri) {

        selectedUriList.remove(uri)


        for (i in 0 until selected_photos_container.childCount) {
            val childView = selected_photos_container.getChildAt(i)


            if (childView.tag == uri) {
                selected_photos_container.removeViewAt(i)
                break
            }
        }

        updateSelectedView()
        imageGalleryAdapter.setSelectedUriList(selectedUriList, uri)
    }

    private fun updateSelectedView() {

        if (selectedUriList.size == 0) {
            selected_photos_empty.visibility = View.VISIBLE
            selected_photos_container.visibility = View.GONE
        } else {
            selected_photos_empty.visibility = View.GONE
            selected_photos_container.visibility = View.VISIBLE
        }

    }

    private fun startCameraIntent() {
        val cameraInent: Intent
        val mediaFile: File?

        if (builder!!.mediaType == Builder.MediaTypeValue.IMAGE) {
            cameraInent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            mediaFile = imageFile
        } else {
            cameraInent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            mediaFile = videoFile
        }

        if (cameraInent.resolveActivity(mContext!!.packageManager) == null) {
            errorMessage("This Application do not have Camera Application")
            return
        }


        val photoURI = FileProvider.getUriForFile(mContext!!, mContext!!.applicationContext.packageName + ".provider", mediaFile!!)

        val resolvedIntentActivities = mContext!!.packageManager.queryIntentActivities(cameraInent, PackageManager.MATCH_DEFAULT_ONLY)
        for (resolvedIntentInfo in resolvedIntentActivities) {
            val packageName = resolvedIntentInfo.activityInfo.packageName
            mContext!!.grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        cameraInent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

        TedOnActivityResult.with(mContext)
                .setIntent(cameraInent)
                .setListener { resultCode, data ->
                    if (resultCode == Activity.RESULT_OK) {
                        onActivityResultCamera(cameraImageUri!!)
                    }
                }
                .startActivityForResult()
    }

    private fun errorMessage(message: String? = null) {
        val errorMessage = message ?: "Something wrong."

        Toast.makeText(mContext, errorMessage, Toast.LENGTH_SHORT).show()
    }

    private fun startGalleryIntent() {
        val galleryIntent: Intent
        val uri: Uri
        if (builder!!.mediaType == Builder.MediaTypeValue.IMAGE) {
            galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryIntent.type = "image/*"
        } else {
            galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            galleryIntent.type = "video/*"

        }

        if (galleryIntent.resolveActivity(mContext!!.packageManager) == null) {
            errorMessage("This Application do not have Gallery Application")
            return
        }

        TedOnActivityResult.with(mContext)
                .setIntent(galleryIntent)
                .setListener { resultCode, data ->
                    if (resultCode == Activity.RESULT_OK) {
                        onActivityResultGallery(data)
                    }
                }
                .startActivityForResult()
    }

    private fun setTitle() {

        if (!builder!!.showTitle) {
            tv_title.visibility = View.GONE

            if (!builder!!.isMultiSelect) {
                view_title_container.visibility = View.GONE
            }

            return
        }

        if (!TextUtils.isEmpty(builder!!.title)) {
            tv_title.text = builder!!.title
        }

        if (builder!!.titleBackgroundResId > 0) {
            tv_title.setBackgroundResource(builder!!.titleBackgroundResId)
        }

    }

    private fun onActivityResultCamera(cameraImageUri: Uri) {

        if (mContext != null)
            MediaScannerConnection.scanFile(mContext, arrayOf(cameraImageUri.path), arrayOf("image/jpeg"), object : MediaScannerConnection.MediaScannerConnectionClient {
                override fun onMediaScannerConnected() {

                }

                override fun onScanCompleted(s: String, uri: Uri) {
                    (mContext as? Activity)!!.runOnUiThread {
                        updateAdapter()
                        complete(cameraImageUri)
                    }

                }
            })
    }


    private fun onActivityResultGallery(data: Intent) {
        val temp = data.data

        if (temp == null) {
            errorMessage()
        }

        val realPath = RealPathUtil.getRealPath(mContext!!, temp)

        var selectedImageUri: Uri? = null
        try {
            selectedImageUri = Uri.fromFile(File(realPath))
        } catch (ex: Exception) {
            selectedImageUri = Uri.parse(realPath)
        }

        complete(selectedImageUri!!)

    }

    companion object {

        val TAG = "TedBottomPicker"
        internal val EXTRA_CAMERA_IMAGE_URI = "camera_image_uri"
        internal val EXTRA_CAMERA_SELECTED_IMAGE_URI = "camera_selected_image_uri"
    }
}
