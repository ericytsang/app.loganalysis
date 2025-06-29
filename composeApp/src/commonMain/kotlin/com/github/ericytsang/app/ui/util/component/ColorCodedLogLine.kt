package com.github.ericytsang.app.ui.util.component

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.github.ericytsang.domain.objects.Theme
import kotlin.random.Random

@Composable
fun ColorCodedLogLine(

    /**
     * text to display for user to see
     */
    text:String,

    /**
     * text to display in a separate, zero-height, non-focusable text view to force the layout to at least be this width
     */
    invisibleText:String,
    defaultColor:Color,
    themeForColorCoding:Theme,
    delimiters:String,
    softWrap:Boolean,
    modifier:Modifier = Modifier,
)
{
    Box(
        modifier = modifier,
    )
    {
        if (!softWrap)
        {
            Text(
                modifier = Modifier.height(0.dp).focusable(false),
                text = invisibleText,
                style = TextStyle(fontFamily = FontFamily.Monospace),
            )
        }
        val logLineString = buildColorCodedLogLine(
            logLine = text,
            delimiters = delimiters,
            isDarkTheme = when (themeForColorCoding)
            {
                Theme.LIGHT -> false
                Theme.DARK -> true
            },
        )
        Text(
            text = logLineString,
            color = defaultColor,
            softWrap = softWrap,
            style = TextStyle(fontFamily = FontFamily.Monospace),
        )
    }
}


/**
 * Function to get a color appropriate for the theme for a word based on its hash code.
 * @param word The word to be colored
 * @param isDarkTheme Boolean indicating if the theme is dark
 * @return Color for the word
 */
private fun getColorForWord(
    word:String,
    isDarkTheme:Boolean,
):Color
{
    // generate a random color based on the hash code of the word
    val random = Random(word.hashCode())
    val red = random.nextFloat()*0.5f
    val green = random.nextFloat()*0.5f
    val blue = random.nextFloat()*0.5f

    // adjust brightness based on theme
    val colorBoost = if (isDarkTheme) 0.5f else 0f

    // create the Color object
    return Color(
        red = red+colorBoost,
        green = green+colorBoost,
        blue = blue+colorBoost,
    )
}

/**
 * Function to build a color-coded log line
 * @param logLine The log line to be color-coded
 * @param isDarkTheme Boolean indicating if the theme is dark
 * @return AnnotatedString with color-coded words
 */
private fun buildColorCodedLogLine(
    logLine:String,
    isDarkTheme:Boolean,
    delimiters:String,
):AnnotatedString
{
    val builder = AnnotatedString.Builder()

    var currentWord = StringBuilder()
    for (char in logLine)
    {
        if (char in delimiters)
        {
            // Add the current word with its color
            if (currentWord.isNotEmpty())
            {
                val word = currentWord.toString()
                builder.withStyle(SpanStyle(color = getColorForWord(word,isDarkTheme)))
                {
                    append(word)
                }
                currentWord.clear()
            }
            // Add the delimiter as is
            builder.append(char)
        }
        else
        {
            currentWord.append(char)
        }
    }
    // Add the last word if any
    if (currentWord.isNotEmpty())
    {
        val word = currentWord.toString()
        builder.withStyle(SpanStyle(color = getColorForWord(word,isDarkTheme)))
        {
            append(word)
        }
    }

    return builder.toAnnotatedString()
}