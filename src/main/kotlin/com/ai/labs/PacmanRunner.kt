package com.ai.labs

abstract class PacmanRunner() {
//    init {
//        println("It's $algorithm!")
//    }

//    abstract var inGame: Boolean
//    abstract var dying: Boolean

    // state and currState

    abstract fun findParent()
    abstract fun checkObject()
    abstract fun pathWeight()

}