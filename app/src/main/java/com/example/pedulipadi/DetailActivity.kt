package com.example.pedulipadi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.pedulipadi.ui.theme.NormalBlue
import com.example.pedulipadi.ui.theme.PeduliPadiTheme
import kotlin.math.roundToInt

class DetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val leaf = intent.getStringExtra("leaf") ?: ""
        val confidence = intent.getFloatExtra("confidence", 0f)

        setContent {
            PeduliPadiTheme {
                DetailScreenContent(leaf, confidence)
            }
        }
    }
}

@Composable
fun DetailScreenContent(leaf: String, confidence: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(10.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.Start
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(
                        id = R.drawable.ic_information
                    ),
                    contentDescription = "icon"
                )

                Text(
                    text = "Detail Informasi",
                    modifier = Modifier
                        .padding(start = 5.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            when (leaf) {
                "Bacterial leaf blight" -> {
                    TextInBox(text = "$leaf (kresek/hawar)")
                    ExpandingText(
                        description = stringResource(id = R.string.brown_spot)
                    )

                    Spacer(modifier = Modifier.padding(5.dp))

                    TextInBox(text = "Gejala umum")
                    ExpandingText(
                        description = stringResource(id = R.string.brown_spot_cause)
                    )

                    Spacer(modifier = Modifier.padding(5.dp))

                    TextInBox(text = "Pencegahan")
                    ExpandingText(
                        description = stringResource(id = R.string.brown_spot_prevention)
                    )

                }

                "Leaf smut" -> {
                    TextInBox(text = "$leaf (daun smut)")
                    ExpandingText(
                        description = stringResource(id = R.string.brown_spot)
                    )

                    Spacer(modifier = Modifier.padding(5.dp))

                    TextInBox(text = "Gejala umum")
                    ExpandingText(
                        description = stringResource(id = R.string.brown_spot_cause)
                    )

                    Spacer(modifier = Modifier.padding(5.dp))

                    TextInBox(text = "Pencegahan")
                    ExpandingText(
                        description = stringResource(id = R.string.brown_spot_prevention)
                    )
                }

                else -> {
                    TextInBox(text = "$leaf (bintik coklat)")
                    ExpandingText(
                        description = stringResource(id = R.string.brown_spot)
                    )

                    Spacer(modifier = Modifier.padding(5.dp))

                    TextInBox(text = "Gejala umum")
                    ExpandingText(
                        description = stringResource(id = R.string.brown_spot_cause)
                    )

                    Spacer(modifier = Modifier.padding(5.dp))

                    TextInBox(text = "Pencegahan")
                    ExpandingText(
                        description = stringResource(id = R.string.brown_spot_prevention)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(
                        id = R.drawable.ic_percent
                    ),
                    contentDescription = "icon"
                )

                Text(
                    text = "Tingkat keyakinan model",
                    modifier = Modifier
                        .padding(start = 5.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            val percentageConfidence = (confidence * 100).roundToInt()
            TextInBox(text = "Tingkat keyakinan: $percentageConfidence%")
            ExpandingText(description = stringResource(id = R.string.disclaimer))
        }

    }
}

@Composable
fun TextInBox(text: String) {
    Box(
        modifier = Modifier
            .wrapContentSize()
            .background(
                shape = RoundedCornerShape(20.dp),
                color = NormalBlue
            )
    ) {
        Text(
            text = text,
            modifier = Modifier
                .padding(10.dp),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ExpandingText(description: String) {
    var expanded by remember { mutableStateOf(false) }

    Text(
        description,
        modifier = Modifier
            .padding(10.dp)
            .animateContentSize()
            .clickable {
                expanded = !expanded
            },
        textAlign = TextAlign.Justify,
        fontSize = 12.sp,
        lineHeight = 2.em,
        maxLines = if (!expanded) 2 else 20,
        overflow = TextOverflow.Ellipsis
    )

    if (!expanded) {
        Text(
            text = "klik teks diatas untuk melihat lebih detail",
            modifier = Modifier
                .padding(start = 10.dp),
            fontSize = 12.sp,
            fontStyle = FontStyle.Italic,
            color = Color.Gray
        )
    }

}
