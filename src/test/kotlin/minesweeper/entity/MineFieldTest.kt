import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import minesweeper.entity.Action
import minesweeper.entity.Cell
import minesweeper.entity.Cells
import minesweeper.entity.Coordinate
import minesweeper.entity.Height
import minesweeper.entity.MineField
import minesweeper.entity.Width

class MineFieldTest : BehaviorSpec({
    Given("MineField 객체를 생성할 때") {
        When("유효한 높이, 너비, 그리고 지뢰 데이터로 생성하면") {
            val height = Height(2)
            val width = Width(2)
            val cells =
                Cells(
                    mapOf(
                        Coordinate(0, 0) to Cell.Mine(Coordinate(0, 0)),
                        Coordinate(1, 0) to Cell.Empty(Coordinate(1, 0)),
                        Coordinate(0, 1) to Cell.Empty(Coordinate(0, 1)),
                        Coordinate(1, 1) to Cell.Mine(Coordinate(1, 1)),
                    ),
                )

            val mineField = MineField(height, width, cells)

            Then("MineField 객체가 정상적으로 생성된다") {
                mineField.cells shouldHaveSize (height.value * width.value)
            }
        }

        When("셀 데이터가 높이 x 너비와 맞지 않으면") {
            val height = Height(3)
            val width = Width(3)
            val invalidCells =
                Cells(
                    listOf(
                        Cell.Mine(Coordinate(0, 0)),
                        Cell.Empty(Coordinate(1, 0)),
                    ).associateBy { it.coordinate },
                )

            val exception =
                shouldThrow<IllegalArgumentException> {
                    MineField(height, width, invalidCells)
                }

            Then("예외가 발생한다") {
                exception.message shouldBe "Cells의 개수가 보드 크기와 일치하지 않습니다."
            }
        }
    }

    Given("MineField 내부 셀 상태를 확인할 때") {
        val height = Height(2)
        val width = Width(2)
        val cells =
            Cells(
                mapOf(
                    Coordinate(0, 0) to Cell.Mine(Coordinate(0, 0)),
                    Coordinate(1, 0) to Cell.Empty(Coordinate(1, 0)),
                    Coordinate(0, 1) to Cell.Empty(Coordinate(0, 1)),
                    Coordinate(1, 1) to Cell.Mine(Coordinate(1, 1)),
                ),
            )
        val mineField = MineField(height, width, cells)

        When("Cells 객체를 통해 모든 셀을 가져오면") {
            Then("총 셀 개수는 높이 x 너비와 같아야 한다") {
                mineField.cells shouldHaveSize (height.value * width.value)
            }
        }

        When("Cells 객체에서 지뢰 셀을 필터링하면") {
            val mineCells = mineField.cells.filterIsInstance<Cell.Mine>()

            Then("지뢰 셀의 개수를 확인할 수 있다") {
                mineCells shouldHaveSize mineField.cells.count { it is Cell.Mine }
            }
        }
    }

    Given("MineField가 초기화된 상태에서") {
        val cells =
            Cells(
                listOf(
                    Cell.Mine(Coordinate(0, 0)),
                    Cell.Empty(Coordinate(1, 0)),
                    Cell.Empty(Coordinate(0, 1)),
                    Cell.Empty(Coordinate(1, 1)),
                ).associateBy { it.coordinate },
            )
        val mineField = MineField(Height(2), Width(2), cells)

        When("countAroundMines를 호출하면") {
            Then("주변 지뢰의 개수가 올바르게 계산되어야 한다") {
                mineField.countAroundMines(Coordinate(1, 1)) shouldBe 1
                mineField.countAroundMines(Coordinate(1, 0)) shouldBe 1
                mineField.countAroundMines(Coordinate(0, 0)) shouldBe 0
            }
        }
    }

    Given("지뢰 밭이 주졌을 때") {
        When("지뢰가 있는 셀을 열면") {
            val cells =
                Cells(
                    listOf(
                        Cell.Mine(Coordinate(0, 0)),
                        Cell.Empty(Coordinate(1, 0)),
                        Cell.Empty(Coordinate(2, 0)),
                        Cell.Empty(Coordinate(0, 1)),
                        Cell.Empty(Coordinate(1, 1)),
                        Cell.Empty(Coordinate(2, 1)),
                        Cell.Empty(Coordinate(0, 2)),
                        Cell.Empty(Coordinate(1, 2)),
                        Cell.Mine(Coordinate(2, 2)),
                    ).associateBy { it.coordinate },
                )
            val mineField = MineField(Height(3), Width(3), cells)
            mineField.open(Coordinate(0, 0))

            Then("지뢰가 있는 셀만 열리고 상태가 변경된다") {
                mineField.findCell(Coordinate(0, 0)).isRevealed shouldBe true
                mineField.findCell(Coordinate(1, 0)).isRevealed shouldBe false
                mineField.findCell(Coordinate(2, 0)).isRevealed shouldBe false
                mineField.findCell(Coordinate(0, 1)).isRevealed shouldBe false
                mineField.findCell(Coordinate(1, 1)).isRevealed shouldBe false
                mineField.findCell(Coordinate(2, 1)).isRevealed shouldBe false
                mineField.findCell(Coordinate(0, 2)).isRevealed shouldBe false
                mineField.findCell(Coordinate(1, 2)).isRevealed shouldBe false
                mineField.findCell(Coordinate(2, 2)).isRevealed shouldBe false
            }
        }

        When("셀을 열면") {
            val cells =
                Cells(
                    listOf(
                        Cell.Mine(Coordinate(0, 0)),
                        Cell.Empty(Coordinate(1, 0)),
                        Cell.Empty(Coordinate(2, 0)),
                        Cell.Empty(Coordinate(0, 1)),
                        Cell.Empty(Coordinate(1, 1)),
                        Cell.Empty(Coordinate(2, 1)),
                        Cell.Empty(Coordinate(0, 2)),
                        Cell.Empty(Coordinate(1, 2)),
                        Cell.Mine(Coordinate(2, 2)),
                    ).associateBy { it.coordinate },
                )
            val mineField = MineField(Height(3), Width(3), cells)
            mineField.open(Coordinate(2, 0))

            Then("해당 셀이 열리고 상태가 변경되어야 한다") {
                mineField.findCell(Coordinate(2, 0)).isRevealed shouldBe true
            }
            Then("지뢰가 없는 인접한 빈 셀들이 연속적으로 열린다") {
                mineField.findCell(Coordinate(0, 0)).isRevealed shouldBe false
                mineField.findCell(Coordinate(1, 0)).isRevealed shouldBe true
                mineField.findCell(Coordinate(2, 0)).isRevealed shouldBe true
                mineField.findCell(Coordinate(0, 1)).isRevealed shouldBe false
                mineField.findCell(Coordinate(1, 1)).isRevealed shouldBe true
                mineField.findCell(Coordinate(2, 1)).isRevealed shouldBe true
                mineField.findCell(Coordinate(0, 2)).isRevealed shouldBe false
                mineField.findCell(Coordinate(1, 2)).isRevealed shouldBe false
                mineField.findCell(Coordinate(2, 2)).isRevealed shouldBe false
            }
        }
    }

    Given("게임의 진행 상태를 결정해야 하는 경우") {
        When("지뢰가 포함된 셀을 열었을 때") {
            val cells =
                Cells(
                    mapOf(
                        Coordinate(0, 0) to Cell.Mine(Coordinate(0, 0)),
                        Coordinate(1, 0) to Cell.Empty(Coordinate(1, 0)),
                        Coordinate(0, 1) to Cell.Empty(Coordinate(0, 1)),
                        Coordinate(1, 1) to Cell.Empty(Coordinate(1, 1)),
                    ),
                )
            val mineField = MineField(Height(2), Width(2), cells)
            mineField.open(Coordinate(0, 0))
            val result = mineField.determineAction()

            Then("게임이 종료된다") {
                result shouldBe Action.GAME_OVER
            }
        }

        When("안전한 셀 중 일부만 열렸을 때") {
            val cells =
                Cells(
                    mapOf(
                        Coordinate(0, 0) to Cell.Mine(Coordinate(0, 0)),
                        Coordinate(1, 0) to Cell.Empty(Coordinate(1, 0)),
                        Coordinate(0, 1) to Cell.Empty(Coordinate(0, 1)),
                        Coordinate(1, 1) to Cell.Empty(Coordinate(1, 1)),
                    ),
                )
            val mineField = MineField(Height(2), Width(2), cells)
            mineField.open(Coordinate(0, 1))
            val result = mineField.determineAction()

            Then("게임이 계속 진행된다") {
                result shouldBe Action.CONTINUE
            }
        }

        When("모든 안전한 셀이 열렸을 때") {
            val cells =
                Cells(
                    mapOf(
                        Coordinate(0, 0) to Cell.Mine(Coordinate(0, 0)),
                        Coordinate(1, 0) to Cell.Empty(Coordinate(1, 0)),
                        Coordinate(0, 1) to Cell.Empty(Coordinate(0, 1)),
                        Coordinate(1, 1) to Cell.Empty(Coordinate(1, 1)),
                    ),
                )
            val mineField = MineField(Height(2), Width(2), cells)
            mineField.open(Coordinate(0, 1))
            mineField.open(Coordinate(1, 1))
            mineField.open(Coordinate(1, 0))

            val result = mineField.determineAction()

            Then("게임이 클리어된다") {
                result shouldBe Action.GAME_CLEARED
            }
        }
    }
})
