package com.example.blinkitclone

import android.app.Activity
import android.os.Bundle
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// 1. THEME & COLORS
val BlinkitYellow = Color(0xFFF8CB46)
val BlinkitGreen = Color(0xFF318616)
val DarkSurface = Color(0xFF1C1C1C)
val LightSurface = Color(0xFFF5F5F5)

private val DarkColorScheme = darkColorScheme(
    primary = BlinkitYellow, background = Color.Black, surface = DarkSurface, onPrimary = Color.Black, error = Color(0xFFCF6679)
)
private val LightColorScheme = lightColorScheme(
    primary = BlinkitYellow, background = Color.White, surface = LightSurface, onPrimary = Color.Black, error = Color(0xFFB3261E)
)

@Composable
fun MyAppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
        }
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}

// 2. DATA MODELS
data class Product(val id: Int, val name: String, val size: String, val price: Int, val emoji: String)

val groceryItems = listOf(
    Product(1, "Amul Taaza Toned Milk", "500 ml", 34, "🥛"),
    Product(2, "Harvest Gold White Bread", "400 g", 40, "🍞"),
    Product(3, "Farm Fresh White Eggs", "6 pieces", 60, "🥚"),
    Product(4, "Amul Pasteurized Butter", "100 g", 58, "🧈"),
    Product(5, "Mother Dairy Paneer", "200 g", 85, "🧀"),
    Product(6, "India Gate Basmati Rice", "1 kg", 120, "🍚"),
    Product(7, "Tata Salt Vacuum Evaporated", "1 kg", 25, "🧂"),
    Product(8, "Aashirvaad Superior Atta", "5 kg", 210, "🌾"),
    Product(9, "Madhur Pure Sugar", "1 kg", 55, "🍬"),
    Product(10, "Tata Sampann Toor Dal", "1 kg", 160, "🍲"),
    Product(11, "Fresh Red Onion", "1 kg", 40, "🧅"),
    Product(12, "Fresh Potato", "1 kg", 30, "🥔"),
    Product(13, "Fresh Tomato", "1 kg", 50, "🍅"),
    Product(14, "Cavendish Banana", "6 pieces", 60, "🍌"),
    Product(15, "Royal Gala Apple", "4 pieces", 180, "🍎")
)
// 3. NAVIGATION STATE

enum class AppScreen { Splash, Login, Register, Home, Cart, Payment, Success }

@Composable
fun MainAppNavigation() {
    var currentScreen by remember { mutableStateOf(AppScreen.Splash) }
    val cart = remember { mutableStateMapOf<Product, Int>() }

    fun updateCart(product: Product, quantity: Int) {
        if (quantity > 0) cart[product] = quantity else cart.remove(product)
    }

    when (currentScreen) {
        AppScreen.Splash -> BlinkitSplashScreen { currentScreen = AppScreen.Login }
        AppScreen.Login -> LoginScreen(onNavigateToRegister = { currentScreen = AppScreen.Register }, onLoginSuccess = { currentScreen = AppScreen.Home })
        AppScreen.Register -> RegistrationScreen(onNavigateToLogin = { currentScreen = AppScreen.Login }, onRegisterSuccess = { currentScreen = AppScreen.Home })
        AppScreen.Home -> HomeScreen(cart = cart, onUpdateCart = ::updateCart, onNavigateToCart = { currentScreen = AppScreen.Cart })
        AppScreen.Cart -> CartScreen(cart = cart, onUpdateCart = ::updateCart, onBack = { currentScreen = AppScreen.Home }, onCheckout = { currentScreen = AppScreen.Payment })
        AppScreen.Payment -> PaymentScreen(cart = cart, onBack = { currentScreen = AppScreen.Cart }, onPaymentSuccess = { cart.clear(); currentScreen = AppScreen.Success })
        AppScreen.Success -> SuccessScreen(onContinueShopping = { currentScreen = AppScreen.Home })
    }
}
// 4. AUTH SCREENS
@Composable
fun BlinkitSplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) { delay(1500); onTimeout() }
    Box(modifier = Modifier.fillMaxSize().background(BlinkitYellow), contentAlignment = Alignment.Center) {
        Text("blinkit", fontSize = 56.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black, letterSpacing = (-1.5).sp)
    }
}

