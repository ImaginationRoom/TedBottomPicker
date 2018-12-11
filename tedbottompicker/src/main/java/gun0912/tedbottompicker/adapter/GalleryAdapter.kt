package gun0912.tedbottompicker.adapter

import android.content.Context
import android.database.Cursor
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import android.support.annotation.IntDef
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

import java.io.File
import java.util.ArrayList

import gun0912.tedbottompicker.Builder
import gun0912.tedbottompicker.R
import gun0912.tedbottompicker.TedBottomPicker
import gun0912.tedbottompicker.view.TedSquareFrameLayout
import gun0912.tedbottompicker.view.TedSquareImageView

/**
 * Created by TedPark on 2016. 8. 30..
 */
class GalleryAdapter(internal var context: Context, internal var builder: Builder) : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {


    internal var pickerTiles: ArrayList<PickerTile>
    internal var onItemClickListener: OnItemClickListener? = null
    internal var selectedUriList: ArrayList<Uri>


    init {

        pickerTiles = ArrayList()
        selectedUriList = ArrayList()

        if (builder.showCamera) {
            pickerTiles.add(PickerTile(PickerTile.CAMERA))
        }

        if (builder.showGallery) {
            pickerTiles.add(PickerTile(PickerTile.GALLERY))
        }

        var cursor: Cursor? = null
        try {
            val columns: Array<String>
            val orderBy: String
            val uri: Uri
            if (builder.mediaType == Builder.MediaTypeValue.IMAGE) {
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                columns = arrayOf(MediaStore.Images.Media.DATA)
                orderBy = MediaStore.Images.Media.DATE_ADDED + " DESC"
            } else {
                uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                columns = arrayOf(MediaStore.Video.VideoColumns.DATA)
                orderBy = MediaStore.Video.VideoColumns.DATE_ADDED + " DESC"
            }




            cursor = context.applicationContext.contentResolver.query(uri, columns, null, null, orderBy)
            //imageCursor = sContext.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);


            if (cursor != null) {

                var count = 0
                while (cursor.moveToNext() && count < builder.previewMaxCount) {

                    val dataIndex: String
                    if (builder.mediaType == Builder.MediaTypeValue.IMAGE) {
                        dataIndex = MediaStore.Images.Media.DATA
                    } else {
                        dataIndex = MediaStore.Video.VideoColumns.DATA
                    }
                    val imageLocation = cursor.getString(cursor.getColumnIndex(dataIndex))
                    val imageFile = File(imageLocation)
                    pickerTiles.add(PickerTile(Uri.fromFile(imageFile)))
                    count++

                }

            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (cursor != null && !cursor.isClosed) {
                cursor.close()
            }
        }


    }

    fun setSelectedUriList(selectedUriList: ArrayList<Uri>, uri: Uri) {
        this.selectedUriList = selectedUriList

        var position = -1


        var pickerTile: PickerTile
        for (i in pickerTiles.indices) {
            pickerTile = pickerTiles[i]
            if (pickerTile.isImageTile && pickerTile.imageUri == uri) {
                position = i
                break
            }

        }


        if (position > 0) {
            notifyItemChanged(position)
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val view = View.inflate(context, R.layout.tedbottompicker_grid_item, null)


        return GalleryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {

        val pickerTile = getItem(position)


        var isSelected = false

        if (pickerTile.isCameraTile) {
            holder.iv_thumbnail.setBackgroundResource(builder.cameraTileBackgroundResId)
            holder.iv_thumbnail.setImageDrawable(ContextCompat.getDrawable(context, builder.cameraTileResId!!))
        } else if (pickerTile.isGalleryTile) {
            holder.iv_thumbnail.setBackgroundResource(builder.galleryTileBackgroundResId)
            holder.iv_thumbnail.setImageDrawable(ContextCompat.getDrawable(context, builder.galleryTileResId!!))

        } else {
            val uri = pickerTile.imageUri
                Glide.with(context)
                        .load(uri)
                        .thumbnail(0.1f)
                        .apply(RequestOptions().centerCrop()
                                .placeholder(R.drawable.ic_gallery)
                                .error(R.drawable.img_error))
                        .into(holder.iv_thumbnail)


            isSelected = selectedUriList.contains(uri)
        }


        val foregroundDrawable: Drawable?

        if (builder.selectedForegroundResId != null) {
            foregroundDrawable = ContextCompat.getDrawable(context, builder.selectedForegroundResId!!)
        } else {
            foregroundDrawable = ContextCompat.getDrawable(context, R.drawable.gallery_photo_selected)
        }

        (holder.root as FrameLayout).foreground = if (isSelected) foregroundDrawable else null

        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener { onItemClickListener!!.onItemClick(holder.itemView, position) }
        }
    }

    fun getItem(position: Int): PickerTile {
        return pickerTiles[position]
    }

    override fun getItemCount(): Int {
        return pickerTiles.size
    }

    fun setOnItemClickListener(
            onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }


    class PickerTile(val imageUri: Uri?, val tileType: Int) {

        val isImageTile: Boolean
            get() = tileType == IMAGE

        val isCameraTile: Boolean
            get() = tileType == CAMERA

        val isGalleryTile: Boolean
            get() = tileType == GALLERY

        internal constructor(@SpecialTileType tileType: Int) : this(null, tileType) {}

        internal constructor(imageUri: Uri) : this(imageUri, IMAGE) {}

        override fun toString(): String {
            return if (isImageTile) {
                "ImageTile: " + imageUri!!
            } else if (isCameraTile) {
                "CameraTile"
            } else if (isGalleryTile) {
                "PickerTile"
            } else {
                "Invalid item"
            }
        }

        @IntDef(IMAGE, CAMERA, GALLERY)
        @Retention(AnnotationRetention.SOURCE)
        annotation class TileType

        @IntDef(CAMERA, GALLERY)
        @Retention(AnnotationRetention.SOURCE)
        annotation class SpecialTileType


        companion object {

            const val IMAGE = 1
            const val CAMERA = 2
            const val GALLERY = 3
        }

    }


    inner class GalleryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var root: TedSquareFrameLayout = view.findViewById<View>(R.id.root) as TedSquareFrameLayout
        var iv_thumbnail: TedSquareImageView = view.findViewById<View>(R.id.iv_thumbnail) as TedSquareImageView
    }


}
