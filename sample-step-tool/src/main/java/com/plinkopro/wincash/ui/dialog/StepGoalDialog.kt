package com.plinkopro.wincash.ui.dialog
import android.content.Context
import android.os.Build
import android.view.View
import android.widget.SeekBar
import com.plinkopro.wincash.R
import com.plinkopro.wincash.base.BaseCenterPopupView
import com.plinkopro.wincash.business.step.StepBusiness
import com.plinkopro.wincash.databinding.DialogStepGoalBinding
import com.plinkopro.wincash.utils.setOnSingleListener

class StepGoalDialog(
    context: Context,
    private val currentGoal: Int,
    private val call: (Int) -> Unit,
) : BaseCenterPopupView(context) {
    private var selectValue = currentGoal

    override fun getImplLayoutId(): Int {
        return R.layout.dialog_step_goal
    }

    override fun onCreate() {

        DialogStepGoalBinding.bind(popupImplView).apply {
            
            listOf(centerSeekbar, topSeekbar).forEach { 
                it.apply {
                    max = 20
                    isEnabled = false
                    isClickable = false
                }
            }
            seekBar.apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) min = 1
                max = 20
                post {
                    // 初始对齐：用当前 progress 计算一次
                    alignLabelWithThumb(this, stepNumTv)
                }

                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(it: SeekBar, p: Int, fromUser: Boolean) {
                        val minVal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) it.min else 1
                        val valid = maxOf(minVal, minOf(p, it.max))
                        if (valid != it.progress) it.progress = valid

                        centerSeekbar.progress = valid
                        topSeekbar.progress = valid

                       selectValue = valid * 1000
                        stepNumTv.text = selectValue.toString()

                        // 位置对齐：用绝对坐标 + 正确的 scale 计算
                        alignLabelWithThumb(it, stepNumTv, progressOverride = valid)
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })

                progress = selectValue/1000
            }

            ivClose.setOnClickListener {
                dismiss()
            }
            makeSureTv.setOnSingleListener {
                StepBusiness.updateStepGoal(selectValue)
                call.invoke(selectValue)
                dismiss()
            }

        }

    }

    /** 将文字居中对齐到 thumb 上方（或下方），并做边界裁剪 */
    private fun alignLabelWithThumb(sb: SeekBar, label: View, progressOverride: Int? = null) {
        val progress = progressOverride ?: sb.progress
        val minVal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) sb.min else 0
        val maxVal = sb.max
        val range = (maxVal - minVal).coerceAtLeast(1)
        val scale = (progress - minVal).toFloat() / range

        val available = (sb.width - sb.paddingLeft - sb.paddingRight).toFloat()
        val thumbCenterXInSb = sb.paddingLeft + available * scale
        val thumbCenterXInParent = sb.x + thumbCenterXInSb

        // 确保 label 已经有宽度；若还没测量，延迟一次
        if (label.width == 0) {
            label.post { alignLabelWithThumb(sb, label, progress) }
            return
        }

        val labelHalfW = label.width / 2f
        val minX = sb.x
        val maxX = sb.x + sb.width - label.width
        val targetX = (thumbCenterXInParent - labelHalfW).coerceIn(minX, maxX)

        label.x = targetX
    }
}