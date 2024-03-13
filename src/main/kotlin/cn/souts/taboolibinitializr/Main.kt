package cn.souts.taboolibinitializr

import taboolib.platform.App

object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        App.init()
        Initializr.start()
    }

}