package com.github.ericytsang.domain.repo.service

interface AppInfoService
{
    fun getAppPackageName():String

    companion object
    {
        val instance:AppInfoService by lazy { AppInfoServiceImpl() }
    }
}

private class AppInfoServiceImpl:AppInfoService
{
    override fun getAppPackageName():String = "com.github.ericytsang.loganalyzer"
}