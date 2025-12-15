package com.wardrobemanager.ui.purchase

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun PurchaseInfoCard(
    price: Double?,
    purchaseLink: String?,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
    purchaseInfoManager: PurchaseInfoManager = hiltViewModel<PurchaseInfoViewModel>().purchaseInfoManager
) {
    val context = LocalContext.current
    var showLinkError by remember { mutableStateOf<String?>(null) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "购买信息",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "编辑购买信息"
                    )
                }
            }
            
            // Price information
            if (price != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = "价格",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = purchaseInfoManager.formatPrice(price),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Purchase link
            if (!purchaseLink.isNullOrBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "购买链接",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    
                    Column(modifier = Modifier.weight(1f)) {
                        val domain = purchaseInfoManager.extractDomainFromUrl(purchaseLink)
                        
                        Text(
                            text = domain ?: "购买链接",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable {
                                when (val result = purchaseInfoManager.openPurchaseLink(purchaseLink)) {
                                    is LinkOpenResult.Success -> {
                                        // Link opened successfully
                                    }
                                    is LinkOpenResult.Error -> {
                                        showLinkError = result.message
                                    }
                                }
                            }
                        )
                        
                        if (domain != null) {
                            Text(
                                text = "点击访问",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = {
                            when (val result = purchaseInfoManager.openPurchaseLink(purchaseLink)) {
                                is LinkOpenResult.Success -> {
                                    // Link opened successfully
                                }
                                is LinkOpenResult.Error -> {
                                    showLinkError = result.message
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "打开链接"
                        )
                    }
                }
            }
            
            // Empty state
            if (price == null && purchaseLink.isNullOrBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "暂无购买信息",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = onEditClick) {
                            Text("添加购买信息")
                        }
                    }
                }
            }
        }
    }
    
    // Error dialog for link opening
    showLinkError?.let { error ->
        AlertDialog(
            onDismissRequest = { showLinkError = null },
            title = { Text("无法打开链接") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { showLinkError = null }) {
                    Text("确定")
                }
            }
        )
    }
}

@Composable
fun PurchaseInfoEditDialog(
    initialPrice: Double?,
    initialLink: String?,
    onSave: (Double?, String?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    purchaseInfoManager: PurchaseInfoManager = hiltViewModel<PurchaseInfoViewModel>().purchaseInfoManager
) {
    var priceText by remember { 
        mutableStateOf(initialPrice?.toString() ?: "") 
    }
    var linkText by remember { 
        mutableStateOf(initialLink ?: "") 
    }
    var priceError by remember { mutableStateOf<String?>(null) }
    var linkError by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑购买信息") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { newValue ->
                        priceText = newValue
                        priceError = when (val result = purchaseInfoManager.validatePrice(newValue)) {
                            is PriceValidationResult.Valid -> null
                            is PriceValidationResult.Invalid -> result.error
                        }
                    },
                    label = { Text("价格") },
                    placeholder = { Text("0.00") },
                    leadingIcon = {
                        Text("¥", style = MaterialTheme.typography.bodyLarge)
                    },
                    isError = priceError != null,
                    supportingText = priceError?.let { { Text(it) } },
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = linkText,
                    onValueChange = { newValue ->
                        linkText = newValue
                        linkError = when (val result = purchaseInfoManager.validatePurchaseLink(newValue)) {
                            is LinkValidationResult.Valid -> null
                            is LinkValidationResult.Invalid -> result.error
                        }
                    },
                    label = { Text("购买链接") },
                    placeholder = { Text("https://example.com") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null
                        )
                    },
                    isError = linkError != null,
                    supportingText = linkError?.let { { Text(it) } },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val priceValidation = purchaseInfoManager.validatePrice(priceText)
                    val linkValidation = purchaseInfoManager.validatePurchaseLink(linkText)
                    
                    if (priceValidation is PriceValidationResult.Valid && 
                        linkValidation is LinkValidationResult.Valid) {
                        onSave(priceValidation.price, linkValidation.link)
                    }
                },
                enabled = priceError == null && linkError == null
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun PurchaseInfoViewModel(): PurchaseInfoViewModel = hiltViewModel()

class PurchaseInfoViewModel @javax.inject.Inject constructor(
    val purchaseInfoManager: PurchaseInfoManager
) : androidx.lifecycle.ViewModel()