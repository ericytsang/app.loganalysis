package com.github.ericytsang.domain.appinfo

import java.io.File

interface AppInfoService
{
    fun getAppPackageName():String

    fun getUserHome():File

    fun getAndCreateAppHome():File

    companion object
    {
        val instance:AppInfoService by lazy { AppInfoServiceImpl() }
    }
}

private class AppInfoServiceImpl:AppInfoService
{
    override fun getAppPackageName():String = "com.github.ericytsang.loganalyzer"

    override fun getUserHome():File
    {
        val userHomePath = System.getProperty("user.home")
        return File(userHomePath)
    }

    override fun getAndCreateAppHome():File
    {
        val userHome = getUserHome()
        val appHome = File(userHome,".${getAppPackageName()}")
        appHome.mkdirs()
        return appHome
    }
}
