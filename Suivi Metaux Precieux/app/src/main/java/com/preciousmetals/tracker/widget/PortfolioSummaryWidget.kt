package com.preciousmetals.tracker.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.preciousmetals.tracker.MainActivity
import com.preciousmetals.tracker.SuiviMetauxApp
import com.preciousmetals.tracker.domain.model.Currency
import com.preciousmetals.tracker.ui.theme.NegativeRedDark
import com.preciousmetals.tracker.ui.theme.OnBackgroundDark
import com.preciousmetals.tracker.ui.theme.PositiveGreenDark
import com.preciousmetals.tracker.ui.theme.TextMuted44Dark
import com.preciousmetals.tracker.util.formatMoney
import com.preciousmetals.tracker.util.formatPercent
import com.preciousmetals.tracker.util.usdTo
import kotlinx.coroutines.flow.first

private data class PortfolioTotals(
    val costBasisUsd: Double?,
    val gainLossUsd: Double?,
    val gainLossPercent: Double?,
)

/**
 * A small home-screen widget mirroring the "Coût total achat / Plus-value / %" card at the top
 * of the Portefeuille tab. Reads the same cached portfolio/price data as the app — no network
 * call from the widget itself — so it reflects whatever was last refreshed, in the background or
 * in the app. Tapping it opens the app.
 */
class PortfolioSummaryWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val container = (context.applicationContext as SuiviMetauxApp).container
        val currency = container.userPreferences.displayCurrency.first()
        val usdToEurRate = container.priceRepository.usdToEurRate.first()
        val summary = container.portfolioRepository.observePortfolio().first()
        val totals = PortfolioTotals(
            costBasisUsd = summary.totalCostBasisUsd,
            gainLossUsd = summary.totalGainLossUsd,
            gainLossPercent = summary.totalGainLossPercent,
        )

        provideContent {
            WidgetContent(totals = totals, currency = currency, usdToEurRate = usdToEurRate)
        }
    }

    companion object {
        /** Called after a successful price refresh so pinned widgets reflect the new totals. */
        suspend fun refreshAllInstances(context: Context) {
            PortfolioSummaryWidget().updateAll(context)
        }
    }
}

@Composable
private fun WidgetContent(totals: PortfolioTotals, currency: Currency, usdToEurRate: Double) {
    fun money(usd: Double) = formatMoney(usd.usdTo(currency, usdToEurRate), currency)

    val gainColor = when {
        totals.gainLossUsd == null -> OnBackgroundDark
        totals.gainLossUsd >= 0 -> PositiveGreenDark
        else -> NegativeRedDark
    }

    WidgetCard(onClick = actionStartActivity<MainActivity>()) {
        Row(modifier = GlanceModifier.fillMaxSize().padding(10.dp)) {
            StatColumn(
                label = "Coût total",
                value = totals.costBasisUsd?.let { money(it) } ?: "—",
                valueColor = OnBackgroundDark,
                modifier = GlanceModifier.defaultWeight(),
            )
            StatColumn(
                label = "Plus-value",
                value = totals.gainLossUsd?.let { money(it) } ?: "—",
                valueColor = gainColor,
                modifier = GlanceModifier.defaultWeight(),
            )
            StatColumn(
                label = "%",
                value = totals.gainLossPercent?.let { formatPercent(it) } ?: "—",
                valueColor = gainColor,
                modifier = GlanceModifier.defaultWeight(),
            )
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color, modifier: GlanceModifier = GlanceModifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = TextStyle(color = ColorProvider(TextMuted44Dark), fontSize = 10.sp),
        )
        Text(
            text = value,
            style = TextStyle(color = ColorProvider(valueColor), fontWeight = FontWeight.Bold, fontSize = 13.sp),
        )
    }
}
