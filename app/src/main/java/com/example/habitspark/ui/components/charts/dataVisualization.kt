package com.example.habitspark.ui.components.charts

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.habitspark.data.dataTypes.LegendItem
import com.example.habitspark.data.dataTypes.PieSlice
import com.example.habitspark.ui.theme.PrimaryText
import ir.ehsannarmani.compose_charts.ColumnChart
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
    chartSourceData: List<Pair<String,List<Double>>>,
    yAxisStepCount: Int = 4,
    yAxisMaxMinValues: Pair<Double, Double> = Pair(90.0, 0.0)
) {
    ColumnChart(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),

        data = remember {
            chartSourceData.map { (label, values) ->
                Bars(
                    label = label,
                    values = values.map { value ->
                        Bars.Data(value = value, color = SolidColor(Color.Blue),
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
    data: List<PieSlice>,
    labelSize: Float = 35f
){
    var chartData by remember {
        mutableStateOf(
            data.map { pieSlice ->
                Pie(
                    label = "${pieSlice.label}\n${pieSlice.percentage.toInt()}%",
                    data = pieSlice.percentage.toDouble(),
                    color = pieSlice.color,
                    selectedColor = pieSlice.color.copy(alpha = 0.8f),
                    selected = false
                )
            }
        )
    }
    modPieChart(
        modifier = Modifier.size(200.dp),
        data = chartData,
        onPieClick = { clickedPie ->
            chartData = chartData.map { pie ->
                pie.copy(selected = clickedPie != null && pie == clickedPie)
            }
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
        spaceDegree = 1f,
        selectedPaddingDegree = 2f,
        style = Pie.Style.Stroke(width = 30.dp),
        labelSize = labelSize
    )

}

@Composable
fun dataLegend(
    items: List<LegendItem>,
    modifier: Modifier = Modifier,
    boxSize: Dp = 16.dp,
    spacing: Dp = 8.dp,
    fontSize: Float = 14f
) {
    Column(modifier = modifier) {
        items.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(boxSize)
                        .background(item.color, shape = RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(spacing))
                Text(
                    text = item.label,
                    style = TextStyle(
                        fontSize = fontSize.sp,
                        color = PrimaryText
                    )
                )
            }
        }
    }
}
@Composable
fun spaceDivider(
    height: Int = 16,
    divide: Boolean = false,
    dividerFraction: Float = 0.5f
){
    val spaceHeight = (height/2).dp
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(spaceHeight))
        if (divide) {
            Divider(
                color = Color.LightGray.copy(alpha = 0.5f),
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth(dividerFraction)
            )
        }
        Spacer(modifier = Modifier.height(spaceHeight))
    }

}
