package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    onLoginSuccess: (String) -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf(false) }
    var otpError by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (isDarkMode) {
                        listOf(Color(0xFF1C1B1F), Color(0xFF2B2930))
                    } else {
                        listOf(Color(0xFFE6F3F1), Color(0xFFF4FAF9))
                    }
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Emblem
            Box(
                modifier = Modifier
                    .size(85.dp)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Vaccines,
                    contentDescription = "اللوجو",
                    tint = primaryColor,
                    modifier = Modifier.size(45.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Text Header
            Text(
                text = "الـرّاجـيـتـة",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor,
                textAlign = TextAlign.Center
            )

            Text(
                text = "مساعدك الطبي العراقي الذكي 🇮🇶",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (val currentAuth = authState) {
                        is AuthState.Unauthenticated -> {
                            Text(
                                text = "تسجيل الدخول عيني",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Right
                            )

                            Text(
                                text = "أهلاً بيك! سجّل رقمك علمود نحفظلك تاريخ الراجيتات والتحاليل مالتك بسلاسة وسرعة.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp, bottom = 20.dp),
                                textAlign = TextAlign.Right
                            )

                            // Phone input field
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                // Prefix Iraqi code
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
                                        .padding(horizontal = 12.dp, vertical = 15.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "🇮🇶", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "+964",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                OutlinedTextField(
                                    value = phoneNumber,
                                    onValueChange = {
                                        if (it.all { char -> char.isDigit() } && it.length <= 11) {
                                            phoneNumber = it
                                            phoneError = false
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("phone_input"),
                                    label = { Text("رقم الهاتف") },
                                    isError = phoneError,
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = primaryColor,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                                        errorBorderColor = MaterialTheme.colorScheme.error
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            if (phoneError) {
                                Text(
                                    text = "يرجى إدخال رقم هاتف عراقي صحيح عيني (مثلا: 7756786034)",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 11.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    textAlign = TextAlign.Right
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    if (phoneNumber.length in 9..11) {
                                        viewModel.sendOtp(phoneNumber) {
                                            // Trigger simulated popup or OTP stage
                                        }
                                    } else {
                                        phoneError = true
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("send_otp_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                enabled = !isScanning
                            ) {
                                Text(
                                    text = if (isScanning) "جاري إرسال الرمز..." else "إرسال كود التحقق",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        is AuthState.OtpSent -> {
                            Text(
                                text = "أدخل رمز التأكيد",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Right
                            )

                            Text(
                                text = "دزّينا كود تأكيد على رقم الهاتف ${currentAuth.phoneNumber}. دخل الرمز للاستمرار عيني.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp, bottom = 12.dp),
                                textAlign = TextAlign.Right
                            )

                            Text(
                                text = "💡 للتجربة السريعة: اكتب أي 6 أرقام (مثلاً: 123456)",
                                fontSize = 12.sp,
                                color = secondaryColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 20.dp),
                                textAlign = TextAlign.Right
                            )

                            OutlinedTextField(
                                value = otpCode,
                                onValueChange = {
                                    if (it.all { char -> char.isDigit() } && it.length <= 6) {
                                        otpCode = it
                                        otpError = false
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("otp_input"),
                                label = { Text("رمز التحقق (OTP)") },
                                isError = otpError,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = "قفل")
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                                    errorBorderColor = MaterialTheme.colorScheme.error
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            if (otpError) {
                                Text(
                                    text = "الرمز مو صحيح عيني، اكتب الرمز المكون من 6 أرقام للتجربة.",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 11.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    textAlign = TextAlign.Right
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    if (otpCode.length >= 4) {
                                        viewModel.verifyOtp(
                                            phone = currentAuth.phoneNumber,
                                            otp = otpCode,
                                            onVerified = {
                                                onLoginSuccess(currentAuth.phoneNumber)
                                            },
                                            onError = {
                                                otpError = true
                                            }
                                        )
                                    } else {
                                        otpError = true
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("verify_otp_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                enabled = !isScanning
                            ) {
                                Text(
                                    text = if (isScanning) "جاري التحقق..." else "تأكيد تسجيل الدخول",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            TextButton(
                                onClick = { viewModel.logout() }
                            ) {
                                Text(
                                    text = "تعديل رقم الهاتف",
                                    color = primaryColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        else -> {}
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Footer Developer Rights
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "حقوق البرمجة والتطوير محفوطة بأسم",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
                )
                Text(
                    text = "أمين محمد حسين",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor.copy(alpha = 0.85f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
