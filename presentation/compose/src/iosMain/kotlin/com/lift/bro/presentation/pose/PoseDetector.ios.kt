package com.lift.bro.presentation.pose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Suppress("FunctionOnlyReturningConstant", "UnusedParameter")
class PoseAnalyzer(modelPath: String) {
    fun analyze(
        frame: ByteArray,
        width: Int,
        height: Int,
        rotation: Int,
    ): PoseResult? {
        return null
    }

@Suppress("EmptyFunctionBlock")
    fun close() { }
}

class PoseAnalyzerFactory {
    fun create(modelPath: String): PoseAnalyzer {
        return PoseAnalyzer(modelPath)
    }
}

@Composable
fun rememberPoseAnalyzerFactory(): PoseAnalyzerFactory {
    return remember { PoseAnalyzerFactory() }
}
