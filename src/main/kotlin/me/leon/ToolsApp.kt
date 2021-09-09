package me.leon

import javafx.scene.image.Image
import me.leon.ext.Prefs
import me.leon.view.Home
import tornadofx.App
import tornadofx.FX
import tornadofx.addStageIcon
import java.util.*

class ToolsApp : App(Home::class, Styles::class) {
    init {
        // for text i18n
        FX.locale = if (Prefs.language == "zh") Locale.CHINESE else Locale.ENGLISH
        addStageIcon(Image(resources.stream("/tb.png")))
    }
}
