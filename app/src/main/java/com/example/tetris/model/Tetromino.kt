package com.example.tetris.model

/**
 * 7种标准俄罗斯方块类型，每种有独特颜色。
 * 颜色使用 ARGB Long 值。
 */
enum class PieceType(val color: Long) {
    I(0xFF00F5FF),  // 青色
    O(0xFFFFEB3B),  // 黄色
    T(0xFFD500F9),  // 紫色
    S(0xFF00E676),  // 绿色
    Z(0xFFFF1744),  // 红色
    J(0xFF2979FF),  // 蓝色
    L(0xFFFF9100);  // 橙色

    companion object {
        /** 每种方块的初始形状矩阵（旋转0） */
        private val baseShapes: Map<PieceType, Array<BooleanArray>> = mapOf(
            I to arrayOf(
                booleanArrayOf(false, false, false, false),
                booleanArrayOf(true, true, true, true),
                booleanArrayOf(false, false, false, false),
                booleanArrayOf(false, false, false, false),
            ),
            O to arrayOf(
                booleanArrayOf(true, true),
                booleanArrayOf(true, true),
            ),
            T to arrayOf(
                booleanArrayOf(false, true, false),
                booleanArrayOf(true, true, true),
                booleanArrayOf(false, false, false),
            ),
            S to arrayOf(
                booleanArrayOf(false, true, true),
                booleanArrayOf(true, true, false),
                booleanArrayOf(false, false, false),
            ),
            Z to arrayOf(
                booleanArrayOf(true, true, false),
                booleanArrayOf(false, true, true),
                booleanArrayOf(false, false, false),
            ),
            J to arrayOf(
                booleanArrayOf(true, false, false),
                booleanArrayOf(true, true, true),
                booleanArrayOf(false, false, false),
            ),
            L to arrayOf(
                booleanArrayOf(false, false, true),
                booleanArrayOf(true, true, true),
                booleanArrayOf(false, false, false),
            ),
        )

        /** 旋转缓存：每种方块预计算4个旋转状态 */
        private val rotationsCache: Map<PieceType, List<Array<BooleanArray>>> by lazy {
            entries.associateWith { type ->
                val rotations = mutableListOf<Array<BooleanArray>>()
                var current = baseShapes[type]!!
                rotations.add(current)
                for (i in 1..3) {
                    current = rotateClockwise(current)
                    rotations.add(current)
                }
                rotations
            }
        }

        /** 获取指定方块在指定旋转状态的形状矩阵 */
        fun getShape(type: PieceType, rotation: Int): Array<BooleanArray> {
            return rotationsCache[type]!![rotation]
        }

        /** 将形状矩阵顺时针旋转90度 */
        private fun rotateClockwise(shape: Array<BooleanArray>): Array<BooleanArray> {
            val rows = shape.size
            val cols = shape[0].size
            val rotated = Array(cols) { BooleanArray(rows) }
            for (i in 0 until rows) {
                for (j in 0 until cols) {
                    rotated[j][rows - 1 - i] = shape[i][j]
                }
            }
            return rotated
        }

        /** 随机获取一种方块类型 */
        fun random(): PieceType = entries.random()
    }
}

/**
 * 俄罗斯方块实体，表示游戏板上的一个方块。
 *
 * @param type     方块类型
 * @param rotation 当前旋转状态（0-3）
 * @param x        方块左上角的列坐标
 * @param y        方块左上角的行坐标
 */
data class Tetromino(
    val type: PieceType,
    val rotation: Int = 0,
    val x: Int = 0,
    val y: Int = 0,
) {
    /** 当前旋转状态的形状矩阵 */
    val shape: Array<BooleanArray>
        get() = PieceType.getShape(type, rotation)

    /** 获取所有填充格子的绝对坐标 (col, row) */
    val cells: List<Pair<Int, Int>>
        get() {
            val result = mutableListOf<Pair<Int, Int>>()
            shape.forEachIndexed { row, rowArray ->
                rowArray.forEachIndexed { col, filled ->
                    if (filled) {
                        result.add(x + col to y + row)
                    }
                }
            }
            return result
        }

    /** 方块颜色 */
    val color: Long get() = type.color

    /** 旋转后的新方块 */
    fun rotated(): Tetromino = copy(rotation = (rotation + 1) % 4)

    /** 平移后的新方块 */
    fun moved(dx: Int, dy: Int): Tetromino = copy(x = x + dx, y = y + dy)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Tetromino) return false
        return type == other.type && rotation == other.rotation && x == other.x && y == other.y
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + rotation
        result = 31 * result + x
        result = 31 * result + y
        return result
    }
}
