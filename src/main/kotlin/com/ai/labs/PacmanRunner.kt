package com.ai.labs

interface PacmanRunner {
    var currState : State

    fun initState()
    fun findSon()
    fun checkObject(pos : Int) : Boolean
    fun pathWeight()
}