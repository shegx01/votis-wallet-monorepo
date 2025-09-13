package finance.votis.wallet.feature.wallet.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/**
 * QR Code display component that renders a QR-like pattern.
 * Since we're avoiding Android-specific QR libraries for multiplatform compliance,
 * this creates a mock QR code pattern that looks realistic.
 */
@Composable
fun QrCodeDisplay(
    data: String,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val qrColor = MaterialTheme.colorScheme.onSurface

    // Generate a deterministic pattern based on the data
    val pattern =
        remember(data) {
            generateQrPattern(data)
        }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize(),
        ) {
            val canvasSize = this.size
            val gridSize = 25 // 25x25 grid like a real QR code
            val cellSize = canvasSize.width / gridSize

            // Draw corner squares (finder patterns)
            drawCornerSquares(this, gridSize, cellSize, qrColor)

            // Draw data pattern based on the input
            pattern.forEachIndexed { row, rowData ->
                rowData.forEachIndexed { col, filled ->
                    // Skip corner squares
                    if (!isCornerSquare(row, col, gridSize) && filled) {
                        drawRect(
                            color = qrColor,
                            topLeft =
                                Offset(
                                    col * cellSize,
                                    row * cellSize,
                                ),
                            size = Size(cellSize, cellSize),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Generate a deterministic QR-like pattern based on the input data.
 * Creates a realistic looking pattern that's consistent for the same input.
 */
private fun generateQrPattern(data: String): List<List<Boolean>> {
    val gridSize = 25
    val pattern = MutableList(gridSize) { MutableList(gridSize) { false } }

    // Use hash of data to create deterministic pattern
    val hash = data.hashCode()
    val random = kotlin.random.Random(hash)

    // Fill pattern with some randomness but structure
    for (row in 0 until gridSize) {
        for (col in 0 until gridSize) {
            // Skip corner areas
            if (!isCornerSquare(row, col, gridSize)) {
                // Create patterns that look QR-like
                val shouldFill =
                    when {
                        // Timing patterns (horizontal and vertical lines)
                        row == 6 -> col % 2 == 0
                        col == 6 -> row % 2 == 0
                        // Some structured randomness for data area
                        else -> random.nextFloat() < 0.45f
                    }
                pattern[row][col] = shouldFill
            }
        }
    }

    return pattern
}

/**
 * Check if a position is within a corner square (finder pattern).
 */
private fun isCornerSquare(
    row: Int,
    col: Int,
    gridSize: Int,
): Boolean {
    val cornerSize = 7
    return (
        // Top-left corner
        (row < cornerSize && col < cornerSize) ||
            // Top-right corner
            (row < cornerSize && col >= gridSize - cornerSize) ||
            // Bottom-left corner
            (row >= gridSize - cornerSize && col < cornerSize)
    )
}

/**
 * Draw the characteristic corner squares (finder patterns) of QR codes.
 */
private fun drawCornerSquares(
    drawScope: androidx.compose.ui.graphics.drawscope.DrawScope,
    gridSize: Int,
    cellSize: Float,
    color: Color,
) {
    val cornerSize = 7
    val cornerPositions =
        listOf(
            Pair(0, 0), // Top-left
            Pair(0, gridSize - cornerSize), // Top-right
            Pair(gridSize - cornerSize, 0), // Bottom-left
        )

    cornerPositions.forEach { (startRow, startCol) ->
        // Outer square (7x7)
        drawScope.drawRect(
            color = color,
            topLeft = Offset(startCol * cellSize, startRow * cellSize),
            size = Size(cornerSize * cellSize, cornerSize * cellSize),
        )

        // Inner white square (5x5)
        drawScope.drawRect(
            color = Color.White,
            topLeft = Offset((startCol + 1) * cellSize, (startRow + 1) * cellSize),
            size = Size((cornerSize - 2) * cellSize, (cornerSize - 2) * cellSize),
        )

        // Center black square (3x3)
        drawScope.drawRect(
            color = color,
            topLeft = Offset((startCol + 2) * cellSize, (startRow + 2) * cellSize),
            size = Size((cornerSize - 4) * cellSize, (cornerSize - 4) * cellSize),
        )
    }
}
