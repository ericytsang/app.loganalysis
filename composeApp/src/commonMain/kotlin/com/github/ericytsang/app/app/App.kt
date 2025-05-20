package com.github.ericytsang.app.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Colors
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import com.github.ericytsang.app.model.Dimens
import com.github.ericytsang.app.ui.modal.openWorkingFileSetEditorInNewWindowBlocking
import com.github.ericytsang.app.util.animatedThemeColors
import com.github.ericytsang.app.util.fillMaxBackground
import com.github.ericytsang.domain.objects.Theme
import com.github.ericytsang.domain.objects.WorkingFileSetEmpty
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.awt.Window
import java.io.File
import kotlin.random.Random

@Composable
@Preview
fun App(
    window:Window,
    viewModelFactory:()->AppViewModel = { AppViewModel.create() },
    logViewerViewModelFactory:()->LogViewerViewModel = { LogViewerViewModel.createDefault() },
    workingFileSetEditorViewModelFactory:()->WorkingFileSetEditorViewModel = { WorkingFileSetEditorViewModel.createDefault() },
)
{
    val viewModel = remember { viewModelFactory() }
    val logViewerViewModel = remember { logViewerViewModelFactory() }
    val workingFileSetEditorViewModel = remember { workingFileSetEditorViewModelFactory() }

    val theme by viewModel.theme.collectAsState(Theme.DARK)
    val animatedThemeColors by animatedThemeColors(theme)

    val logcatFilterString by logViewerViewModel.logcatFilterString.collectAsState("")

    val workingFileSet by workingFileSetEditorViewModel.workingFileSet.collectAsState(WorkingFileSetEmpty)
    logViewerViewModel.setConcatenatedFiles(workingFileSet.files.map { File(it.filePath) })

    fillMaxBackground(colors = animatedThemeColors)
    {

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        )
        {
            Row(
                modifier = Modifier.fillMaxWidth().padding(Dimens.mttPadding),
                verticalAlignment = Alignment.CenterVertically,
            )
            {
                Button(
                    modifier = Modifier.padding(end = Dimens.mttPadding),
                    onClick = { viewModel.switchTheme() },
                    content = { Text("Toggle theme") },
                )

                Button(
                    modifier = Modifier.padding(end = Dimens.mttPadding),
                    onClick =
                        {
                            openWorkingFileSetEditorInNewWindowBlocking(
                                owner = window,
                                viewModel = workingFileSetEditorViewModel,
                            )
                        },
                    content =
                        {
                            val workingFileSetFiles = workingFileSet.files
                            if (workingFileSetFiles.isEmpty())
                            {
                                Text("Add files")
                            }
                            else
                            {
                                Text("Edit files (${workingFileSetFiles.size})")
                            }
                        },
                )

                TextField(
                    value = logcatFilterString,
                    onValueChange = { newValue -> logViewerViewModel.setLogcatFilterString(newValue) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = animatedThemeColors.onBackground,
                    ),
                )
            }

            val logLines by logViewerViewModel.getLogLinesFlow().collectAsState(emptyList())

            Column(
                modifier = Modifier.fillMaxSize().padding(Dimens.mttPadding),
            )
            {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                )
                {
                    items(count = logLines.size)
                    { index ->
                        ColorCodedLogLine(
                            text = logLines[index],
                            modifier = Modifier.fillMaxWidth(),
                            themeForColorCoding = theme,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ColorCodedLogLine(
    text:String,
    themeForColorCoding:Theme,
    modifier: Modifier = Modifier,
)
{
    val logLineString = buildColorCodedLogLine(
        logLine = text,
        isDarkTheme = when (themeForColorCoding)
        {
            Theme.LIGHT -> false
            Theme.DARK -> true
        },
    )
    Text(
        text = logLineString,
        modifier = modifier,
    )
}


/**
 * Function to get a color appropriate for the theme for a word based on its hash code.
 * @param word The word to be colored
 * @param isDarkTheme Boolean indicating if the theme is dark
 * @return Color for the word
 */
fun getColorForWord(word:String,isDarkTheme:Boolean):Color
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
fun buildColorCodedLogLine(
    logLine:String,
    isDarkTheme:Boolean,
):AnnotatedString
{
    val delimiters = ":,\"<>(){}[]. ".toSet()
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


