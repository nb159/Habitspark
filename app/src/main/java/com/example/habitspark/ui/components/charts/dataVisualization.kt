package com.example.habitspark.ui.components.charts

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.IndicatorCount
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Pie


@Composable
fun barChart(
    chartLabel: String,
    chartSourceData: List<Pair<String,List<Int>>>,
    yAxisStepCount: Int = 4,
    yAxisMaxMinValues: Pair<Double, Double> = Pair(90.0, 0.0)
) {
    Text(
        text = chartLabel
    )
    Spacer(modifier = Modifier.height(15.dp))

    ColumnChart(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),

        data = remember {
            chartSourceData.map { (label, values) ->
                Bars(
                    label = label,
                    values = values.map { value ->
                        Bars.Data(value = value.toDouble(), color = SolidColor(Color.Blue),
                            properties = BarProperties(
                                spacing = 10.dp,
                                thickness = 25.dp,
                                style = DrawStyle.Fill,
                                cornerRadius = Bars.Data.Radius.Rectangle(
                                    topLeft = 5.dp,
                                    topRight = 5.dp,
                                )
                            )
                        )
                    }
                )
            }

        },
        //X and Y axis labels
        labelProperties = LabelProperties(
            enabled = true,
            rotation = LabelProperties.Rotation(
                degree = 0f,
            ),
            builder = { modifier, label, isSelected, index ->
                Text(
                    text = label,
                    modifier = modifier
                        .padding(end = 8.dp),
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color.White,
                    )
                )
            }
        ),
        labelHelperProperties = LabelHelperProperties(
            enabled = false,
        ),
        indicatorProperties = HorizontalIndicatorProperties(
            enabled = true,
            textStyle = TextStyle(
                fontSize = 10.sp,
                color = Color.White
            ),
            count = IndicatorCount.CountBased(yAxisStepCount),
        ),
        maxValue = yAxisMaxMinValues.first,
        minValue = yAxisMaxMinValues.second,
        gridProperties = GridProperties(
            enabled = true,
            xAxisProperties = GridProperties.AxisProperties(
                enabled = true,
                color = SolidColor(Color.Gray)
            ),
            yAxisProperties = GridProperties.AxisProperties(
                enabled = true,
                color = SolidColor(Color.Gray)
            )
        ),

        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
    )
}

@Composable
fun pieChart(

){
    var data by remember {
        mutableStateOf(
            listOf(
                Pie(label = "Android", data = 20.0, color = Color.Red, selectedColor = Color.Green),
                Pie(label = "Windows", data = 45.0, color = Color.Cyan, selectedColor = Color.Blue),
                Pie(label = "Linux", data = 35.0, color = Color.Gray, selectedColor = Color.Yellow),
            )
        )
    }
    PieChart(
        modifier = Modifier.size(200.dp),
        data = data,
        onPieClick = {
            println("${it.label} Clicked")
            val pieIndex = data.indexOf(it)
            data = data.mapIndexed { mapIndex, pie -> pie.copy(selected = pieIndex == mapIndex) }
        },
        selectedScale = 1.2f,
        scaleAnimEnterSpec = spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        colorAnimEnterSpec = tween(300),
        colorAnimExitSpec = tween(300),
        scaleAnimExitSpec = tween(300),
        spaceDegreeAnimExitSpec = tween(300),
        style = Pie.Style.Fill
    )

}