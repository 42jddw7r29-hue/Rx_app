package com.example.ui

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.api.GeminiService
import com.example.database.PrescriptionEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    userName: String
) {
    val context = LocalContext.current
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val selectedBitmap by viewModel.selectedBitmap.collectAsState()
    val prescriptionsHistory by viewModel.prescriptionsHistory.collectAsState()
    val activeScanResult by viewModel.activeScanResult.collectAsState()
    val focusedPrescription by viewModel.focusedPrescription.collectAsState()

    var manualTitle by remember { mutableStateOf("") }
    var scanModeIsTest by remember { mutableStateOf(false) } // false = RX, true = Test
    var rotatingMessageIndex by remember { mutableStateOf(0) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    // Iraqi translation messages rotated during loading state
    val iraqiLoadingMessages = listOf(
        "ثواني عيني، جاي نقرأ خط الدكتور ونفهمه مالتك...",
        "جاي ندقق بأسماء العلاج عيني علمود سلامتك...",
        "دا نبحث بالأسماء العلمية والشركات المصنعة بدقة...",
        "لحظات وتجهز الراجيتة واضحة ومبسطة بالعامية مالتنا..."
    )

    LaunchedEffect(isScanning) {
        if (isScanning) {
            while (true) {
                kotlinx.coroutines.delay(2500)
                rotatingMessageIndex = (rotatingMessageIndex + 1) % iraqiLoadingMessages.size
            }
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            viewModel.selectImage(bitmap)
        } else {
            Toast.makeText(context, "تم إلغاء التصوير عيني", Toast.LENGTH_SHORT).show()
        }
    }

    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val bitmap = uri.toBitmap(context)
            if (bitmap != null) {
                viewModel.selectImage(bitmap)
            } else {
                Toast.makeText(context, "فشل تحميل الصورة عيني", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تطبيق الراجيتة 🩺",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.toggleTheme() }) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "تغيير الوضع",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "خروج",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                // Welcome card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = primaryColor.copy(alpha = 0.08f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "أهلاً بك عيني: $userName",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                        Text(
                            text = "سهّلنا عليك قراءة وتفسير أي راجيتة أو تحليل مخبري بالعامية العراقية وبأمان كامل وعالي الدقة.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            textAlign = TextAlign.Right,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Image Picker Section / Active Scan Selector
            item {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (selectedBitmap == null) {
                            // Blank upload state
                            Text(
                                text = "ابـدأ بـإضـافـة الـراجـيـتـة",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "التقط صورة للراجيتة الطبية مالتك أو حمّلها من البوم الأستوديو لقراءتها بدقة",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                // Upload Button
                                OutlinedButton(
                                    onClick = { galleryLauncher.launch("image/*") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(55.dp)
                                        .testTag("gallery_button"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = primaryColor
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = "الاستوديو"
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("الألبوم عيني (الاستوديو)", fontSize = 12.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Camera Button
                                Button(
                                    onClick = { cameraLauncher.launch() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(55.dp)
                                        .testTag("camera_button"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = primaryColor
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "الكاميرا"
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("التقاط صورة", fontSize = 12.sp, color = Color.White)
                                }
                            }
                        } else {
                            // Image Selected state
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(
                                        1.dp,
                                        primaryColor.copy(alpha = 0.3f),
                                        RoundedCornerShape(16.dp)
                                    )
                            ) {
                                Image(
                                    bitmap = selectedBitmap!!.asImageBitmap(),
                                    contentDescription = "الراجيتة المحددة",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Close icon on top right
                                Box(
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.62f))
                                        .clickable { viewModel.clearActiveScan() }
                                        .align(Alignment.TopEnd),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "حذف",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Extra search enrich details
                            OutlinedTextField(
                                value = manualTitle,
                                onValueChange = { manualTitle = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("prescription_title_input"),
                                label = { Text("عنوان أو تفاصيل إضافية (اختياري عيني)") },
                                placeholder = { Text("مثلاً: دكتور باطنية، مفاصل، علاج الضغط...") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Action buttons: Scan as Prescription vs Scan as Lab Test
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Lab Test Scan
                                OutlinedButton(
                                    onClick = {
                                        scanModeIsTest = true
                                        viewModel.runDetections(
                                            title = manualTitle.ifBlank { "تحليل مخبري" },
                                            isTest = true
                                        ) {
                                            manualTitle = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                        .testTag("scan_analysis_button"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = secondaryColor
                                    ),
                                    enabled = !isScanning
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Analytics,
                                        contentDescription = "تحليل",
                                        tint = secondaryColor
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("ترجمة تحليل", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Prescription (Rx) Scan
                                Button(
                                    onClick = {
                                        scanModeIsTest = false
                                        viewModel.runDetections(
                                            title = manualTitle.ifBlank { "وصفة طبية" },
                                            isTest = false
                                        ) {
                                            manualTitle = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                        .testTag("scan_prescription_button"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = primaryColor
                                    ),
                                    enabled = !isScanning
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MedicalServices,
                                        contentDescription = "راجيتة",
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("تفسير راجيتة", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Elegant Dark constant medical warning banner
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF3B383E)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "تنبيه طبي صارم ⚠️",
                                color = Color(0xFFF2B8B5),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = "يمنع منعاً باتاً تغيير الجرعات المكتوبة. إذا كانت النتائج غير واضحة أو حرجة، يرجى مراجعة طبيبك المختص فوراً للاطمئنان.",
                            fontSize = 11.sp,
                            color = Color(0xFFCAC4D0),
                            textAlign = TextAlign.Right,
                            modifier = Modifier.padding(top = 4.dp),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Warnings and Fallbacks Alert when scanning
            if (GeminiService.isApiKeyPlaceholder()) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "⚙️ كود الـ API غير مهيأ",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "مفتاح الـ API مالتك مو شغال أو فارغ بـ AI Studio. تم تشغيل المحاكاة الذكية الذاتية لمراجعة عمل التطبيق بسلاسة وجمال!",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                textAlign = TextAlign.Right,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            // Scanning progress indicators
            item {
                AnimatedVisibility(
                    visible = isScanning,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = primaryColor)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = iraqiLoadingMessages[rotatingMessageIndex],
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = primaryColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }

            // Active OCR Scan Result Card (Full detail overlay)
            item {
                AnimatedVisibility(
                    visible = !activeScanResult.isNullOrBlank() && !isScanning,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.clearActiveScan() }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                            }
                            Text(
                                text = "تحليل وقراءة الراجيتة الحالية 📜",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        ResultViewerCard(resultText = activeScanResult ?: "")
                    }
                }
            }

            // Custom Developer Profile Cards (MANDATORY)
            item {
                Spacer(modifier = Modifier.height(20.dp))
                DeveloperCard()
            }

            // Scanned History List Header
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "السجل",
                            tint = primaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "السجل الطبي مالتك",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = "عدد السجلات (${prescriptionsHistory.size})",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }

            // Scanned History Elements (Room database list mappings)
            if (prescriptionsHistory.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "سجل فارغ",
                                tint = primaryColor.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "السجل مالتك فارغ حالياً عيني",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "أي راجيتة أو تحليل كيميائي تسويله قراءة راح ينحفظ هنا تلقائياً لسهولة الرجوع إله بعدين.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp, start = 12.dp, end = 12.dp)
                            )
                        }
                    }
                }
            } else {
                items(prescriptionsHistory) { item ->
                    HistoryItemRow(
                        item = item,
                        onClick = { viewModel.viewPrescriptionDetails(item) },
                        onDelete = { viewModel.deleteHistoryId(item.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Historical Scan Full Result Overlay dialog
    if (focusedPrescription != null) {
        val prescription = focusedPrescription!!
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.62f))
                .clickable { viewModel.closePrescriptionDetails() }
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .clickable(enabled = false) {}, // prevent closing on click inside
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Header row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.closePrescriptionDetails() }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "أغلق")
                        }
                        Text(
                            text = prescription.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "تاريخ القراءة: " + formatTimestamp(prescription.date),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ResultViewerCard(resultText = prescription.resultText)
                }
            }
        }
    }
}

// Subordinate composable to display formatted OCR & Diagnostic details
@Composable
fun ResultViewerCard(
    resultText: String,
    modifier: Modifier = Modifier
) {
    val lines = resultText.split("\n")
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("result_display_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
        ),
        border = borderStroke()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(320.dp)
        ) {
            items(lines) { line ->
                val isHeaderIndex = line.trim().startsWith("##")
                val isBulletPoint = line.trim().startsWith("*") || line.trim().startsWith("-")

                val cleanedLine = line
                    .replace("##", "")
                    .replace("*", "")
                    .replace("-", "• ")
                    .trim()

                if (cleanedLine.isNotBlank()) {
                    if (isHeaderIndex) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = cleanedLine,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            textAlign = TextAlign.Right
                        )
                    } else if (cleanedLine.contains("غير واضحة في الصورة") || cleanedLine.contains("يمنع منعاً باتاً")) {
                        Text(
                            text = cleanedLine,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            textAlign = TextAlign.Right
                        )
                    } else {
                        Text(
                            text = cleanedLine,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .padding(end = if (isBulletPoint) 10.dp else 0.dp),
                            textAlign = TextAlign.Right,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItemRow(
    item: PrescriptionEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val isTest = item.type == "TEST"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() }
            .testTag("history_item_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Delete action
            IconButton(
                onClick = { onDelete() },
                modifier = Modifier.testTag("delete_item_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "حذف",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Info items (Right aligned)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatTimestamp(item.date),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                // Type Icon badge
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isTest) {
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                            } else {
                                primaryColor.copy(alpha = 0.12f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isTest) Icons.Default.Analytics else Icons.Default.MedicalServices,
                        contentDescription = "نوع السجل",
                        tint = if (isTest) MaterialTheme.colorScheme.secondary else primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// String Helper for Uri parsing to Bitmap converter
private fun Uri.toBitmap(context: Context): Bitmap? {
    return try {
        val inputStream = context.contentResolver.openInputStream(this)
        android.graphics.BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        null
    }
}

// Help format dates smoothly
private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
private fun borderStroke() = androidx.compose.foundation.BorderStroke(
    width = 1.dp,
    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f)
)
