package dev.nyon.autodrop.extensions

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen

fun Minecraft.openScreen(parent: Screen?) { //? if >26.1
    gui.setScreen(parent) //? if <=26.1
    //setScreen(parent)
}

val Minecraft.currentScreen: Screen?
    get() { //? if >26.1
        return gui.screen() //? if <=26.1
        //return screen
    }
