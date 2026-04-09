package com.SzpontCompany.check.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

@Composable
fun CheckLogo(
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    val logo = remember(primaryColor, secondaryColor) {
        val bgPath = "m130,0h252c71.8,0 130,58.2 130,130v262c0,71.8 -58.2,130 -130,130h-252c-71.8,0 -130,-58.2 -130,-130v-262c0,-71.8 58.2,-130 130,-130z"
        val checkPath = "m368.5,131.04c2.75,-0.03 7.25,0.59 10,1.38 2.75,0.79 7.02,2.95 9.5,4.81q4.5,3.37 7.85,9.82c3.18,6.13 3.33,6.87 2.93,14.95 -0.31,6.14 -1.03,9.68 -2.6,12.75 -1.2,2.34 -6.2,8.86 -11.1,14.5 -4.91,5.64 -15.99,18.35 -24.62,28.25 -8.63,9.9 -23.92,27.45 -33.98,39 -10.06,11.55 -25.35,29.1 -33.98,39 -8.62,9.9 -20.75,23.85 -26.93,31 -6.19,7.15 -15.26,17.47 -20.16,22.94 -6,6.69 -10.54,10.77 -13.89,12.5 -4.32,2.22 -6.24,2.56 -14.5,2.56 -8.44,0 -10.03,-0.29 -14.02,-2.61 -2.66,-1.54 -9.8,-8.5 -17.5,-17.05 -7.15,-7.94 -17.34,-19.37 -22.64,-25.39 -5.3,-6.02 -15.32,-17.47 -22.26,-25.45 -6.95,-7.98 -13.76,-16.75 -15.14,-19.5 -1.38,-2.75 -2.78,-7.48 -3.11,-10.5q-0.61,-5.5 0.96,-11c0.86,-3.02 3.17,-7.64 5.13,-10.25 1.96,-2.61 5.47,-5.87 7.81,-7.23 2.34,-1.37 6.95,-2.98 10.25,-3.59 4.36,-0.81 7.51,-0.82 11.5,-0.03 3.03,0.6 7.45,2.33 9.83,3.85 2.58,1.64 13.42,13.18 26.75,28.5 12.33,14.16 22.64,25.75 22.92,25.75 0.28,0 2.86,-2.86 5.75,-6.35 2.89,-3.49 12.44,-14.63 21.22,-24.75 8.78,-10.12 23.61,-27.17 32.96,-37.9 9.35,-10.72 23.31,-26.73 31.03,-35.57 7.72,-8.84 19.52,-22.34 26.21,-30 6.7,-7.66 13.67,-15.39 15.5,-17.16 1.83,-1.78 5.58,-4.12 8.33,-5.21 2.75,-1.08 7.25,-1.99 10,-2.02zM361.5,144.82c-1.65,1 -5.76,4.93 -9.13,8.75 -3.37,3.81 -14.4,16.42 -24.5,28.02 -10.1,11.59 -33,37.89 -50.87,58.43 -17.88,20.54 -38.46,44.24 -45.75,52.66 -7.29,8.43 -13.62,15.21 -14.07,15.07 -0.45,-0.14 -3.03,-2.74 -5.75,-5.79 -2.71,-3.05 -14.61,-16.67 -26.43,-30.28 -15.48,-17.8 -22.62,-25.28 -25.5,-26.69 -2.2,-1.08 -5.57,-1.96 -7.5,-1.96 -1.93,0 -5.5,1 -7.94,2.23 -2.6,1.31 -5.5,3.87 -7,6.17 -1.98,3.04 -2.56,5.21 -2.56,9.57 0,4.59 0.56,6.51 3,10.25 1.65,2.52 9.3,11.69 17,20.36 7.7,8.67 22.1,25 32,36.29 9.9,11.29 19.57,21.56 21.5,22.83 1.93,1.26 5.3,2.56 7.5,2.88 2.63,0.38 5.37,0.01 8,-1.07 2.2,-0.92 4.85,-2.42 5.9,-3.35 1.04,-0.93 11.67,-12.94 23.61,-26.69 11.95,-13.75 32.87,-37.82 46.49,-53.5 13.63,-15.67 26.63,-30.53 28.89,-33.01 2.26,-2.49 15.59,-17.7 29.61,-33.8 14.02,-16.1 26.4,-30.95 27.5,-32.98 1.26,-2.34 1.99,-5.56 1.97,-8.71 -0.03,-3.55 -0.76,-6.14 -2.5,-8.92 -1.36,-2.16 -4.04,-4.98 -5.97,-6.26 -2.61,-1.74 -4.9,-2.33 -9,-2.32 -3.53,0 -6.57,0.66 -8.5,1.82z"
        val dotPath = "m370,369.5c-17.7,0 -32,-14.42 -32,-32.25 0,-17.84 14.3,-32.25 32,-32.25 17.7,0 32,14.41 32,32.25 0,17.83 -14.3,32.25 -32,32.25z"

        ImageVector.Builder(
            name = "CheckLogo",
            defaultWidth = 512.dp,
            defaultHeight = 522.dp,
            viewportWidth = 512f,
            viewportHeight = 522f
        ).apply {
            // Tło w kolorze głównego akcentu
            addPath(
                pathData = PathParser().parsePathString(bgPath).toNodes(),
                fill = SolidColor(primaryColor)
            )
            // Ptaszek (zawsze biały)
            addPath(
                pathData = PathParser().parsePathString(checkPath).toNodes(),
                fill = SolidColor(Color.White)
            )
            // Kropeczka w kolorze Secondary
            addPath(
                pathData = PathParser().parsePathString(dotPath).toNodes(),
                fill = SolidColor(secondaryColor)
            )
        }.build()
    }

    Image(
        imageVector = logo,
        contentDescription = "Sukces",
        modifier = modifier
    )
}