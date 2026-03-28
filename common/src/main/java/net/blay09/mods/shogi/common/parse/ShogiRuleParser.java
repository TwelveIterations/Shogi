package net.blay09.mods.shogi.common.parse;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.blay09.mods.shogi.effect.ShogiEffect;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ShogiRuleParser {

    public static DataResult<ShogiEffect<?>> parse(ShogiScope scope, DynamicOps<JsonElement> ops, String input) {
        return parse(scope, ops, input, scope.getDefaultNamespaces());
    }

    public static DataResult<ShogiEffect<?>> parse(ShogiScope scope, DynamicOps<JsonElement> ops, String input, String defaultNamespace) {
        return parse(scope, ops, input, List.of(defaultNamespace));
    }

    public static DataResult<ShogiEffect<?>> parse(ShogiScope scope, DynamicOps<JsonElement> ops, String input, List<String> defaultNamespaces) {
        final JsonObject ruleJson;
        try {
            ruleJson = new Parser(scope, input, defaultNamespaces).parseRule();
        } catch (ParseException e) {
            return DataResult.error(() -> "Ruleset parse error at index " + e.position + ": " + e.getMessage());
        }

        return scope.getEffectCodec().parse(ops, ruleJson);
    }

    private static class Parser {
        private final ShogiScope scope;
        private final String input;
        private final List<String> defaultNamespaces;
        private int pos;

        private Parser(ShogiScope scope, String input, List<String> defaultNamespaces) {
            this.scope = scope;
            this.input = input;
            this.defaultNamespaces = defaultNamespaces;
        }

        private JsonObject parseRule() throws ParseException {
            skipWhitespace();
            JsonObject condition = null;
            if (hasTopLevelArrow()) {
                condition = parseConditionExpression();
                expectArrow();
            }

            final JsonObject action = parseAction();
            skipWhitespace();
            if (!isEof()) {
                throw error("Unexpected trailing token");
            }

            if (condition == null) {
                return action;
            }

            return conditionEffect(condition, action);
        }

        private JsonObject parseConditionExpression() throws ParseException {
            final List<JsonObject> anyConditions = new ArrayList<>();
            do {
                anyConditions.add(parseConditionAndGroup());
                skipWhitespace();
            } while (tryConsume(','));

            if (anyConditions.size() == 1) {
                return anyConditions.getFirst();
            }
            return anyEffect(anyConditions);
        }

        private JsonObject parseConditionAndGroup() throws ParseException {
            final List<JsonObject> andConditions = new ArrayList<>();
            do {
                andConditions.add(parseConditionPrimary());
                skipWhitespace();
            } while (tryConsume('+'));

            if (andConditions.size() == 1) {
                return andConditions.getFirst();
            }
            return andEffect(andConditions);
        }

        private JsonObject parseConditionPrimary() throws ParseException {
            skipWhitespace();
            if (tryConsume('!')) {
                return notEffect(parseConditionPrimary());
            }

            if (tryConsume('(')) {
                if (tryConsume(')')) {
                    throw error("Expected condition");
                }
                final JsonObject nested = parseConditionExpression();
                skipWhitespace();
                expect(')');
                return nested;
            }

            return parseConditionCall();
        }

        private JsonObject parseConditionCall() throws ParseException {
            skipWhitespace();
            final String identifier = parseCallIdentifier();
            skipWhitespace();
            if (tryConsume('(')) {
                return parseFunctionCallWithIdentifier(identifier).json();
            }
            if (isBoundaryForConditionBareCall(peek()) || isArrowAhead()) {
                return buildFunctionCall(identifier, List.of(), Map.of());
            }
            throw error("Expected function call");
        }

        private JsonObject parseAction() throws ParseException {
            skipWhitespace();
            final int start = pos;
            if (peek() == '$') {
                final String variable = parseVariablePath();
                skipWhitespace();
                if (tryConsume('=')) {
                    final Expr value = parseExpression();
                    return assignmentEffect(variable, effectFromExpr(value));
                }
                pos = start;
            }

            final Expr expression = parseExpression();
            return effectFromExpr(expression);
        }

        private Expr parseExpression() throws ParseException {
            Expr left = parseTerm();
            skipWhitespace();
            while (peek() == '+' || peek() == '-') {
                final String op = String.valueOf(input.charAt(pos++));
                final Expr right = parseTerm();
                left = new BinaryExpr(op, left, right);
                skipWhitespace();
            }
            return left;
        }

        private Expr parseTerm() throws ParseException {
            Expr left = parseFactor();
            skipWhitespace();
            while (peek() == '*' || peek() == '/') {
                final String op = String.valueOf(input.charAt(pos++));
                final Expr right = parseFactor();
                left = new BinaryExpr(op, left, right);
                skipWhitespace();
            }
            return left;
        }

        private Expr parseFactor() throws ParseException {
            skipWhitespace();
            if (tryConsume('!')) {
                return new UnaryExpr("!", parseFactor());
            }

            final char ch = peek();
            if (ch == '(') {
                pos++;
                final Expr nested = parseExpression();
                skipWhitespace();
                expect(')');
                return nested;
            }

            if (ch == '$') {
                return new VariableExpr(parseVariablePath());
            }

            if (ch == '\'' || ch == '"') {
                return new LiteralExpr(parseString());
            }

            if (isDigit(ch)) {
                return new LiteralExpr(parseNumber());
            }

            if (isIdentifierStart(ch)) {
                final int start = pos;
                final String literalCandidate = parseSimpleIdentifier();
                if ("true".equals(literalCandidate)) {
                    return new LiteralExpr(new JsonPrimitive(true));
                }
                if ("false".equals(literalCandidate)) {
                    return new LiteralExpr(new JsonPrimitive(false));
                }
                pos = start;
            }

            if (isIdentifierStart(ch)) {
                final String identifier = parseCallIdentifier();
                skipWhitespace();
                if (tryConsume('(')) {
                    return parseFunctionCallWithIdentifier(identifier);
                }

                if (isBoundaryForBareCall(peek())) {
                    return new FunctionCallExpr(buildFunctionCall(identifier, List.of(), Map.of()));
                }

                throw error("Expected '(' after function name");
            }

            throw error("Expected expression");
        }

        private JsonObject parseFunctionCall(boolean allowBare) throws ParseException {
            skipWhitespace();
            final String identifier = parseCallIdentifier();
            skipWhitespace();
            if (tryConsume('(')) {
                return parseFunctionCallWithIdentifier(identifier).json();
            }
            if (allowBare && (isBoundaryForBareCall(peek()) || isArrowAhead())) {
                return buildFunctionCall(identifier, List.of(), Map.of());
            }
            throw error("Expected function call");
        }

        private FunctionCallExpr parseFunctionCallWithIdentifier(String identifier) throws ParseException {
            final List<Expr> positional = new ArrayList<>();
            final Map<String, Expr> named = new LinkedHashMap<>();
            boolean hasNamed = false;
            boolean hasPositional = false;

            skipWhitespace();
            if (!tryConsume(')')) {
                while (true) {
                    skipWhitespace();
                    final NamedArgument namedArgument = tryParseNamedArgument();
                    if (namedArgument != null) {
                        if (hasPositional) {
                            throw error("Cannot mix named and positional arguments");
                        }
                        hasNamed = true;
                        if (named.put(namedArgument.name(), namedArgument.value()) != null) {
                            throw error("Duplicate named parameter: " + namedArgument.name());
                        }
                    } else {
                        if (hasNamed) {
                            throw error("Cannot mix named and positional arguments");
                        }
                        hasPositional = true;
                        positional.add(parseExpression());
                    }

                    skipWhitespace();
                    if (tryConsume(')')) {
                        break;
                    }
                    expect(',');
                }
            }

            return new FunctionCallExpr(buildFunctionCall(identifier, positional, named));
        }

        @Nullable
        private NamedArgument tryParseNamedArgument() throws ParseException {
            skipWhitespace();
            final int start = pos;
            if (!isIdentifierStart(peek())) {
                return null;
            }

            final String name = parseSimpleIdentifier();
            skipWhitespace();
            if (!tryConsume('=')) {
                pos = start;
                return null;
            }
            final Expr value = parseExpression();
            return new NamedArgument(name, value);
        }

        private JsonObject buildFunctionCall(String identifier, List<Expr> positional, Map<String, Expr> named) throws ParseException {
            final Identifier resolvedIdentifier = DefaultedIdentifiers.parse(identifier, defaultNamespaces, scope::hasEffect);
            if (resolvedIdentifier == null) {
                throw error("Invalid effect identifier: " + identifier);
            }
            final var canonicalIdentifier = scope.resolveEffectIdentifier(resolvedIdentifier)
                    .orElseThrow(() -> error("Unknown effect: " + resolvedIdentifier));

            final JsonObject json = new JsonObject();
            json.addProperty("type", canonicalIdentifier.toString());

            for (final var entry : named.entrySet()) {
                json.add(entry.getKey(), valueForArgument(entry.getValue()));
            }

            final List<String> ordinals = scope.getOrdinalParameters(canonicalIdentifier);
            if (!positional.isEmpty()) {
                if (ordinals.isEmpty()) {
                    throw error("Effect '" + resolvedIdentifier + "' does not support positional parameters");
                }

                if (ordinals.size() == 1 && positional.size() > 1 && supportsVariadicOrdinal(ordinals.getFirst())) {
                    if (json.has(ordinals.getFirst())) {
                        throw error("Named argument collides with positional parameter: " + ordinals.getFirst());
                    }

                    final JsonArray array = new JsonArray();
                    for (final Expr expr : positional) {
                        array.add(valueForArgument(expr));
                    }
                    json.add(ordinals.getFirst(), array);
                } else {
                    if (positional.size() > ordinals.size()) {
                        throw error("Too many positional arguments for effect '" + resolvedIdentifier + "'");
                    }

                    for (int i = 0; i < positional.size(); i++) {
                        final String parameter = ordinals.get(i);
                        if (json.has(parameter)) {
                            throw error("Named argument collides with positional parameter: " + parameter);
                        }
                        json.add(parameter, valueForArgument(positional.get(i)));
                    }
                }
            }

            return json;
        }

        private boolean supportsVariadicOrdinal(String ordinalName) {
            return "conditions".equals(ordinalName) || "effects".equals(ordinalName);
        }

        private JsonObject effectFromExpr(Expr expression) {
            return switch (expression) {
                case LiteralExpr(JsonElement value) -> constantEffect(value);
                case FunctionCallExpr(JsonObject json) -> json;
                case VariableExpr(String path) -> variableEffect(path);
                case UnaryExpr(String op, Expr expr) -> switch (op) {
                    case "!" -> notEffect(effectFromExpr(expr));
                    default -> throw new IllegalStateException("Unknown unary operator: " + op);
                };
                case BinaryExpr(String op, Expr left, Expr right) ->
                        binaryOpEffect(op, effectFromExpr(left), effectFromExpr(right));
                case null, default ->
                        throw new IllegalStateException("Unknown expression type: " + expression.getClass().getName());
            };
        }

        private JsonElement valueForArgument(Expr expression) {
            return switch (expression) {
                case LiteralExpr(JsonElement value) -> value;
                case FunctionCallExpr(JsonObject json) -> json;
                case VariableExpr(String path) -> variableEffect(path);
                case UnaryExpr(String op, Expr expr) -> switch (op) {
                    case "!" -> notEffect(effectFromExpr(expr));
                    default -> throw new IllegalStateException("Unknown unary operator: " + op);
                };
                case BinaryExpr(String op, Expr left, Expr right) ->
                        binaryOpEffect(op, effectFromExpr(left), effectFromExpr(right));
                case null, default ->
                        throw new IllegalStateException("Unknown expression type: " + expression.getClass().getName());
            };
        }

        private JsonObject constantEffect(JsonElement value) {
            final JsonObject json = new JsonObject();
            json.addProperty("type", "shogi:constant");
            json.add("value", value);
            return json;
        }

        private JsonObject variableEffect(String path) {
            final JsonObject json = new JsonObject();
            json.addProperty("type", "shogi:variable");
            json.addProperty("name", path);
            return json;
        }

        private JsonObject assignmentEffect(String variable, JsonObject value) {
            final JsonObject json = new JsonObject();
            json.addProperty("type", "shogi:assignment");
            json.addProperty("variable", variable);
            json.add("value", value);
            return json;
        }

        private JsonObject binaryOpEffect(String op, JsonObject left, JsonObject right) {
            final JsonObject json = new JsonObject();
            json.addProperty("type", "shogi:binary_op");
            json.addProperty("op", op);
            json.add("left", left);
            json.add("right", right);
            return json;
        }

        private JsonObject andEffect(List<JsonObject> conditions) {
            final JsonObject json = new JsonObject();
            json.addProperty("type", "shogi:and");
            final JsonArray array = new JsonArray();
            for (final JsonObject condition : conditions) {
                array.add(condition);
            }
            json.add("conditions", array);
            return json;
        }

        private JsonObject anyEffect(List<JsonObject> conditions) {
            final JsonObject json = new JsonObject();
            json.addProperty("type", "shogi:any");
            final JsonArray array = new JsonArray();
            for (final JsonObject condition : conditions) {
                array.add(condition);
            }
            json.add("conditions", array);
            return json;
        }

        private JsonObject notEffect(JsonObject condition) {
            final JsonObject json = new JsonObject();
            json.addProperty("type", "shogi:not");
            json.add("condition", condition);
            return json;
        }

        private JsonObject conditionEffect(JsonObject condition, JsonObject then) {
            final JsonObject json = new JsonObject();
            json.addProperty("type", "shogi:if");
            json.add("condition", condition);
            json.add("then", then);
            final JsonObject elseValue = new JsonObject();
            elseValue.addProperty("type", "shogi:noop");
            json.add("else", elseValue);
            return json;
        }

        private String parseVariablePath() throws ParseException {
            expect('$');
            final StringBuilder builder = new StringBuilder(parseSimpleIdentifier());
            while (tryConsume('.')) {
                builder.append('.').append(parseSimpleIdentifier());
            }
            return builder.toString();
        }

        private JsonElement parseNumber() {
            final int start = pos;
            while (isDigit(peek())) {
                pos++;
            }

            if (peek() == '.') {
                do {
                    pos++;
                } while (isDigit(peek()));
                return new JsonPrimitive(Double.parseDouble(input.substring(start, pos)));
            }

            return new JsonPrimitive(Long.parseLong(input.substring(start, pos)));
        }

        private JsonElement parseString() throws ParseException {
            final char quote = input.charAt(pos++);
            final StringBuilder builder = new StringBuilder();
            while (!isEof()) {
                final char ch = input.charAt(pos++);
                if (ch == quote) {
                    return new JsonPrimitive(builder.toString());
                }
                if (ch == '\\' && !isEof()) {
                    builder.append(input.charAt(pos++));
                } else {
                    builder.append(ch);
                }
            }
            throw error("Unterminated string literal");
        }

        private String parseSimpleIdentifier() throws ParseException {
            if (!isIdentifierStart(peek())) {
                throw error("Expected identifier");
            }

            final int start = pos++;
            while (isIdentifierPart(peek())) {
                pos++;
            }
            return input.substring(start, pos);
        }

        private String parseCallIdentifier() throws ParseException {
            if (!isIdentifierStart(peek())) {
                throw error("Expected function name");
            }
            final int start = pos++;
            while (isCallIdentifierPart(peek()) && !(peek() == '-' && peek(1) == '>')) {
                pos++;
            }
            final String identifier = input.substring(start, pos);
            if ("true".equals(identifier) || "false".equals(identifier)) {
                throw error("Boolean cannot be used as function name");
            }
            return identifier;
        }

        private boolean hasTopLevelArrow() {
            int depth = 0;
            boolean inQuote = false;
            char quoteChar = 0;
            for (int i = pos; i < input.length() - 1; i++) {
                final char ch = input.charAt(i);
                if (inQuote) {
                    if (ch == '\\') {
                        i++;
                        continue;
                    }
                    if (ch == quoteChar) {
                        inQuote = false;
                    }
                    continue;
                }

                if (ch == '\'' || ch == '"') {
                    inQuote = true;
                    quoteChar = ch;
                    continue;
                }
                if (ch == '(') {
                    depth++;
                    continue;
                }
                if (ch == ')') {
                    depth--;
                    continue;
                }
                if (depth == 0 && ch == '-' && input.charAt(i + 1) == '>') {
                    return true;
                }
            }
            return false;
        }

        private void expectArrow() throws ParseException {
            skipWhitespace();
            if (!(peek() == '-' && peek(1) == '>')) {
                throw error("Expected '->'");
            }
            pos += 2;
            skipWhitespace();
        }

        private void expect(char c) throws ParseException {
            if (!tryConsume(c)) {
                throw error("Expected '" + c + "'");
            }
        }

        private boolean tryConsume(char c) {
            skipWhitespace();
            if (peek() == c) {
                pos++;
                skipWhitespace();
                return true;
            }
            return false;
        }

        private void skipWhitespace() {
            while (!isEof() && Character.isWhitespace(input.charAt(pos))) {
                pos++;
            }
        }

        private ParseException error(String message) {
            return new ParseException(pos, message);
        }

        private char peek() {
            return peek(0);
        }

        private char peek(int offset) {
            final int index = pos + offset;
            return index >= input.length() ? '\0' : input.charAt(index);
        }

        private boolean isEof() {
            return pos >= input.length();
        }

        private boolean isDigit(char ch) {
            return ch >= '0' && ch <= '9';
        }

        private boolean isIdentifierStart(char ch) {
            return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_';
        }

        private boolean isIdentifierPart(char ch) {
            return isIdentifierStart(ch) || isDigit(ch);
        }

        private boolean isCallIdentifierPart(char ch) {
            return isIdentifierPart(ch) || ch == ':' || ch == '/' || ch == '.' || ch == '-';
        }

        private boolean isBoundaryForBareCall(char ch) {
            return ch == '\0' || ch == ')' || ch == ',' || Character.isWhitespace(ch);
        }

        private boolean isBoundaryForConditionBareCall(char ch) {
            return ch == '\0' || ch == ')' || ch == ',' || ch == '+' || Character.isWhitespace(ch);
        }

        private boolean isArrowAhead() {
            return peek() == '-' && peek(1) == '>';
        }
    }

    private interface Expr {
    }

    private record LiteralExpr(JsonElement value) implements Expr {
    }

    private record FunctionCallExpr(JsonObject json) implements Expr {
    }

    private record VariableExpr(String path) implements Expr {
    }

    private record UnaryExpr(String op, Expr expr) implements Expr {
    }

    private record BinaryExpr(String op, Expr left, Expr right) implements Expr {
    }

    private record NamedArgument(String name, Expr value) {
    }

    private static class ParseException extends Exception {
        private final int position;

        private ParseException(int position, String message) {
            super(message);
            this.position = position;
        }
    }
}
