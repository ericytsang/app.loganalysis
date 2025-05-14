package org.example.project

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
