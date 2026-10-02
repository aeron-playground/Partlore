package dev.partlore.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** Corner radii: xs chips, sm inputs, md cards, lg sheets, xl big buttons, full pills and pins. */
object PartloreShapes {
    val xs = RoundedCornerShape(6.dp)
    val sm = RoundedCornerShape(10.dp)
    val md = RoundedCornerShape(16.dp)
    val lg = RoundedCornerShape(24.dp)
    val xl = RoundedCornerShape(32.dp)
    val full = RoundedCornerShape(percent = 50)
}

/** Line widths. */
object PartloreStroke {
    val hairline = 1.dp
}
