package com.example.habitspark.ui.components.charts

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.IndicatorCount
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties


@Composable
fun BarChart(
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

