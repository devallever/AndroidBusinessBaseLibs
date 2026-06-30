package com.allever.video.editor.function.editor.action

import com.allever.video.editor.function.Ratio
import com.allever.video.editor.function.editor.bean.EffectBean

interface IApplyAction {
    fun applyAction(action: Action?)
}

open class Action {
    var type = ActionType.TYPE_UNKNOWN
    var prevAction: Action? = null
    var restoreTips: String? = null
    var revertTips: String? = null

    open fun equalsByAction(other: Action?): Boolean {
        return true
    }

    open fun compareValue(other: Action?): Boolean {
        return other != null
    }

    open fun clone(action: Action): Action {
        action.prevAction = prevAction
        action.restoreTips = restoreTips
        action.revertTips = revertTips
        return action
    }

    open fun clone(): Action {
        return clone(Action())
    }
}


open class EffectAction(var effectBean: EffectBean? = null) : Action() {

    override fun equalsByAction(other: Action?): Boolean {
        return super.equalsByAction(other)
                && other is EffectAction
                && other.effectBean?.id == effectBean?.id
    }

    override fun clone(action: Action): Action {
        super.clone(action)
        (action as? EffectAction)?.let {
            action.effectBean = effectBean?.clone()
        }
        return action
    }

    override fun clone(): Action {
        return clone(EffectAction())
    }
}

open class AddEffectAction(effectBean: EffectBean? = null) : EffectAction(effectBean) {

    init {
        type = ActionType.TYPE_ADD
    }

    override fun clone(action: Action): Action {
        super.clone(action)
        (action as? AddEffectAction)?.let {
            action.effectBean = effectBean?.clone()
        }
        return action
    }

    override fun clone(): Action {
        return clone(AddEffectAction())
    }
}

open class DeleteEffectAction(effectBean: EffectBean? = null) : EffectAction(effectBean) {
    var index: Int = 0

    init {
        type = ActionType.TYPE_REMOVE
    }

    override fun clone(action: Action): Action {
        super.clone(action)
        (action as? DeleteEffectAction)?.let {
            action.effectBean = effectBean?.clone()
            action.index = index
        }
        return action
    }

    override fun clone(): Action {
        return clone(DeleteEffectAction())
    }
}

open class SwapEffectAction : Action() {
    var prevIds: List<Int> = ArrayList()
    var ids: List<Int> = ArrayList()

    init {
        type = ActionType.TYPE_SWAP
    }

    override fun clone(action: Action): Action {
        super.clone(action)
        (action as? SwapEffectAction)?.let {
            action.prevIds = ArrayList(prevIds)
            action.ids = ArrayList(ids)
        }
        return action
    }

    override fun clone(): Action {
        return clone(SwapEffectAction())
    }
}

class CropEffectAction(effectBean: EffectBean? = null) : EffectAction(effectBean) {
    init {
        type = ActionType.TYPE_TRIM
    }

    override fun clone(action: Action): Action {
        super.clone(action)
        (action as? CropEffectAction)?.let {
            action.effectBean = effectBean?.clone()
        }
        return action
    }

    override fun clone(): Action {
        return clone(CropEffectAction())
    }
}

class SingleEffectAction(effectBean: EffectBean? = null) : EffectAction(effectBean) {
    var prevObj: Any? = null
    var obj: Any? = null
    var currentObj: Any? = null

    override fun clone(action: Action): Action {
        super.clone(action)
        (action as? SingleEffectAction)?.let {
            action.effectBean = effectBean?.clone()
            action.prevObj = prevObj
            action.obj = obj
            action.currentObj = currentObj
        }
        return action
    }

    override fun clone(): Action {
        return clone(SingleEffectAction())
    }
}

class MultiEffectAction(effectBean: EffectBean? = null) : EffectAction(effectBean) {
    var prevObj: ArrayList<Any?>? = null
    var obj: ArrayList<Any?>? = null
    var currentObj: ArrayList<Any?>? = null

    override fun clone(action: Action): Action {
        super.clone(action)
        (action as? MultiEffectAction)?.let {
            action.effectBean = effectBean?.clone()
            val prevObj = prevObj
            action.prevObj = if (prevObj != null) {
                ArrayList(prevObj)
            } else null
            val obj = obj
            action.obj = if (obj != null) {
                ArrayList(obj)
            } else null
            val currentObj = currentObj
            action.currentObj = if (currentObj != null) {
                ArrayList(currentObj)
            } else null
        }
        return action
    }

    override fun clone(): Action {
        return clone(MultiEffectAction())
    }
}

/**
 * 单个特效view的动作
 */
class ViewAction : Action() {
    var effectBean: EffectBean? = null
    var left: Int? = null
    var top: Int? = null
    var right: Int? = null
    var bottom: Int? = null
    var translationX: Float? = null
    var translationY: Float? = null
    var pivotX: Float? = null
    var pivotY: Float? = null
    var rotation: Float? = null
    var scaleX: Float? = null
    var scaleY: Float? = null

    init {
        type = ActionType.TYPE_MOVE
    }

    override fun equalsByAction(other: Action?): Boolean {
        return super.equalsByAction(other)
                && other is ViewAction
                && other.effectBean?.id == effectBean?.id
    }

    override fun compareValue(other: Action?): Boolean {
        return super.compareValue(other)
                && other is ViewAction
                && other.effectBean?.id == effectBean?.id
                && other.left == left
                && other.top == top
                && other.right == right
                && other.bottom == bottom
                && other.translationX == translationX
                && other.translationY == translationY
                && other.pivotX == pivotX
                && other.pivotY == pivotY
                && other.rotation == rotation
                && other.scaleX == scaleX
                && other.scaleY == scaleY
    }

    override fun clone(action: Action): Action {
        val action = super.clone(action) as ViewAction
        action.effectBean = effectBean?.clone()
        action.left = left
        action.top = top
        action.right = right
        action.bottom = bottom
        action.translationX = translationX
        action.translationY = translationY
        action.pivotX = pivotX
        action.pivotY = pivotY
        action.rotation = rotation
        action.scaleX = scaleX
        action.scaleY = scaleY
        return action
    }

    override fun clone(): Action {
        return clone(ViewAction())
    }

}