@Composable
fun LoginScreen(onNavigateToRegister: () -> Unit, onLoginSuccess: () -> Unit) {
    var phoneNumber by remember { mutableStateOf("") }
    var isPhoneError by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("blinkit", fontSize = 42.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground, letterSpacing = (-1.sp))
        Text("India's last minute app", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Gray, modifier = Modifier.padding(bottom = 48.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { if (it.length <= 10 && it.all { char -> char.isDigit() }) { phoneNumber = it; isPhoneError = false } },
            label = { Text("Phone Number") }, prefix = { Text("+91 ") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp), singleLine = true, isError = isPhoneError,
            supportingText = { if (isPhoneError) Text("Please enter a valid 10-digit number", color = MaterialTheme.colorScheme.error) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { if (phoneNumber.length == 10) onLoginSuccess() else isPhoneError = true },
            modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = BlinkitYellow)
        ) { Text("Continue", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onNavigateToRegister) { Text("Don't have an account? Sign up", color = MaterialTheme.colorScheme.onBackground) }
    }
}

@Composable
fun RegistrationScreen(onNavigateToLogin: () -> Unit, onRegisterSuccess: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isNameError by remember { mutableStateOf(false) }
    var isPhoneError by remember { mutableStateOf(false) }
    var isEmailError by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Create Account", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(bottom = 32.dp))

        OutlinedTextField(
            value = name, onValueChange = { if (it.all { char -> char.isLetter() || char.isWhitespace() }) { name = it; isNameError = false } },
            label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, isError = isNameError
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = phoneNumber, onValueChange = { if (it.length <= 10 && it.all { char -> char.isDigit() }) { phoneNumber = it; isPhoneError = false } },
            label = { Text("Phone Number") }, prefix = { Text("+91 ") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, isError = isPhoneError
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = email, onValueChange = { email = it; isEmailError = false },
            label = { Text("Email Address (Optional)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, isError = isEmailError
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                val isNameValid = name.isNotBlank()
                val isPhoneValid = phoneNumber.length == 10
                val isEmailValid = email.isEmpty() || Patterns.EMAIL_ADDRESS.matcher(email).matches()
                isNameError = !isNameValid; isPhoneError = !isPhoneValid; isEmailError = !isEmailValid
                if (isNameValid && isPhoneValid && isEmailValid) onRegisterSuccess()
            },
            modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = BlinkitYellow)
        ) { Text("Sign Up", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onNavigateToLogin) { Text("Already have an account? Log in", color = MaterialTheme.colorScheme.onBackground) }
    }
}
// 5. SHOPPING & CHECKOUT SCREENS
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(cart: Map<Product, Int>, onUpdateCart: (Product, Int) -> Unit, onNavigateToCart: () -> Unit) {
    val totalItems = cart.values.sum()
    val totalPrice = cart.entries.sumOf { it.key.price * it.value }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Delivery in 10 minutes", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Home - Mumbai, Maharashtra", fontSize = 12.sp, color = Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            if (totalItems > 0) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp).clip(RoundedCornerShape(12.dp)).background(BlinkitGreen).clickable { onNavigateToCart() }.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("$totalItems item${if (totalItems > 1) "s" else ""}", color = Color.White, fontSize = 12.sp)
                            Text("₹$totalPrice", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("View Cart >", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), modifier = Modifier.fillMaxSize().padding(paddingValues).background(MaterialTheme.colorScheme.surface),
            contentPadding = PaddingValues(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(groceryItems) { product ->
                val quantity = cart[product] ?: 0
                ProductCard(product, quantity) { newQuantity -> onUpdateCart(product, newQuantity) }
            }
        }
    }
}

@Composable
fun ProductCard(product: Product, quantity: Int, onQuantityChange: (Int) -> Unit) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF3F3F3)), contentAlignment = Alignment.Center) {
                Text(text = product.emoji, fontSize = 60.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = product.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.height(40.dp))
            Text(text = product.size, fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "₹${product.price}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                if (quantity == 0) {
                    Box(modifier = Modifier.border(1.dp, BlinkitGreen, RoundedCornerShape(6.dp)).clickable { onQuantityChange(1) }.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        Text("ADD", color = BlinkitGreen, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    }
                } else {
                    Row(modifier = Modifier.background(BlinkitGreen, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("-", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onQuantityChange(quantity - 1) }.padding(horizontal = 4.dp))
                        Text("$quantity", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                        Text("+", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onQuantityChange(quantity + 1) }.padding(horizontal = 4.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(cart: Map<Product, Int>, onUpdateCart: (Product, Int) -> Unit, onBack: () -> Unit, onCheckout: () -> Unit) {
    val totalPrice = cart.entries.sumOf { it.key.price * it.value }
    val deliveryFee = if (totalPrice > 0) 15 else 0
    val grandTotal = totalPrice + deliveryFee

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, // Fixed ArrowBack
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            if (cart.isNotEmpty()) {
                Surface(shadowElevation = 8.dp) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Total to pay", fontSize = 12.sp, color = Color.Gray)
                            Text("₹$grandTotal", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(onClick = onCheckout, colors = ButtonDefaults.buttonColors(containerColor = BlinkitGreen), shape = RoundedCornerShape(8.dp)) {
                            Text("Proceed to Pay", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (cart.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Your cart is empty", color = Color.Gray) }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues).background(MaterialTheme.colorScheme.surface), contentPadding = PaddingValues(16.dp)) {
                items(cart.entries.toList()) { (product, quantity) ->
                    Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp)).padding(12.dp).padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(product.emoji, fontSize = 40.sp, modifier = Modifier.padding(end = 12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.name, fontWeight = FontWeight.SemiBold)
                            Text(product.size, color = Color.Gray, fontSize = 12.sp)
                            Text("₹${product.price}", fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.background(BlinkitGreen, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("-", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onUpdateCart(product, quantity - 1) }.padding(horizontal = 4.dp))
                            Text("$quantity", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                            Text("+", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onUpdateCart(product, quantity + 1) }.padding(horizontal = 4.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Bill Details", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Item Total"); Text("₹$totalPrice") }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Delivery Fee"); Text("₹$deliveryFee") }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp)) // Fixed Divider

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Grand Total", fontWeight = FontWeight.Bold); Text("₹$grandTotal", fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(cart: Map<Product, Int>, onBack: () -> Unit, onPaymentSuccess: () -> Unit) {
    val totalAmount = cart.entries.sumOf { it.key.price * it.value } + 15
    var selectedMethod by remember { mutableStateOf("UPI") }
    val paymentMethods = listOf("UPI (Google Pay, PhonePe)", "Credit / Debit Card", "Cash on Delivery")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } } // Fixed ArrowBack
            )
        },
        bottomBar = {
            Button(onClick = onPaymentSuccess, modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = BlinkitGreen), shape = RoundedCornerShape(8.dp)) {
                Text("Pay ₹$totalAmount", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            Text("Select Payment Method", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
            paymentMethods.forEach { method ->
                Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp)).clickable { selectedMethod = method }.padding(16.dp).padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = (selectedMethod == method), onClick = { selectedMethod = method }, colors = RadioButtonDefaults.colors(selectedColor = BlinkitGreen))
                    Text(method, modifier = Modifier.padding(start = 8.dp), fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun SuccessScreen(onContinueShopping: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(modifier = Modifier.size(100.dp).background(BlinkitGreen, CircleShape), contentAlignment = Alignment.Center) { Text("✅", fontSize = 50.sp) }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Order Placed Successfully!", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Arriving in 10 minutes", color = Color.Gray, modifier = Modifier.padding(top = 8.dp, bottom = 32.dp))
        Button(onClick = onContinueShopping, colors = ButtonDefaults.buttonColors(containerColor = BlinkitYellow)) { Text("Back to Home", color = Color.Black, fontWeight = FontWeight.Bold) }
    }
}
// 6. MAIN ACTIVITY (ENTRY POINT)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MyAppTheme { Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { MainAppNavigation() } } }
    }
}