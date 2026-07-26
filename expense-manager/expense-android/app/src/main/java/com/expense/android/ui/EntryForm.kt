package com.expense.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.expense.android.ui.components.PickerField
import com.expense.android.viewmodel.CategoryHint
import com.expense.core.domain.Account
import com.expense.core.domain.Category
import com.expense.core.domain.PaymentMethod
import java.time.LocalDate

/**
 * Shared entry form used by both the Quick Expense and Quick Income screens:
 * account, category, optional payment method, amount, description and date.
 * Presentation only; the submit action is provided by the host and delegates to
 * the ViewModel.
 *
 * When [hint] is supplied (expenses only) the category suggested by the core
 * categoriser is preselected — until the user picks one themselves, after which
 * their choice is never overwritten.
 */
@Composable
fun EntryForm(
    title: String,
    accounts: List<Account>,
    categories: List<Category>,
    paymentMethods: List<PaymentMethod>,
    defaultAccountId: Long,
    status: String,
    submitLabel: String,
    hint: CategoryHint?,
    onDescriptionChanged: (String) -> Unit,
    onSubmit: (
        accountId: Long,
        categoryId: Long?,
        paymentMethodId: Long?,
        amount: String,
        description: String,
        date: String,
    ) -> Unit,
) {
    var selectedAccount by remember(accounts) {
        mutableStateOf(accounts.firstOrNull { it.id() == defaultAccountId } ?: accounts.firstOrNull())
    }
    var selectedCategory by remember(categories) { mutableStateOf<Category?>(null) }
    var categoryTouched by remember(categories) { mutableStateOf(false) }
    var selectedPaymentMethod by remember(paymentMethods) { mutableStateOf<PaymentMethod?>(null) }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now().toString()) }

    // Apply the categoriser's suggestion only while the user hasn't chosen one.
    LaunchedEffect(hint, categories) {
        if (!categoryTouched && hint != null) {
            selectedCategory = categories.firstOrNull { it.id() == hint.categoryId }
        }
    }

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall)

        PickerField(
            label = "Account",
            options = accounts,
            selected = selectedAccount,
            itemLabel = { it.name() },
            onSelect = { selectedAccount = it },
        )
        PickerField(
            label = "Category (optional)",
            options = categories,
            selected = selectedCategory,
            itemLabel = { it.name() },
            onSelect = {
                selectedCategory = it
                categoryTouched = true
            },
        )
        if (hint != null && !categoryTouched) {
            Text(
                "Suggested from the description: ${hint.categoryName}",
                style = MaterialTheme.typography.bodySmall,
            )
        }
        if (paymentMethods.isNotEmpty()) {
            PickerField(
                label = "Payment method (optional)",
                options = paymentMethods,
                selected = selectedPaymentMethod,
                itemLabel = { it.name() },
                onSelect = { selectedPaymentMethod = it },
            )
        }
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = description,
            onValueChange = {
                description = it
                onDescriptionChanged(it)
            },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth(),
        )
        TextButton(onClick = { date = LocalDate.now().toString() }) { Text("Today") }
        Button(
            onClick = {
                selectedAccount?.let { acct ->
                    onSubmit(
                        acct.id(),
                        selectedCategory?.id(),
                        selectedPaymentMethod?.id(),
                        amount,
                        description,
                        date,
                    )
                    amount = ""
                    description = ""
                    categoryTouched = false
                    selectedCategory = null
                }
            },
            enabled = selectedAccount != null,
        ) {
            Text(submitLabel)
        }
        if (status.isNotEmpty()) {
            Text(status)
        }
    }
}
