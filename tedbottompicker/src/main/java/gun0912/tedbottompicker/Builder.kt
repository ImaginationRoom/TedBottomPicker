package gun0912.tedbottompicker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.*
import android.support.v4.content.ContextCompat
import java.util.*

class Builder(
        internal var previewMaxCount: Int = 25,

        @DrawableRes
        internal var cameraTileResId: Int? = null,
        @DrawableRes
        internal var galleryTileResId: Int? = null,
        @DrawableRes
        internal var deSelectIconResId: Int? = null,
        @DrawableRes
        internal var selectedForegroundResId: Int? = null,

        internal var spacing: Int = 0,
        internal var includeEdgeSpacing: Boolean = false,
        internal var showCamera: Boolean = true,
        internal var showGallery: Boolean = true,
        internal var peekHeight: Int = -1,

        @ColorRes
        internal var cameraTileBackgroundResId: Int = R.color.tedbottompicker_camera,
        @ColorRes
        internal var galleryTileBackgroundResId: Int = R.color.tedbottompicker_gallery,

        internal var title: String? = null,
        internal var showTitle: Boolean = true,

        @ColorRes
        internal var titleBackgroundResId: Int = 0,

        internal var selectMaxCount: Int = Integer.MAX_VALUE,
        internal var selectMinCount: Int = 0,

        internal var completeButtonText: String? = null,
        internal var emptySelectionText: String? = null,
        internal var selectMaxCountErrorText: String? = null,
        internal var selectMinCountErrorText: String? = null,

        @MediaType
        internal var mediaType: Int = MediaTypeValue.IMAGE,

        internal val selectedUriList: MutableList<Uri> = ArrayList(),
        internal var selectedUri: Uri? = null,
        internal var isMultiSelect: Boolean = false) : Parcelable {


    constructor(context: Context) : this() {
        setCameraTile(R.drawable.ic_camera)
        setGalleryTile(R.drawable.ic_gallery)
        setSpacingResId(context, R.dimen.tedbottompicker_grid_layout_margin)
    }

    fun setCameraTile(@DrawableRes cameraTileResId: Int) = apply {
        this.cameraTileResId = cameraTileResId
    }

    fun setGalleryTile(@DrawableRes galleryTileResId: Int) = apply {
        this.galleryTileResId = galleryTileResId
    }

    fun setDeSelectIcon(@DrawableRes deSelectIconResId: Int) = apply {
        this.deSelectIconResId = deSelectIconResId
    }

    fun setSelectedForeground(@DrawableRes selectedForegroundResId: Int) = apply {
        this.selectedForegroundResId = selectedForegroundResId
    }

    fun setPreviewMaxCount(previewMaxCount: Int) = apply {
        this.previewMaxCount = previewMaxCount
    }

    fun setSelectMaxCount(selectMaxCount: Int) = apply {
        this.selectMaxCount = selectMaxCount
    }

    fun setSelectMinCount(selectMinCount: Int) = apply {
        this.selectMinCount = selectMinCount
    }

    fun showCameraTile(showCamera: Boolean) = apply {
        this.showCamera = showCamera
    }

    fun showGalleryTile(showGallery: Boolean) = apply {
        this.showGallery = showGallery
    }

    fun setSpacing(spacing: Int) = apply {
        this.spacing = spacing
    }

    fun setIsMultiSelect(isMultiSelect: Boolean) = apply {
        this.isMultiSelect = isMultiSelect
    }

    fun setSpacingResId(context: Context, @DimenRes dimenResId: Int) = apply {
        setSpacing(context.resources.getDimensionPixelSize(dimenResId))
    }

    fun setIncludeEdgeSpacing(includeEdgeSpacing: Boolean) = apply {
        this.includeEdgeSpacing = includeEdgeSpacing
    }

    fun setPeekHeight(peekHeight: Int) = apply {
        this.peekHeight = peekHeight
    }

    fun setPeekHeightResId(context: Context, @DimenRes dimenResId: Int) = apply {
        setPeekHeight(context.resources.getDimensionPixelSize(dimenResId))
    }

    fun setCameraTileBackgroundResId(@ColorRes colorResId: Int) = apply {
        this.cameraTileBackgroundResId = colorResId
    }

    fun setGalleryTileBackgroundResId(@ColorRes colorResId: Int) = apply {
        this.galleryTileBackgroundResId = colorResId
    }

    fun setTitle(title: String) = apply {
        this.title = title
    }

    fun setTitle(context: Context, @StringRes stringResId: Int) = apply {
        setTitle(context.resources.getString(stringResId))
    }

    fun setShowTitle(showTitle: Boolean) = apply {
        this.showTitle = showTitle
    }

    fun setCompleteButtonText(completeButtonText: String) = apply {
        this.completeButtonText = completeButtonText
    }

    fun setCompleteButtonText(context: Context, @StringRes completeButtonResId: Int) = apply {
        setCompleteButtonText(context.resources.getString(completeButtonResId))
    }

    fun setEmptySelectionText(emptySelectionText: String) = apply {
        this.emptySelectionText = emptySelectionText
    }

    fun setEmptySelectionText(context: Context, @StringRes emptySelectionResId: Int) = apply {
        setEmptySelectionText(context.resources.getString(emptySelectionResId))
    }

    fun setSelectMaxCountErrorText(selectMaxCountErrorText: String) = apply {
        this.selectMaxCountErrorText = selectMaxCountErrorText
    }

    fun setSelectMaxCountErrorText(context: Context, @StringRes selectMaxCountErrorResId: Int) = apply {
        setSelectMaxCountErrorText(context.resources.getString(selectMaxCountErrorResId))
    }

    fun setSelectMinCountErrorText(selectMinCountErrorText: String) = apply {
        this.selectMinCountErrorText = selectMinCountErrorText
    }

    fun setSelectMinCountErrorText(context: Context, @StringRes selectMinCountErrorResId: Int) = apply {
        setSelectMinCountErrorText(context.resources.getString(selectMinCountErrorResId))
    }

    fun setTitleBackgroundResId(@ColorRes colorResId: Int) = apply {
        this.titleBackgroundResId = colorResId
    }

    fun setSelectedUriList(selectedUriList: ArrayList<Uri>) = apply {
        this.selectedUriList.clear()
        this.selectedUriList.addAll(selectedUriList)
    }

    fun setSelectedUri(selectedUri: Uri) = apply {
        this.selectedUri = selectedUri
    }

    fun showVideoMedia() = apply {
        this.mediaType = MediaTypeValue.VIDEO
    }

    fun create(context: Context): TedBottomPicker {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            throw RuntimeException("Missing required WRITE_EXTERNAL_STORAGE permission. Did you remember to request it first?")
        }

        if ((isMultiSelect && context !is OnMultiImageSelectedListener)
                || (!isMultiSelect && context !is OnImageSelectedListener)) {
            throw RuntimeException("You have implement either OnMultiImageSelectedListener() or OnImageSelectedListener() to receive selected Uri")
        }

        val bundle = Bundle()
        bundle.putParcelable("builder", this)

        val customBottomSheetDialogFragment = TedBottomPicker()
        customBottomSheetDialogFragment.arguments = bundle
        return customBottomSheetDialogFragment
    }


    @Retention(AnnotationRetention.SOURCE)
    @IntDef(MediaTypeValue.IMAGE, MediaTypeValue.VIDEO)
    annotation class MediaType {

    }

    object MediaTypeValue {
        const val IMAGE = 1
        const val VIDEO = 2
    }

    interface OnMultiImageSelectedListener {
        fun onImagesSelected(uriList: ArrayList<Uri>)
    }

    interface OnImageSelectedListener {
        fun onImageSelected(uri: Uri)
    }

    interface OnErrorListener {
        fun onError(message: String)
    }

    constructor(source: Parcel) : this(
            source.readInt(),
            source.readValue(Int::class.java.classLoader) as Int?,
            source.readValue(Int::class.java.classLoader) as Int?,
            source.readValue(Int::class.java.classLoader) as Int?,
            source.readValue(Int::class.java.classLoader) as Int?,
            source.readInt(),
            1 == source.readInt(),
            1 == source.readInt(),
            1 == source.readInt(),
            source.readInt(),
            source.readInt(),
            source.readInt(),
            source.readString(),
            1 == source.readInt(),
            source.readInt(),
            source.readInt(),
            source.readInt(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readInt(),
            source.createTypedArrayList(Uri.CREATOR)!!,
            source.readParcelable<Uri>(Uri::class.java.classLoader),
            1 == source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {

        writeInt(previewMaxCount)
        writeValue(cameraTileResId)
        writeValue(galleryTileResId)
        writeValue(deSelectIconResId)
        writeValue(selectedForegroundResId)
        writeValue(spacing)
        writeInt((if (includeEdgeSpacing) 1 else 0))
        writeInt((if (showCamera) 1 else 0))
        writeInt((if (showGallery) 1 else 0))
        writeInt(peekHeight)
        writeInt(cameraTileBackgroundResId)
        writeInt(galleryTileBackgroundResId)
        writeString(title)
        writeInt((if (showTitle) 1 else 0))
        writeInt(titleBackgroundResId)
        writeInt(selectMaxCount)
        writeInt(selectMinCount)
        writeString(completeButtonText)
        writeString(emptySelectionText)
        writeString(selectMaxCountErrorText)
        writeString(selectMinCountErrorText)
        writeInt(mediaType)
        writeTypedList(selectedUriList)
        writeParcelable(selectedUri, 0)
        writeInt((if (isMultiSelect) 1 else 0))
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Builder> = object : Parcelable.Creator<Builder> {
            override fun createFromParcel(source: Parcel): Builder = Builder(source)
            override fun newArray(size: Int): Array<Builder?> = arrayOfNulls(size)
        }
    }
}
