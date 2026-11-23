package com.exercise.equation;

import com.exercise.expression.BinaryExpression;
import com.exercise.expression.ConstantExpression;
import com.exercise.expression.Expression;
import com.exercise.operation.Operation;

import java.util.List;
import java.util.Objects;

/**
 * 三个数参与的混合运算算式。
 *
 * <p>三个数字与两个运算符按"先乘除、后加减"的数学规则组织成一棵表达式树：</p>
 * <ul>
 *   <li>若第一个运算符优先级不低于第二个（例如 {@code 2 * 3 + 4}），
 *       树形为 {@code (a op1 b) op2 c}；</li>
 *   <li>若第一个运算符优先级低于第二个（例如 {@code 2 + 3 * 4}），
 *       树形为 {@code a op1 (b op2 c)}。</li>
 * </ul>
 *
 * <p>因此显示文本严格保持书写顺序 {@code a op1 b op2 c}，而求值顺序由树结构保证
 * 先乘除后加减；所有中间结果都会进入求值轨迹并接受 (0, 100) 校验。</p>
 */
public final class ThreeOperandEquation extends Equation {

    private final int first;
    private final int second;
    private final int third;
    private final Operation firstOperation;
    private final Operation secondOperation;

    /**
     * 构造三数混合算式。
     *
     * @param first           第一个数，必须为 1~99
     * @param second          第二个数，必须为 1~99
     * @param third           第三个数，必须为 1~99
     * @param firstOperation  第一个运算符，不能为空
     * @param secondOperation 第二个运算符，不能为空
     * @throws IllegalArgumentException 操作数越界、运算符为空、除法不能整除
     *                                  或出现越界的中间结果/最终结果时抛出
     */
    public ThreeOperandEquation(int first, int second, int third,
                                Operation firstOperation, Operation secondOperation) {
        super(buildExpression(first, second, third, firstOperation, secondOperation));
        this.first = first;
        this.second = second;
        this.third = third;
        this.firstOperation = firstOperation;
        this.secondOperation = secondOperation;
    }

    /**
     * 按数学优先级把三个数组织成表达式树。
     *
     * @param first           第一个数
     * @param second          第二个数
     * @param third           第三个数
     * @param firstOperation  第一个运算符
     * @param secondOperation 第二个运算符
     * @return 表示该算式的表达式树
     */
    static Expression buildExpression(int first, int second, int third,
                                      Operation firstOperation, Operation secondOperation) {
        requireOperandInRange(first, "第一个操作数");
        requireOperandInRange(second, "第二个操作数");
        requireOperandInRange(third, "第三个操作数");
        Objects.requireNonNull(firstOperation, "第一个运算符不能为空");
        Objects.requireNonNull(secondOperation, "第二个运算符不能为空");

        Expression firstExpression = new ConstantExpression(first);
        Expression secondExpression = new ConstantExpression(second);
        Expression thirdExpression = new ConstantExpression(third);

        if (firstOperation.precedence() >= secondOperation.precedence()) {
            // 先算前两个数：(a op1 b) op2 c
            return new BinaryExpression(secondOperation,
                    new BinaryExpression(firstOperation, firstExpression, secondExpression),
                    thirdExpression);
        }
        // 先算后两个数：a op1 (b op2 c)
        return new BinaryExpression(firstOperation,
                firstExpression,
                new BinaryExpression(secondOperation, secondExpression, thirdExpression));
    }

    /**
     * 返回第一个数。
     *
     * @return 第一个数
     */
    public int getFirst() {
        return first;
    }

    /**
     * 返回第二个数。
     *
     * @return 第二个数
     */
    public int getSecond() {
        return second;
    }

    /**
     * 返回第三个数。
     *
     * @return 第三个数
     */
    public int getThird() {
        return third;
    }

    /**
     * 返回第一个运算符。
     *
     * @return 第一个运算符
     */
    public Operation getFirstOperation() {
        return firstOperation;
    }

    /**
     * 返回第二个运算符。
     *
     * @return 第二个运算符
     */
    public Operation getSecondOperation() {
        return secondOperation;
    }

    @Override
    public List<Operation> getOperations() {
        return List.of(firstOperation, secondOperation);
    }
}
