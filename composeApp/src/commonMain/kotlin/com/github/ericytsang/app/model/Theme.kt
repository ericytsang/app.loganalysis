package com.github.ericytsang.app.model

/**
 * supported themes.
 */
enum class Theme
{
    LIGHT,
    DARK,
}

fun Theme.getNextTheme() = when (this)
{
    Theme.LIGHT -> Theme.DARK
    Theme.DARK -> Theme.LIGHT
}
