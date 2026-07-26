package com.hd.calculator.app.ui;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hd.calculator.app.R;
import com.hd.calculator.app.base.BaseActivity;
import com.hd.calculator.app.business.AccountManager;
import com.hd.calculator.app.databinding.ActivityCalculatorBinding;
import com.hd.calculator.app.function.db.DataBaseRepository;
import com.hd.calculator.app.util.LogUtils;
import com.hd.calculator.app.util.ThreadUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 计算器&启动页
 */
public class CalculatorActivity extends BaseActivity<ActivityCalculatorBinding> {


    private final List<String> mExpressionParts = new ArrayList<>();
    private final int[] buttonIds = {R.id.zero, R.id.one, R.id.two, R.id.three, R.id.four, R.id.five, R.id.six, R.id.seven, R.id.eight, R.id.night, R.id.plus, R.id.sub, R.id.multiply, R.id.divide, R.id.mob, R.id.dot, R.id.equal, R.id.ac};
    private TextView mDisplay;
    private double mResult = Double.NaN;
    private String pendingOperation = "";
    private boolean mIsNewExpression = true;
    private final View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();

            if (id == R.id.ac) {
                clearAll();
            } else if (id == R.id.del) {
                clearLastChar();
            } else if (id == R.id.equal) {
                handleEquals();
            } else if (id == R.id.plus) {
                handleOperator("+");
            } else if (id == R.id.sub) {
                handleOperator("-");
            } else if (id == R.id.multiply) {
                handleOperator("×");
            } else if (id == R.id.divide) {
                handleOperator("÷");
            } else if (id == R.id.mob) {
                handleOperator("%");
            } else if (id == R.id.dot) {
                addDecimalPoint();
            } else {
                if (view instanceof TextView) {
                    TextView button = (TextView) view;
                    appendNumber(button.getText().toString());
                }
            }
        }
    };


    @Override
    protected ActivityCalculatorBinding getViewBinding() {
        return ActivityCalculatorBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {

        mDisplay = findViewById(R.id.panel);

        // 设置所有按钮的点击监听器
        for (int id : buttonIds) {
            View button = findViewById(id);
            button.setOnClickListener(buttonClickListener);
        }

        // 为ImageView单独设置点击监听器
        ImageView buttonC = findViewById(R.id.del);
        buttonC.setOnClickListener(buttonClickListener);
        buttonC.setClickable(true);
        buttonC.setFocusable(true);

    }

    @Override
    protected void initData() {

    }

    private void handleEquals() {
        ThreadUtils.runOnIoThreadDelayed(() -> {
            // 构建表达式字符串
            StringBuilder expressionBuilder = new StringBuilder();
            for (String part : mExpressionParts) {
                expressionBuilder.append(part);
            }
            String result = expressionBuilder.toString();
            if (result.isEmpty()) {
                runOnUiThread(this::calculateResult);
            } else {
                if (result.startsWith("%")) {
                    String pwd = result.substring(1);
                    LogUtils.log("pwd:" + pwd);
                    AccountManager.getIns().updateAccount(DataBaseRepository.getInstance().getByPassword(pwd));
                    if (AccountManager.getIns().getAccount() == null) {
                        runOnUiThread(this::calculateResult);
                    } else {
                        SelectUserLoginActivity.start(CalculatorActivity.this, false);
                        finish();
//                        startActivity(new Intent(CalculatorActivity.this, MainActivity.class));
//                        finish();
                    }
                } else {
                    runOnUiThread(this::calculateResult);
                }
            }
        });

    }

    private void appendNumber(String number) {
        if (mIsNewExpression) {
            mExpressionParts.clear();
            mIsNewExpression = false;
        }

        if (mExpressionParts.isEmpty() || !isNumberPart(mExpressionParts.get(mExpressionParts.size() - 1))) {
            mExpressionParts.add(number);
        } else {
            String current = mExpressionParts.get(mExpressionParts.size() - 1);
            // 避免多个前导零
            if (!"0".equals(current) || !"0".equals(number)) {
                mExpressionParts.set(mExpressionParts.size() - 1, current + number);
            }
        }
        updateDisplay();
    }

    private void addDecimalPoint() {
        if (mIsNewExpression) {
            mExpressionParts.clear();
            mExpressionParts.add("0.");
            mIsNewExpression = false;
            updateDisplay();
            return;
        }

        if (mExpressionParts.isEmpty()) {
            mExpressionParts.add("0.");
        } else {
            String lastPart = mExpressionParts.get(mExpressionParts.size() - 1);
            if (isNumberPart(lastPart) && !lastPart.contains(".")) {
                mExpressionParts.set(mExpressionParts.size() - 1, lastPart + ".");
            } else if (!isNumberPart(lastPart)) {
                mExpressionParts.add("0.");
            }
        }
        updateDisplay();
    }

    private void handleOperator(String operator) {
        if (mExpressionParts.isEmpty()) {
            if (!Double.isNaN(mResult)) {
                mExpressionParts.add(formatResult(mResult));
            } else {
//                mExpressionParts.add("0");
            }
        }

        // 如果最后一部分是操作符，替换它
        if (!mExpressionParts.isEmpty() && !isNumberPart(mExpressionParts.get(mExpressionParts.size() - 1))) {
            mExpressionParts.set(mExpressionParts.size() - 1, operator);
        } else {
            mExpressionParts.add(operator);
        }

        pendingOperation = operator;
        mIsNewExpression = false;
        updateDisplay();
    }

    private void calculateResult() {
        if (mExpressionParts.isEmpty()) return;

        // 确保表达式以数字结尾
        if (!isNumberPart(mExpressionParts.get(mExpressionParts.size() - 1))) {
            mExpressionParts.remove(mExpressionParts.size() - 1);
        }

        try {
            mResult = evaluateExpression();

            // 构建表达式字符串
            StringBuilder expressionBuilder = new StringBuilder();
            for (String part : mExpressionParts) {
                expressionBuilder.append(part).append(" ");
            }
            String expressionStr = expressionBuilder.toString().trim();

            // 格式化结果
            String resultStr = formatResult(mResult);

            // 显示表达式，等号和结果另起一行，并在它们之间加空格
            mDisplay.setText(expressionStr + "\n= " + resultStr);

            // 重置状态：expressionParts只保留结果字符串
            mExpressionParts.clear();
            mExpressionParts.add(resultStr);
            pendingOperation = "";
            mIsNewExpression = true;
        } catch (NumberFormatException | ArithmeticException e) {
            mDisplay.setText("Error");
            clearAll();
        }
    }

    private double evaluateExpression() {
        double value = Double.parseDouble(mExpressionParts.get(0));

        // 从左到右计算表达式（不考虑操作符优先级）
        for (int i = 1; i < mExpressionParts.size(); i += 2) {
            String operator = mExpressionParts.get(i);
            if (i + 1 < mExpressionParts.size()) {
                double nextValue = Double.parseDouble(mExpressionParts.get(i + 1));
                switch (operator) {
                    case "+":
                        value += nextValue;
                        break;
                    case "-":
                        value -= nextValue;
                        break;
                    case "×":
                        value *= nextValue;
                        break;
                    case "÷":
                        if (nextValue == 0) {
                            throw new ArithmeticException("Division by zero");
                        }
                        value /= nextValue;
                        break;
                    case "%":
                        value %= nextValue;
                        break;
                }
            }
        }
        return value;
    }

    private void updateDisplay() {
        if (mExpressionParts.isEmpty()) {
            mDisplay.setText("0");
            return;
        }

        StringBuilder displayText = new StringBuilder();
        for (String part : mExpressionParts) {
            displayText.append(part).append(" ");
        }

        // 移除最后一个空格
        if (displayText.length() > 0) {
            displayText.setLength(displayText.length() - 1);
        }

        mDisplay.setText(displayText.toString());
    }

    private String formatResult(double value) {
        if (Double.isNaN(value)) {
            return "Error";
        }

        if (value == (int) value) {
            return String.valueOf((int) value);
        } else {
            String result = String.format("%.10f", value);
            // 去除尾部多余的零和小数点
            return result.replaceAll("0*$", "").replaceAll("\\.$", "");
        }
    }

    private boolean isNumberPart(String str) {
        return str.matches("-?\\d+(\\.\\d*)?");
    }

    private void clearLastChar() {
        if (mExpressionParts.isEmpty()) return;

        String lastPart = mExpressionParts.get(mExpressionParts.size() - 1);
        if (lastPart.length() > 1) {
            // 缩短最后一个部分
            mExpressionParts.set(mExpressionParts.size() - 1, lastPart.substring(0, lastPart.length() - 1));
        } else {
            // 删除最后一个部分
            mExpressionParts.remove(mExpressionParts.size() - 1);
        }

        // 如果删除数字后只剩下操作符，也删除操作符
        if (!mExpressionParts.isEmpty() && !isNumberPart(mExpressionParts.get(mExpressionParts.size() - 1))) {
            mExpressionParts.remove(mExpressionParts.size() - 1);
        }

        updateDisplay();
    }

    private void clearAll() {
        mExpressionParts.clear();
        mResult = Double.NaN;
        pendingOperation = "";
        mIsNewExpression = true;
        mDisplay.setText("0");
    }
}
