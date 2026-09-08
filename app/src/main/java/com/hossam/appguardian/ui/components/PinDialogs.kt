package com.hossam.appguardian.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.hossam.appguardian.R
import com.hossam.appguardian.utils.Constants

/** Verifying an already-set PIN (e.g. before disabling blocking, or removing a blocked app). */
@Composable
fun PinPromptDialog(
    title: String,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= Constants.PIN_MAX_LENGTH) pin = it.filter(Char::isDigit) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                isError = errorMessage != null,
                supportingText = { if (errorMessage != null) Text(errorMessage) }
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(pin) }, enabled = pin.length >= Constants.PIN_MIN_LENGTH) {
                Text(stringResource(R.string.action_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}

/** Setting a brand-new PIN, or replacing one after the current PIN was already verified. */
@Composable
fun SetPinDialog(
    onDismiss: () -> Unit,
    onComplete: (String) -> Unit
) {
    var step by remember { mutableStateOf(1) }
    var firstEntry by remember { mutableStateOf("") }
    var currentInput by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    // Read the string resource here, in composable context, so it can be used safely
    // inside the confirmButton's plain (non-composable) onClick lambda below.
    val mismatchMessage = stringResource(R.string.error_pin_mismatch)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (step == 1) R.string.pin_set_title else R.string.pin_confirm_prompt)) },
        text = {
            OutlinedTextField(
                value = currentInput,
                onValueChange = {
                    if (it.length <= Constants.PIN_MAX_LENGTH) currentInput = it.filter(Char::isDigit)
                    error = null
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                label = {
                    Text(stringResource(if (step == 1) R.string.pin_set_new_prompt else R.string.pin_confirm_prompt))
                },
                isError = error != null,
                supportingText = { if (error != null) Text(error!!) }
            )
        },
        confirmButton = {
            TextButton(
                enabled = currentInput.length >= Constants.PIN_MIN_LENGTH,
                onClick = {
                    if (step == 1) {
                        firstEntry = currentInput
                        currentInput = ""
                        step = 2
                    } else if (currentInput == firstEntry) {
                        onComplete(currentInput)
                    } else {
                        error = mismatchMessage
                        currentInput = ""
                    }
                }
            ) {
                Text(stringResource(if (step == 1) R.string.action_confirm else R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}
