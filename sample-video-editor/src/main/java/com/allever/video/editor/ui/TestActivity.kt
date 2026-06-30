//package com.videoeditor.ui
//
//import android.app.Activity
//import android.content.Context
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import android.view.View
//import android.widget.TextView
//import com.allever.video.editor.R
//import Base2Activity
//import ThumbnailBean
//import java.lang.StringBuilder
//
//class TestActivity: Base2Activity() {
//
//    companion object {
//
//
//        private val TAG = TestActivity::class.java.simpleName
//
//        private const val REQUEST_CODE_PICK = 0x01
//
//
//
//        private const val EXTRA_THUMBNAIL_LIST = "lsjtois"
//        fun startActivity(context: Context,thumbnailBeanList: ArrayList<ThumbnailBean>?){
//            val intent = Intent(context, TestActivity::class.java)
//            intent.putParcelableArrayListExtra(EXTRA_THUMBNAIL_LIST, thumbnailBeanList)
//            context.startActivity(intent)
//        }
//    }
//
//    private var mThumbnailBeanList: MutableList<ThumbnailBean> = mutableListOf()
//
//
//    private lateinit var mTvName: TextView
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_test)
//
//        mThumbnailBeanList.addAll(intent.getParcelableArrayListExtra(EXTRA_THUMBNAIL_LIST))
//
//        Log.d(TAG, "life onCreate()")
//
//        findViewById<View>(R.id.btn_select_pic).setOnClickListener {
//            AlbumActivity.startActivity(this, AlbumActivity.KEY_PICK, REQUEST_CODE_PICK)
//        }
//
//
//        mTvName = findViewById(R.id.tv_image_name)
//        val stringBuilder = StringBuilder()
//        for (i in 0 until mThumbnailBeanList.size){
//            stringBuilder.append(mThumbnailBeanList[i].path).append("\n")
//        }
//
//        mTvName.text = stringBuilder.toString()
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
////        super.onActivityResult(requestCode, resultCode, data)
//        Log.d(TAG, "onActivityResult()")
//        if (requestCode == REQUEST_CODE_PICK && resultCode == Activity.RESULT_OK){
//            Log.d(TAG, "onActivityResult() RESULT_OK")
//
//            val result = data?.getParcelableArrayListExtra<ThumbnailBean>("picked_data")
//            if (result != null){
//                mThumbnailBeanList.addAll(result)
//            }
//
//            val stringBuilder = StringBuilder()
//            for (i in 0 until mThumbnailBeanList.size){
//                stringBuilder.append(mThumbnailBeanList[i].path).append("\n")
//            }
//
//            mTvName.text = stringBuilder.toString()
//
//        }
//    }
//
//    override fun onNewIntent(intent: Intent?) {
//        super.onNewIntent(intent)
//        Log.d(TAG, "life onNewIntent()")
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        Log.d(TAG, "life life onDestroy()")
//    }
//}