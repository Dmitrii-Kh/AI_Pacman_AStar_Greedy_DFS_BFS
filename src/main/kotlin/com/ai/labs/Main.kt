package com.ai.labs

import java.awt.EventQueue


fun main() {
    EventQueue.invokeLater {
        val ex = Pacman()
        ex.isVisible = true
    }
}