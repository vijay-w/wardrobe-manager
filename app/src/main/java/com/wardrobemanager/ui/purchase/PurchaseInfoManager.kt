package com.wardrobemanager.ui.purchase

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseInfoManager @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private val URL_PATTERN = Pattern.compile(
            "^(https?://)?" +
            "([\\w\\-]+\\.)+[\\w\\-]+" +
            "(/[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=]*)?$",
            Pattern.CASE_INSENSITIVE
        )
        
        private val PRICE_PATTERN = Pattern.compile(
            "^\\d+(\\.\\d{1,2})?$"
        )
    }
    
    fun validatePrice(priceString: String): PriceValidationResult {
        if (priceString.isBlank()) {
            return PriceValidationResult.Valid(null)
        }
        
        if (!PRICE_PATTERN.matcher(priceString).matches()) {
            return PriceValidationResult.Invalid("请输入有效的价格格式")
        }
        
        return try {
            val price = priceString.toDouble()
            when {
                price < 0 -> PriceValidationResult.Invalid("价格不能为负数")
                price > 999999.99 -> PriceValidationResult.Invalid("价格过高")
                else -> PriceValidationResult.Valid(price)
            }
        } catch (e: NumberFormatException) {
            PriceValidationResult.Invalid("请输入有效的数字")
        }
    }
    
    fun validatePurchaseLink(link: String): LinkValidationResult {
        if (link.isBlank()) {
            return LinkValidationResult.Valid(null)
        }
        
        val normalizedLink = if (!link.startsWith("http://") && !link.startsWith("https://")) {
            "https://$link"
        } else {
            link
        }
        
        return if (URL_PATTERN.matcher(normalizedLink).matches()) {
            LinkValidationResult.Valid(normalizedLink)
        } else {
            LinkValidationResult.Invalid("请输入有效的链接格式")
        }
    }
    
    fun openPurchaseLink(link: String): LinkOpenResult {
        return try {
            val validationResult = validatePurchaseLink(link)
            when (validationResult) {
                is LinkValidationResult.Valid -> {
                    val normalizedLink = validationResult.link ?: return LinkOpenResult.Error("链接为空")
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(normalizedLink)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    LinkOpenResult.Success
                }
                is LinkValidationResult.Invalid -> {
                    LinkOpenResult.Error(validationResult.error)
                }
            }
        } catch (e: Exception) {
            LinkOpenResult.Error("无法打开链接: ${e.message}")
        }
    }
    
    fun formatPrice(price: Double?): String {
        return price?.let { "¥${String.format("%.2f", it)}" } ?: ""
    }
    
    fun parsePriceFromString(priceString: String): Double? {
        return try {
            // Remove currency symbols and whitespace
            val cleanPrice = priceString.replace(Regex("[¥$€£\\s,]"), "")
            if (cleanPrice.isBlank()) null else cleanPrice.toDouble()
        } catch (e: NumberFormatException) {
            null
        }
    }
    
    fun extractDomainFromUrl(url: String): String? {
        return try {
            val uri = Uri.parse(url)
            uri.host?.let { host ->
                // Remove www. prefix if present
                if (host.startsWith("www.")) {
                    host.substring(4)
                } else {
                    host
                }
            }
        } catch (e: Exception) {
            null
        }
    }
    
    fun generatePurchaseInfoSummary(price: Double?, link: String?): String {
        val parts = mutableListOf<String>()
        
        price?.let { parts.add(formatPrice(it)) }
        
        link?.let { url ->
            extractDomainFromUrl(url)?.let { domain ->
                parts.add("来自 $domain")
            }
        }
        
        return if (parts.isEmpty()) "无购买信息" else parts.joinToString(" • ")
    }
}

sealed class PriceValidationResult {
    data class Valid(val price: Double?) : PriceValidationResult()
    data class Invalid(val error: String) : PriceValidationResult()
}

sealed class LinkValidationResult {
    data class Valid(val link: String?) : LinkValidationResult()
    data class Invalid(val error: String) : LinkValidationResult()
}

sealed class LinkOpenResult {
    object Success : LinkOpenResult()
    data class Error(val message: String) : LinkOpenResult()
}