package com.preciousmetals.tracker.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.preciousmetals.tracker.AppContainer
import com.preciousmetals.tracker.MainActivity
import com.preciousmetals.tracker.SuiviMetauxApp
import com.preciousmetals.tracker.domain.model.Currency
import com.preciousmetals.tracker.domain.model.Metal
import com.preciousmetals.tracker.util.formatMoney
import com.preciousmetals.tracker.util.formatPercent
import com.preciousmetals.tracker.util.usdTo
import java.time.LocalDate
import kotlinx.coroutines.flow.first

private val WidgetBackground = Color(0xFF1B1B21)
private val WidgetOnBackground = Color(0xFFF5F3EF)
private val WidgetOnBackgroundMuted = Color(0xFF9A96A8)
private val WidgetBrandGold = Color(0xFFF2B705)
private val WidgetPositive = Color(0xFF34C77B)
private val WidgetNegative = Color(0xFFFF6B6B)

private data class MetalPriceRow(
    val metal: Metal,
    val priceUsdPerGram: Double?,
    val changePercent: Double?,
)

/**
 * A compact home-screen widget showing every tracked metal's latest cached spot price (see
 * [com.preciousmetals.tracker.data.repository.PriceRepository]) and its move since yesterday's
 * cached close. Reads from the local cache only — no network call from the widget itself — so it
 * stays fast and in sync with whatever the app last refreshed. Tapping it opens the app.
 */
class SpotPriceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val container = (context.applicationContext as SuiviMetauxApp).container
        val currency = container.userPreferences.displayCurrency.first()
        val usdToEurRate = container.priceRepository.usdToEurRate.first()
        val rows = Metal.entries.map { metal -> loadRow(container, metal) }

        provideContent {
            WidgetContent(rows = rows, currency = currency, usdToEurRate = usdToEurRate)
        }
    }

    private suspend fun loadRow(container: AppContainer, metal: Metal): MetalPriceRow {
        val price = container.priceRepository.getLatestPriceOnceUsdPerGram(metal)
        val yesterday = container.priceRepository
            .getNearestHistoricalPriceUsdPerGram(metal, LocalDate.now().minusDays(1))
        val changePercent = if (price != null && yesterday != null && yesterday > 0.0) {
            ((price - yesterday) / yesterday) * 100.0
        } else {
            null
        }
        return MetalPriceRow(metal, price, changePercent)
    }

    companion object {
        /** Called after a successful price refresh so pinned widgets reflect the new prices. */
        suspend fun refreshAllInstances(context: Context) {
            SpotPriceWidget().updateAll(context)
        }
    }
}

@Composable
private fun WidgetContent(rows: List<MetalPriceRow>, currency: Currency, usdToEurRate: Double) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(WidgetBackground))
            .cornerRadius(20.dp)
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>()),
    ) {
        Text(
            text = "Suivi Métaux",
            style = TextStyle(color = ColorProvider(WidgetBrandGold), fontWeight = FontWeight.Bold, fontSize = 13.sp),
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        rows.forEach { row -> MetalPriceLine(row = row, currency = currency, usdToEurRate = usdToEurRate) }
    }
}

@Composable
private fun MetalPriceLine(row: MetalPriceRow, currency: Currency, usdToEurRate: Double) {
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = row.metal.displayNameFr,
            style = TextStyle(color = ColorProvider(WidgetOnBackground), fontSize = 13.sp),
            modifier = GlanceModifier.defaultWeight(),
        )
        val priceText = row.priceUsdPerGram
            ?.usdTo(currency, usdToEurRate)
            ?.let { "${formatMoney(it, currency)}/g" }
            ?: "…"
        Text(
            text = priceText,
            style = TextStyle(color = ColorProvider(WidgetOnBackground), fontWeight = FontWeight.Medium, fontSize = 13.sp),
        )
        Spacer(modifier = GlanceModifier.width(6.dp))
        val changeColor = when {
            row.changePercent == null -> WidgetOnBackgroundMuted
            row.changePercent >= 0 -> WidgetPositive
            else -> WidgetNegative
        }
        Text(
            text = row.changePercent?.let { formatPercent(it) } ?: "",
            style = TextStyle(color = ColorProvider(changeColor), fontSize = 11.sp),
        )
    }
}
