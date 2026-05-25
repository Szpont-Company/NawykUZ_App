package com.SzpontCompany.check.widgets

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * WidgetReceiver - odbiornik dla widżetu nawyków.
 *
 * Klasa pośrednicząca między systemem Android a komponentem widżetu (HabitWidget).
 * Umożliwia systemowi zarządzanie cyklem życia widżetu i wysyłanie aktualizacji.
 *
 * @since 1.0
 * @author Szpont Company
 */
class WidgetReceiver : GlanceAppWidgetReceiver() {
    /**
     * Instancja widżetu nawyków do wyświetlania.
     */
    override val glanceAppWidget : GlanceAppWidget = HabitWidget()
}

/**
 * StepsWidgetReceiver - odbiornik dla widżetu kroków.
 *
 * Klasa pośrednicząca między systemem Android a komponentem widżetu (StepsWidget).
 * Umożliwia systemowi zarządzanie cyklem życia widżetu i wysyłanie aktualizacji.
 *
 * @since 1.0
 * @author Szpont Company
 */
class StepsWidgetReceiver : GlanceAppWidgetReceiver() {
    /**
     * Instancja widżetu kroków do wyświetlania.
     */
    override val glanceAppWidget: GlanceAppWidget = StepsWidget()
}