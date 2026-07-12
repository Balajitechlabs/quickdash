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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import com.balajitechlabs.quickdash.core.data.UserStore

data class Country(val code: String, val iso: String, val name: String, val flag: String)

@Composable
fun QuickChatScreen(
    userStore: UserStore,
    showSettings: Boolean,
    onToggleSettings: (Boolean) -> Unit,
    selectingCountry: Boolean,
    onToggleSelectingCountry: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Collect settings from DataStore
    val defaultCode by userStore.chatDefaultCode.collectAsState(initial = "91")
    val defaultIso by userStore.chatDefaultIso.collectAsState(initial = "IN")
    val historyList by userStore.chatHistory.collectAsState(initial = emptyList())
    val pauseHistory by userStore.chatPauseHistory.collectAsState(initial = false)

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
        if (cleanInput.startsWith("+")) {
            if (digits.length >= 3 && countryCodeToIso.containsKey(digits.substring(0, 3))) {
                return countryCodeToIso[digits.substring(0, 3)]!!
            }
            if (digits.length >= 2 && countryCodeToIso.containsKey(digits.substring(0, 2))) {
                return countryCodeToIso[digits.substring(0, 2)]!!
            }
            if (digits.length >= 1 && countryCodeToIso.containsKey(digits.substring(0, 1))) {
                return countryCodeToIso[digits.substring(0, 1)]!!
            }
        } else {
            // Check if digits start with a known country code
            if (digits.length > 10) {
                val potentialCodeLength = digits.length - 10
                val possibleCode = digits.substring(0, potentialCodeLength)
                if (countryCodeToIso.containsKey(possibleCode)) {
                    return countryCodeToIso[possibleCode]!!
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
                                            userStore.saveChatDefaultCountry(country.code, country.iso)
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
            Column(modifier = Modifier.fillMaxWidth()) {
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
                        Icon(painterResource(R.drawable.ic_keyboard_arrow_down), contentDescription = "Select")
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
                                painter = painterResource(R.drawable.ic_delete),
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
                            onCheckedChange = { scope.launch { userStore.saveChatPauseHistory(it) } }
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
                            scope.launch { userStore.clearChatHistory() }
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
        var messageText by remember { mutableStateOf("") }
        
        val templates = when (selectedTab) {
            "WhatsApp" -> listOf(
                "Hello! 👋",
                "Urgent! Please check. 🚨",
                "Busy now, call later. ⏳",
                "Let's meet up! ☕",
                "Can you share the files? 📁"
            )
            "Telegram" -> listOf(
                "Hello! 👋",
                "Hey! Let's chat on Telegram. ✈️",
                "Join community link: https://t.me/+7jh0CvLVDlFjNDU1 📢",
                "Busy now, call later. ⏳",
                "Can you check the updates? 🔄"
            )
            "Signal" -> listOf(
                "Hello! 👋",
                "Secure message on Signal. 🔒",
                "Urgent! Please check. 🚨",
                "Busy now, call later. ⏳"
            )
            else -> listOf(
                "Urgent SMS! Call me when free. 📱",
                "Busy now, call later. ⏳",
                "Where are you? 📍",
                "I'm on my way. 🚗",
                "Let's meet up! ☕"
            )
        }

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

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Select Target App",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
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
                    val color = when (tab) {
                        "WhatsApp" -> Color(0xFF25D366)
                        "Telegram" -> Color(0xFF0088CC)
                        "Signal" -> Color(0xFF3A76F0)
                        else -> Color(0xFFE91E63)
                    }
                    val iconRes = when (tab) {
                        "WhatsApp" -> R.drawable.ic_whatsapp
                        "Telegram" -> R.drawable.ic_share
                        "Signal" -> R.drawable.ic_quickdash_tile
                        else -> R.drawable.ic_phone
                    }
                    Card(
                        onClick = { selectedTab = tab },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(iconRes),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = tab,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
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
                shape = RoundedCornerShape(16.dp),
                leadingIcon = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(start = 12.dp, end = 4.dp)
                            .then(
                                if (!isUsername) {
                                    Modifier.clickable { onToggleSelectingCountry(true) }
                                } else {
                                    Modifier
                                }
                            )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = if (isUsername) "👤" else activeFlag, fontSize = 20.sp)
                            if (!isUsername) {
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    painter = painterResource(R.drawable.ic_keyboard_arrow_down),
                                    contentDescription = "Select country code",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                                    painter = painterResource(R.drawable.ic_close),
                                    contentDescription = "Clear"
                                )
                            }
                        }
                        IconButton(onClick = {
                            val barcodeScanner = com.google.mlkit.vision.codescanner.GmsBarcodeScanning.getClient(context)
                            barcodeScanner.startScan().addOnSuccessListener { barcode ->
                                val raw = barcode.rawValue ?: ""
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
                                    if (parsed == null) {
                                        parsed = clean
                                    }
                                }
                                
                                if (parsed != null) {
                                    phoneNumber = parsed
                                } else {
                                    android.widget.Toast.makeText(context, "Invalid WhatsApp QR code", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        }) {
                            Icon(Icons.Default.QrCodeScanner, "Scan QR")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Message Input
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                label = { Text("Message (Optional)") },
                placeholder = { Text("Type custom message here...") },
                shape = RoundedCornerShape(16.dp),
                singleLine = false,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Templates Chips
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(templates) { template ->
                    Card(
                        onClick = { messageText = template },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = template,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Button
            Button(
                onClick = {
                    if (isValid) {
                        scope.launch {
                            val flagToSave = if (isUsername) "👤" else activeFlag
                            userStore.saveChatNumberToHistory(
                                if (isUsername) phoneNumber.trim() else finalNumber,
                                flagToSave
                            )
                        }

                        try {
                            when (selectedTab) {
                                "WhatsApp" -> {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse("https://api.whatsapp.com/send?phone=$finalNumber&text=${Uri.encode(messageText)}")
                                    }
                                    context.startActivity(intent)
                                }
                                "Telegram" -> {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = if (isLink) {
                                            var url = phoneNumber.trim()
                                            if (url.startsWith("t.me")) {
                                                url = "https://$url"
                                            }
                                            Uri.parse(url)
                                        } else if (isUsername) {
                                            val user = phoneNumber.trim().removePrefix("@")
                                            if (user.startsWith("+")) {
                                                Uri.parse("https://t.me/$user")
                                            } else {
                                                Uri.parse("https://t.me/$user?text=${Uri.encode(messageText)}")
                                            }
                                        } else {
                                            Uri.parse("tg://msg?text=${Uri.encode(messageText)}&to=$finalNumber")
                                        }
                                    }
                                    context.startActivity(intent)
                                }
                                "Signal" -> {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse("https://signal.me/#p/$finalNumber")
                                    }
                                    context.startActivity(intent)
                                }
                                "SMS" -> {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("smsto:$finalNumber")
                                        putExtra("sms_body", messageText)
                                    }
                                    context.startActivity(intent)
                                }
                            }
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Could not open $selectedTab client", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        onDismiss()
                    }
                },
                enabled = isValid,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                val iconRes = when (selectedTab) {
                    "WhatsApp" -> R.drawable.ic_whatsapp
                    "Telegram" -> R.drawable.ic_share // Use share icon for Telegram
                    "Signal" -> R.drawable.ic_quickdash_tile // Fallback to tile
                    else -> R.drawable.ic_phone // SMS
                }
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Start $selectedTab Chat",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
