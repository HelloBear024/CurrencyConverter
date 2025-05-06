package com.currecy.mycurrencyconverter.utills.ui_utills

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

@OptIn(ExperimentalHazeMaterialsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GlassmorphicContainerTextInputs(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .hazeEffect(
                state = hazeState,
                style = HazeMaterials.ultraThin(Color.White),
            ){
                blurRadius = 60.dp
            }
    ) {
        content()
    }
}

@Composable
fun EditNumberField(
    label: String,
    keyboardOptions: KeyboardOptions,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    var internalValue by remember { mutableStateOf(value) }


    LaunchedEffect(value) {
        internalValue = value
    }


    OutlinedTextField(
        value = internalValue,
        onValueChange = { newValue ->
            internalValue = newValue
            onValueChange(newValue)
        },
        label = null,
        shape = RoundedCornerShape(15.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,

            focusedTextColor = MaterialTheme.colorScheme.tertiary,
            unfocusedTextColor = MaterialTheme.colorScheme.tertiary,

            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,

            focusedLabelColor = Color.Transparent,
            unfocusedLabelColor = Color.Transparent,

            ),
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = keyboardOptions
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuSpinner(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent,
    textColor: Color = Color.White,
    trailingColor: Color = Color(0xFFFD5B66),
    borderColor: Color = Color.Transparent,
    optionsList: List<Pair<String, String>>,
    selectedCurrency: String,
    onCurrencySelected: (String) -> Unit) {

    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember {
        mutableStateOf(
            optionsList.find { it.second == selectedCurrency }?.second ?: optionsList[0].second
        )
    }

    LaunchedEffect(selectedCurrency) {
        selectedOptionText = selectedCurrency
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedOptionText,
            onValueChange = {},
            label = null,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(
                focusedContainerColor = backgroundColor,
                unfocusedContainerColor = backgroundColor,

                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                disabledTextColor = textColor,

                // trailing Icon
                unfocusedTrailingIconColor = trailingColor,
                focusedTrailingIconColor = trailingColor,
                disabledTrailingIconColor = trailingColor,

                // border
                unfocusedIndicatorColor = borderColor,
                focusedIndicatorColor = borderColor,
                disabledIndicatorColor = borderColor,


            ),
            shape = RoundedCornerShape(15.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            readOnly = true
        )

        if (expanded) {
            DropdownMenu(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.onTertiary)
                    .exposedDropdownSize(false),
                properties = PopupProperties(focusable = false),
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                optionsList.forEach { currencyPair ->
                    DropdownMenuItem(
                        text = { Text(currencyPair.first + " (" + currencyPair.second.uppercase() + " )") },
                        onClick = {
                            selectedOptionText =
                                currencyPair.second
                            expanded = false
                            onCurrencySelected(currencyPair.second)
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }
}