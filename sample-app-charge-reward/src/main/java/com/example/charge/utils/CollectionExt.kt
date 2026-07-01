package com.example.charge.utils

/**
 * MutableList扩展方法：遍历列表，将满足条件的元素移到列表末尾
 * @param predicate 判断元素是否需要移动到末尾的条件函数
 * @return 返回处理后的列表，便于链式调用
 */
fun <T> MutableList<T>.moveElementsToEnd(predicate: (T) -> Boolean): MutableList<T> {
    // 首先收集所有需要移动的元素
    val elementsToMove = mutableListOf<T>()
    
    // 使用迭代器安全地移除元素，避免ConcurrentModificationException
    val iterator = this.iterator()
    while (iterator.hasNext()) {
        val element = iterator.next()
        if (predicate(element)) {
            iterator.remove()
            elementsToMove.add(element)
        }
    }
    
    // 将收集到的元素添加到列表末尾
    this.addAll(elementsToMove)
    
    return this
}