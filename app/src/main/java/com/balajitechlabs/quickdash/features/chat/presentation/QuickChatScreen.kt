/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/chat
 * File: QuickChatScreen.kt
 * Description: EssentialX-styled component for features/chat supporting high performance productivity tools.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.chat.presentation

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.balajitechlabs.quickdash.R
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

data class Country(val code: String, val iso: String, val name: String, val flag: String)

@Composable
fun QuickChatScreen(
    viewModel: QuickChatViewModel = hiltViewModel(),
    showSettings: Boolean,
    onToggleSettings: (Boolean) -> Unit,
    selectingCountry: Boolean,
    onToggleSelectingCountry: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Collect settings from ViewModel
    val defaultCode by viewModel.chatDefaultCode.collectAsStateWithLifecycle(initialValue = "91")
    val defaultIso by viewModel.chatDefaultIso.collectAsStateWithLifecycle(initialValue = "IN")
    val historyList by viewModel.chatHistory.collectAsStateWithLifecycle(initialValue = emptyList())
    val pauseHistory by viewModel.chatPauseHistory.collectAsStateWithLifecycle(initialValue = false)

    var phoneNumber by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var showClearHistoryConfirm by remember { mutableStateOf(false) }

    // Comprehensive list of countries sorted alphabetically by name
    val countries = remember {
        listOf(
            Country("93", "AF", "Afghanistan", "🇦🇫"),
            Country("355", "AL", "Albania", "🇦🇱"),
            Country("213", "DZ", "Algeria", "🇩🇿"),
            Country("376", "AD", "Andorra", "🇦🇩"),
            Country("244", "AO", "Angola", "🇦🇴"),
            Country("1", "AI", "Anguilla", "🇦🇮"),
            Country("1", "AG", "Antigua and Barbuda", "🇦🇬"),
            Country("54", "AR", "Argentina", "🇦🇷"),
            Country("374", "AM", "Armenia", "🇦🇲"),
            Country("297", "AW", "Aruba", "🇦🇼"),
            Country("61", "AU", "Australia", "🇦🇺"),
            Country("43", "AT", "Austria", "🇦🇹"),
            Country("994", "AZ", "Azerbaijan", "🇦🇿"),
            Country("1", "BS", "Bahamas", "🇧🇸"),
            Country("973", "BH", "Bahrain", "🇧🇭"),
            Country("880", "BD", "Bangladesh", "🇧🇩"),
            Country("1", "BB", "Barbados", "🇧🇧"),
            Country("375", "BY", "Belarus", "🇧🇾"),
            Country("32", "BE", "Belgium", "🇧🇪"),
            Country("501", "BZ", "Belize", "🇧🇿"),
            Country("229", "BJ", "Benin", "🇧🇯"),
            Country("1", "BM", "Bermuda", "🇧🇲"),
            Country("975", "BT", "Bhutan", "🇧🇹"),
            Country("591", "BO", "Bolivia", "🇧🇴"),
            Country("387", "BA", "Bosnia and Herzegovina", "🇧🇦"),
            Country("267", "BW", "Botswana", "🇧🇼"),
            Country("55", "BR", "Brazil", "🇧🇷"),
            Country("1", "VG", "British Virgin Islands", "🇻🇬"),
            Country("673", "BN", "Brunei", "🇧🇳"),
            Country("359", "BG", "Bulgaria", "🇧🇬"),
            Country("226", "BF", "Burkina Faso", "🇧🇫"),
            Country("257", "BI", "Burundi", "🇧🇮"),
            Country("855", "KH", "Cambodia", "🇰🇭"),
            Country("237", "CM", "Cameroon", "🇨🇲"),
            Country("1", "CA", "Canada", "🇨🇦"),
            Country("238", "CV", "Cape Verde", "🇨🇻"),
            Country("1", "KY", "Cayman Islands", "🇰🇾"),
            Country("236", "CF", "Central African Republic", "🇨🇫"),
            Country("235", "TD", "Chad", "🇹🇩"),
            Country("56", "CL", "Chile", "🇨🇱"),
            Country("86", "CN", "China", "🇨🇳"),
            Country("57", "CO", "Colombia", "🇨🇴"),
            Country("269", "KM", "Comoros", "🇰🇲"),
            Country("242", "CG", "Congo", "🇨🇬"),
            Country("243", "CD", "Congo (DRC)", "🇨🇩"),
            Country("682", "CK", "Cook Islands", "🇨🇰"),
            Country("506", "CR", "Costa Rica", "🇨🇷"),
            Country("385", "HR", "Croatia", "🇭🇷"),
            Country("53", "CU", "Cuba", "🇨🇺"),
            Country("357", "CY", "Cyprus", "🇨🇾"),
            Country("420", "CZ", "Czech Republic", "🇨🇿"),
            Country("45", "DK", "Denmark", "🇩🇰"),
            Country("253", "DJ", "Djibouti", "🇩🇯"),
            Country("1", "DM", "Dominica", "🇩🇲"),
            Country("1", "DO", "Dominican Republic", "🇩🇴"),
            Country("593", "EC", "Ecuador", "🇪🇨"),
            Country("20", "EG", "Egypt", "🇪🇬"),
            Country("503", "SV", "El Salvador", "🇸🇻"),
            Country("240", "GQ", "Equatorial Guinea", "🇬🇶"),
            Country("291", "ER", "Eritrea", "🇪🇷"),
            Country("372", "EE", "Estonia", "🇪🇪"),
            Country("268", "SZ", "Eswatini", "🇸🇿"),
            Country("251", "ET", "Ethiopia", "🇪🇹"),
            Country("298", "FO", "Faroe Islands", "🇫🇴"),
            Country("679", "FJ", "Fiji", "🇫🇯"),
            Country("358", "FI", "Finland", "🇫🇮"),
            Country("33", "FR", "France", "🇫🇷"),
            Country("594", "GF", "French Guiana", "🇬🇫"),
            Country("689", "PF", "French Polynesia", "🇵🇫"),
            Country("241", "GA", "Gabon", "🇬🇦"),
            Country("220", "GM", "Gambia", "🇬🇲"),
            Country("995", "GE", "Georgia", "🇬🇪"),
            Country("49", "DE", "Germany", "🇩🇪"),
            Country("233", "GH", "Ghana", "🇬🇭"),
            Country("350", "GI", "Gibraltar", "🇬🇮"),
            Country("30", "GR", "Greece", "🇬🇷"),
            Country("299", "GL", "Greenland", "🇬🇱"),
            Country("1", "GD", "Grenada", "🇬🇩"),
            Country("590", "GP", "Guadeloupe", "🇬🇵"),
            Country("1", "GU", "Guam", "🇬🇺"),
            Country("502", "GT", "Guatemala", "🇬🇹"),
            Country("224", "GN", "Guinea", "🇬🇳"),
            Country("245", "GW", "Guinea-Bissau", "🇬🇼"),
            Country("592", "GY", "Guyana", "🇬🇾"),
            Country("509", "HT", "Haiti", "🇭🇹"),
            Country("504", "HN", "Honduras", "🇭🇳"),
            Country("852", "HK", "Hong Kong", "🇭🇰"),
            Country("36", "HU", "Hungary", "🇭🇺"),
            Country("354", "IS", "Iceland", "🇮🇸"),
            Country("91", "IN", "India", "🇮🇳"),
            Country("62", "ID", "Indonesia", "🇮🇩"),
            Country("98", "IR", "Iran", "🇮🇷"),
            Country("964", "IQ", "Iraq", "🇮🇶"),
            Country("353", "IE", "Ireland", "🇮🇪"),
            Country("972", "IL", "Israel", "🇮🇱"),
            Country("39", "IT", "Italy", "🇮🇹"),
            Country("225", "CI", "Ivory Coast", "🇨🇮"),
            Country("1", "JM", "Jamaica", "🇯🇲"),
            Country("81", "JP", "Japan", "🇯🇵"),
            Country("962", "JO", "Jordan", "🇯🇴"),
            Country("7", "KZ", "Kazakhstan", "🇰🇿"),
            Country("254", "KE", "Kenya", "🇰🇪"),
            Country("686", "KI", "Kiribati", "🇰🇮"),
            Country("965", "KW", "Kuwait", "🇰🇼"),
            Country("996", "KG", "Kyrgyzstan", "🇰🇬"),
            Country("856", "LA", "Laos", "🇱🇦"),
            Country("371", "LV", "Latvia", "🇱🇻"),
            Country("961", "LB", "Lebanon", "🇱🇧"),
            Country("266", "LS", "Lesotho", "🇱🇸"),
            Country("231", "LR", "Liberia", "🇱🇷"),
            Country("218", "LY", "Libya", "🇱🇾"),
            Country("423", "LI", "Liechtenstein", "🇱🇮"),
            Country("370", "LT", "Lithuania", "🇱🇹"),
            Country("352", "LU", "Luxembourg", "🇱🇺"),
            Country("853", "MO", "Macau", "🇲🇴"),
            Country("389", "MK", "North Macedonia", "🇲🇰"),
            Country("261", "MG", "Madagascar", "🇲🇬"),
            Country("265", "MW", "Malawi", "🇲🇼"),
            Country("60", "MY", "Malaysia", "🇲🇾"),
            Country("960", "MV", "Maldives", "🇲🇻"),
            Country("223", "ML", "Mali", "🇲🇱"),
            Country("356", "MT", "Malta", "🇲🇹"),
            Country("692", "MH", "Marshall Islands", "🇲🇭"),
            Country("596", "MQ", "Martinique", "🇲🇶"),
            Country("222", "MR", "Mauritania", "🇲🇷"),
            Country("230", "MU", "Mauritius", "🇲🇺"),
            Country("262", "YT", "Mayotte", "🇾🇹"),
            Country("52", "MX", "Mexico", "🇲🇽"),
            Country("691", "FM", "Micronesia", "🇫🇲"),
            Country("373", "MD", "Moldova", "🇲🇩"),
            Country("377", "MC", "Monaco", "🇲🇨"),
            Country("976", "MN", "Mongolia", "🇲🇳"),
            Country("382", "ME", "Montenegro", "🇲🇪"),
            Country("1", "MS", "Montserrat", "🇲🇸"),
            Country("212", "MA", "Morocco", "🇲🇦"),
            Country("258", "MZ", "Mozambique", "🇲🇿"),
            Country("95", "MM", "Myanmar", "🇲🇲"),
            Country("264", "NA", "Namibia", "🇳🇦"),
            Country("674", "NR", "Nauru", "🇳🇷"),
            Country("977", "NP", "Nepal", "🇳🇵"),
            Country("31", "NL", "Netherlands", "🇳🇱"),
            Country("687", "NC", "New Caledonia", "🇳🇨"),
            Country("64", "NZ", "New Zealand", "🇳🇿"),
            Country("505", "NI", "Nicaragua", "🇳🇮"),
            Country("227", "NE", "Niger", "🇳🇪"),
            Country("234", "NG", "Nigeria", "🇳🇬"),
            Country("683", "NU", "Niue", "🇳🇺"),
            Country("850", "KP", "North Korea", "🇰🇵"),
            Country("1", "MP", "Northern Mariana Islands", "🇲🇵"),
            android.os.Build.VERSION_CODES.N.let { Country("47", "NO", "Norway", "🇳🇴") },
            Country("968", "OM", "Oman", "🇴🇲"),
            Country("92", "PK", "Pakistan", "🇵🇰"),
            Country("680", "PW", "Palau", "🇵🇼"),
            Country("970", "PS", "Palestine", "🇵🇸"),
            Country("507", "PA", "Panama", "🇵🇦"),
            Country("675", "PG", "Papua New Guinea", "🇵🇬"),
            Country("595", "PY", "Paraguay", "🇵🇾"),
            Country("51", "PE", "Peru", "🇵🇪"),
            Country("63", "PH", "Philippines", "🇵🇭"),
            Country("48", "PL", "Poland", "🇵🇱"),
            Country("351", "PT", "Portugal", "🇵🇹"),
            Country("1", "PR", "Puerto Rico", "🇵🇷"),
            Country("974", "QA", "Qatar", "🇶🇦"),
            Country("262", "RE", "Réunion", "🇷🇪"),
            Country("40", "RO", "Romania", "🇷🇴"),
            Country("7", "RU", "Russia", "🇷🇺"),
            Country("250", "RW", "Rwanda", "🇷🇼"),
            Country("290", "SH", "Saint Helena", "🇸🇭"),
            Country("1", "KN", "Saint Kitts and Nevis", "🇰🇳"),
            Country("1", "LC", "Saint Lucia", "🇱🇨"),
            Country("508", "PM", "Saint Pierre and Miquelon", "🇵🇲"),
            Country("1", "VC", "Saint Vincent and the Grenadines", "🇻🇨"),
            Country("685", "WS", "Samoa", "🇼🇸"),
            Country("378", "SM", "San Marino", "🇸🇲"),
            Country("239", "ST", "São Tomé and Príncipe", "🇸🇹"),
            Country("966", "SA", "Saudi Arabia", "🇸🇦"),
            Country("221", "SN", "Senegal", "🇸🇳"),
            Country("381", "RS", "Serbia", "🇷🇸"),
            Country("248", "SC", "Seychelles", "🇸🇨"),
            Country("232", "SL", "Sierra Leone", "🇸🇱"),
            Country("65", "SG", "Singapore", "🇸🇬"),
            Country("421", "SK", "Slovakia", "🇸🇰"),
            Country("386", "SI", "Slovenia", "🇸🇮"),
            Country("677", "SB", "Solomon Islands", "🇸🇧"),
            Country("252", "SO", "Somalia", "🇸🇴"),
            Country("27", "ZA", "South Africa", "🇿🇦"),
            Country("82", "KR", "South Korea", "🇰🇷"),
            Country("211", "SS", "South Sudan", "🇸🇸"),
            Country("34", "ES", "Spain", "🇪🇸"),
            Country("94", "LK", "Sri Lanka", "🇱🇰"),
            Country("249", "SD", "Sudan", "🇸🇩"),
            Country("597", "SR", "Suriname", "🇸🇷"),
            Country("46", "SE", "Sweden", "🇸🇪"),
            Country("41", "CH", "Switzerland", "🇨🇭"),
            Country("963", "SY", "Syria", "🇸🇾"),
            Country("886", "TW", "Taiwan", "🇹🇼"),
            Country("992", "TJ", "Tajikistan", "🇹🇯"),
            Country("255", "TZ", "Tanzania", "🇹🇿"),
            Country("66", "TH", "Thailand", "🇹🇭"),
            Country("228", "TG", "Togo", "🇹🇬"),
            Country("690", "TK", "Tokelau", "🇹🇰"),
            Country("676", "TO", "Tonga", "🇹🇴"),
            Country("1", "TT", "Trinidad and Tobago", "🇹🇹"),
            Country("216", "TN", "Tunisia", "🇹🇳"),
            Country("90", "TR", "Turkey", "🇹🇷"),
            Country("993", "TM", "Turkmenistan", "🇹🇲"),
            Country("1", "TC", "Turks and Caicos Islands", "🇹🇨"),
            Country("688", "TV", "Tuvalu", "🇹🇻"),
            Country("1", "VI", "U.S. Virgin Islands", "🇻🇮"),
            Country("256", "UG", "Uganda", "🇺🇬"),
            Country("380", "UA", "Ukraine", "🇺🇦"),
            Country("971", "AE", "United Arab Emirates", "🇦🇪"),
            Country("44", "GB", "United Kingdom", "🇬🇧"),
            Country("1", "US", "United States", "🇺🇸"),
            Country("598", "UY", "Uruguay", "🇺🇾"),
            Country("998", "UZ", "Uzbekistan", "🇺🇿"),
            Country("678", "VU", "Vanuatu", "🇻🇺"),
            Country("379", "VA", "Vatican City", "🇻🇦"),
            Country("58", "VE", "Venezuela", "🇻🇪"),
            Country("84", "VN", "Vietnam", "🇻🇳"),
            Country("681", "WF", "Wallis and Futuna", "🇼🇫"),
            Country("967", "YE", "Yemen", "🇾🇪"),
            Country("260", "ZM", "Zambia", "🇿🇲"),
            Country("263", "ZW", "Zimbabwe", "🇿🇼")
        ).sortedBy { it.name }
    }

    // Parse country code map for dynamic detection of input prefixes
    val countryCodeToIso = remember {
        countries.associate { it.code to it.iso }
    }

    // Helper to generate flag emoji from ISO
    fun getFlagEmoji(countryIso: String): String {
        if (countryIso.length != 2) return "🌐"
        try {
            val firstChar = Character.codePointAt(countryIso, 0) - 0x41 + 0x1F1E6
            val secondChar = Character.codePointAt(countryIso, 1) - 0x41 + 0x1F1E6
            return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
        } catch (e: Exception) {
            return "🌐"
        }
    }

    // Detect country ISO based on number prefix
    fun detectCountryIso(input: String): String {
        val cleanInput = input.trim()
        val digits = cleanInput.replace(Regex("[^0-9]"), "")
        if (digits.isNotEmpty()) {
            for (len in minOf(3, digits.length) downTo 1) {
                val candidate = digits.substring(0, len)
                if (countryCodeToIso.containsKey(candidate)) {
                    return countryCodeToIso[candidate]!!
                }
            }
        }
        return defaultIso
    }

    if (showSettings) {
        if (selectingCountry) {
            // --- Default Country Selection Screen ---
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search country...") },
                    leadingIcon = { Icon(painterResource(R.drawable.ic_search), contentDescription = "Search") },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                val filteredCountries = countries.filter {
                    it.name.contains(searchQuery, ignoreCase = true) || it.code.contains(searchQuery)
                }

                Box(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(filteredCountries) { country ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch {
                                            viewModel.saveChatDefaultCountry(country.code, country.iso)
                                            onToggleSelectingCountry(false)
                                            searchQuery = ""
                                        }
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = country.flag, fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = country.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "+${country.code}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))
                        }
                    }
                }
            }
        } else {
            // --- Quick Chat Settings Main View ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 96.dp)
            ) {
                // Country Selection Card
                Text(
                    text = "Default Country",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                val activeCountryName = countries.find { it.code == defaultCode && it.iso == defaultIso }?.name ?: "Default"
                val activeFlag = getFlagEmoji(defaultIso)
                Card(
                    onClick = { onToggleSelectingCountry(true) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = activeFlag, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activeCountryName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Dial Code: +$defaultCode",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Select",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // History Header with Pause Toggle and Delete Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    if (historyList.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearHistoryConfirm = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Clear History",
                                tint = Color.Red,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Pause History Toggle Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pause History",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Temporarily stop saving numbers to history.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = pauseHistory,
                            onCheckedChange = { viewModel.saveChatPauseHistory(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                checkedBorderColor = Color.Transparent,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFF2A2B30),
                                uncheckedBorderColor = Color(0xFF44474F)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // History Items List
                if (historyList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No history items saved.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    Box(modifier = Modifier.weight(1f)) {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(historyList) { entry ->
                                val parts = entry.split(":")
                                val number = parts.getOrNull(0) ?: ""
                                val flag = parts.getOrNull(1) ?: "🌐"
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            phoneNumber = number
                                            onToggleSettings(false)
                                        }
                                        .padding(vertical = 12.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = flag, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = number,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        painter = painterResource(R.drawable.ic_whatsapp),
                                        contentDescription = "Message",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))
                            }
                        }
                    }
                }
            }
        }

        // Clear History Confirmation dialog
        if (showClearHistoryConfirm) {
            AlertDialog(
                onDismissRequest = { showClearHistoryConfirm = false },
                title = { Text("Clear Chat History?") },
                text = { Text("Are you sure you want to permanently clear all saved chat numbers from your history?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.clearChatHistory()
                            showClearHistoryConfirm = false
                        }
                    ) {
                        Text("Clear", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearHistoryConfirm = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    } else {
        // --- Redesigned Smart Chat Input Screen ---
        var selectedTab by remember { mutableStateOf("WhatsApp") } // "WhatsApp", "Telegram", "Signal", "SMS"
        var telegramMode by remember { mutableStateOf("Username") } // "Username" or "Phone"

        val detectedIso = detectCountryIso(phoneNumber)
        val activeFlag = getFlagEmoji(detectedIso)

        val digitsOnly = phoneNumber.replace(Regex("[^0-9a-zA-Z]"), "")
        val isLink = phoneNumber.trim().startsWith("http") || phoneNumber.trim().startsWith("t.me") || phoneNumber.trim().contains("/")
        val isUsername = selectedTab == "Telegram" && (telegramMode == "Username" || isLink)
        val isValid = if (isLink) {
            phoneNumber.trim().length >= 5
        } else if (isUsername) {
            phoneNumber.trim().length >= 3
        } else {
            digitsOnly.length >= 7
        }

        val finalNumber = when {
            phoneNumber.trim().startsWith("+") -> {
                "+$digitsOnly"
            }
            digitsOnly.startsWith(defaultCode) && digitsOnly.length > 10 -> {
                "+$digitsOnly"
            }
            else -> {
                "+$defaultCode$digitsOnly"
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Select Target App",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Horizontal Tab Chips with custom icons in frames
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val tabs = listOf("WhatsApp", "Telegram", "Signal", "SMS")
                tabs.forEach { tab ->
                    val isSelected = selectedTab == tab
                    val iconRes = when (tab) {
                        "WhatsApp" -> R.drawable.ic_whatsapp
                        "Telegram" -> R.drawable.ic_telegram
                        "Signal" -> R.drawable.ic_signal
                        else -> R.drawable.ic_sms
                    }
                    Card(
                        onClick = { selectedTab = tab },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF38393F) else Color(0xFF1E2024)
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) Color.White else Color(0xFF44474F).copy(alpha = 0.4f)
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(iconRes),
                                contentDescription = tab,
                                modifier = Modifier.size(24.dp),
                                tint = if (isSelected) Color.White else Color(0xFFC5C6D0)
                            )
                        }
                    }
                }
            }

            // Telegram mode selection
            if (selectedTab == "Telegram") {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Username" to "Open with Wizard ID", "Phone" to "Open with Phone Number").forEach { (mode, label) ->
                        val isSelected = telegramMode == mode
                        Card(
                            onClick = { 
                                telegramMode = mode 
                                phoneNumber = ""
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF0088CC).copy(alpha = 0.15f)
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFF0088CC) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                            )
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(0xFF0088CC) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Phone / Username Input
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text(if (isUsername) "Telegram Username / Link" else "Phone Number") },
                placeholder = {
                    if (isUsername) {
                        Text("@username or t.me/joinlink")
                    } else {
                        Text("+$defaultCode 98765-43210")
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (isUsername) KeyboardType.Text else KeyboardType.Phone
                ),
                leadingIcon = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(start = 12.dp, end = 6.dp)
                            .then(
                                if (!isUsername) {
                                    Modifier.clickable { onToggleSelectingCountry(true) }
                                } else {
                                    Modifier
                                }
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isUsername) {
                                Text(text = "👤", fontSize = 18.sp)
                            } else {
                                Text(text = activeFlag, fontSize = 18.sp)
                                Text(
                                    text = "+$defaultCode",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Icon(
                                    imageVector = Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = "Select country code",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFFC5C6D0)
                                )
                            }
                        }
                    }
                },
                trailingIcon = {
                    Row {
                        if (phoneNumber.isNotEmpty()) {
                            IconButton(onClick = { phoneNumber = "" }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Clear",
                                    tint = Color(0xFFC5C6D0)
                                )
                            }
                        }
                        IconButton(onClick = {
                            com.balajitechlabs.quickdash.features.qr.utils.QrScannerHelper.startScan(
                                context = context,
                                onResult = { raw ->
                                    val clean = raw.trim()
                                    val lower = clean.lowercase()
                                    val isWhatsAppPayload = lower.contains("wa.me/") ||
                                            lower.contains("api.whatsapp.com/send") ||
                                            lower.contains("whatsapp://send") ||
                                            lower.contains("web.whatsapp.com/send") ||
                                            lower.contains("chat.whatsapp.com/") ||
                                            lower.contains("whatsapp.com/channel/")
                                    
                                    var parsed: String? = null
                                    if (isWhatsAppPayload) {
                                        parsed = Regex("[?&]phone=([+0-9]+)", RegexOption.IGNORE_CASE)
                                            .find(clean)?.groupValues?.get(1)
                                        if (parsed == null) {
                                            parsed = Regex("wa\\.me/([+0-9]+)", RegexOption.IGNORE_CASE)
                                                .find(clean)?.groupValues?.get(1)
                                        }
                                    } else {
                                        if (clean.matches(Regex("^[+0-9]+$"))) {
                                            parsed = clean
                                        }
                                    }

                                    if (parsed != null) {
                                        phoneNumber = parsed.removePrefix("+$defaultCode").removePrefix("+")
                                    } else {
                                        android.widget.Toast.makeText(context, "Invalid WhatsApp QR code", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.QrCodeScanner,
                                contentDescription = "Scan QR",
                                tint = Color(0xFFC5C6D0)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF44474F),
                    unfocusedBorderColor = Color(0xFF44474F).copy(alpha = 0.6f),
                    focusedContainerColor = Color(0xFF38393F),
                    unfocusedContainerColor = Color(0xFF38393F),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Action Button
            val cleanNumericDigits = when {
                phoneNumber.trim().startsWith("+") -> phoneNumber.trim().removePrefix("+").replace(Regex("[^0-9]"), "")
                phoneNumber.replace(Regex("[^0-9]"), "").startsWith(defaultCode) && phoneNumber.replace(Regex("[^0-9]"), "").length > 10 -> phoneNumber.replace(Regex("[^0-9]"), "")
                else -> "$defaultCode${phoneNumber.replace(Regex("[^0-9]"), "")}"
            }

            Button(
                onClick = {
                    if (isValid) {
                        scope.launch {
                            val flagToSave = if (isUsername) "👤" else activeFlag
                            viewModel.saveChatNumberToHistory(
                                if (isUsername) phoneNumber.trim() else "+$cleanNumericDigits",
                                flagToSave
                            )
                        }

                        try {
                            when (selectedTab) {
                                "WhatsApp" -> {
                                    val waUri = Uri.parse("https://wa.me/$cleanNumericDigits")
                                    val waIntent = Intent(Intent.ACTION_VIEW, waUri).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(waIntent)
                                }
                                "Telegram" -> {
                                    val tgUri = if (isLink) {
                                        var url = phoneNumber.trim()
                                        if (!url.startsWith("http")) url = "https://$url"
                                        Uri.parse(url)
                                    } else if (isUsername) {
                                        val user = phoneNumber.trim().removePrefix("@")
                                        Uri.parse("https://t.me/$user")
                                    } else {
                                        Uri.parse("https://t.me/+$cleanNumericDigits")
                                    }
                                    val tgIntent = Intent(Intent.ACTION_VIEW, tgUri).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(tgIntent)
                                }
                                "Signal" -> {
                                    val signalIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://signal.me/#p/+$cleanNumericDigits")).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(signalIntent)
                                }
                                "SMS" -> {
                                    val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:+$cleanNumericDigits")).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(smsIntent)
                                }
                            }
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Could not open $selectedTab client", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        onDismiss()
                    }
                },
                enabled = isValid,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF2A2B30),
                    disabledContentColor = Color(0xFF8E9099)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val iconRes = when (selectedTab) {
                        "WhatsApp" -> R.drawable.ic_whatsapp
                        "Telegram" -> R.drawable.ic_telegram
                        "Signal" -> R.drawable.ic_signal
                        else -> R.drawable.ic_sms
                    }
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Open Chat in $selectedTab",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}
