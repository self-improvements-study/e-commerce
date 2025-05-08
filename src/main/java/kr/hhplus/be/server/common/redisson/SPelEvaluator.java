package kr.hhplus.be.server.common.redisson;

import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Component
public class SPelEvaluator {

    public Object evaluateExpression(String first, Object second, String keyExpression) {

        SpelExpressionParser parser = new SpelExpressionParser();
        Expression expression = parser.parseExpression(keyExpression);

        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable(first, second);
        return expression.getValue(context);
    }
}
