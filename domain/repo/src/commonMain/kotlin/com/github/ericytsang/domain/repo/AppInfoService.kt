package com.github.ericytsang.domain.repo

interface AppInfoService
{
    fun getAppPackageName():String
}

class AppInfoServiceImpl:AppInfoService
{
    override fun getAppPackageName():String = "com.github.ericytsang.loganalyzer"
}