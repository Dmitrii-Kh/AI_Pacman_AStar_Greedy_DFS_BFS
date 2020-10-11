package com.ai.labs

interface PacmanRunner {
//    val currState : State

    fun initState()
    fun findSon()
    fun checkObject(pos : Int) : Boolean
    fun pathWeight()
}