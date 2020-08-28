/*******************************************************************************
 * Copyright 2013-2019 alladin-IT GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
// based on: https://raw.githubusercontent.com/alladin-IT/open-rmbt/master/RMBTControlServer/src/at/alladin/rmbt/qos/testscript/TestScriptInterpreter.java
package at.rtr.rmbt.shared.qos.testscript;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.*;

import at.rtr.rmbt.shared.Helperfunctions;
import at.rtr.rmbt.shared.qos.AbstractResult;
import at.rtr.rmbt.shared.qos.ResultOptions;
import org.json.JSONObject;


public class TestScriptInterpreter {
	/**
	 *
	 */
	public final static String COMMAND_RANDOM = "RANDOM";

	/**
	 *
	 */
	public final static String COMMAND_PARAM = "PARAM";

	/**
	 *
	 */
	public final static String COMMAND_RANDOM_URL = "RANDOMURL";

	/**
	 *
	 */
	public final static String COMMAND_EVAL = "EVAL";

	public final static class EvalResult {
		public static enum EvalResultType {
			FAILURE,
			SUCCESS,
			OTHER
		}

		private final EvalResultType type;
		private final String resultKey;

		public EvalResult(EvalResultType type) {
			this(type, null);
		}

		public EvalResult(EvalResultType type, String resultKey) {
			this.type = type;
			this.resultKey = resultKey;
		}

		public EvalResultType getType() {
			return type;
		}

		public String getResultKey() {
			return resultKey;
		}

		@Override
		public String toString() {
			return "EvalResult [type=" + type + ", resultKey=" + resultKey
					+ "]";
		}
	}

	public final static Pattern PATTERN_ARRAY = Pattern.compile("([^\\[]*)\\[([0-9]*)\\]");

	public final static Pattern PATTERN_CONTROL = Pattern.compile("%\\$([A-Z]*) (.*)%([\\s\\S.]*)%\\$(END\\1) \\2%");

	public final static Pattern PATTERN_CONTROL_SWITCH = Pattern.compile("%\\$(CASE|DEFAULT)(.*)%([\\s\\S.]*)%\\$END\\1\\2%");

	public final static Pattern PATTERN_COMMAND = Pattern.compile("%([A-Z]*)(.*)%");

	public final static Pattern PATTERN_RECURSIVE_COMMAND = Pattern.compile("([%%])(?:(?=(\\\\?))\\2.)*?\\1");

	private static ScriptEngine jsEngine;

	private static Method jsEngineNativeObjectGetter;

	private static boolean alredayLookedForGetter = false;

	/**
	 *
	 * @param command
	 * @return
	 */
	public static Object interpret(String command, ResultOptions resultOptions) {
		return interpret(command, null, null, false, resultOptions);
	}

	/**
	 *
	 * @param command
	 * @return
	 */
	public synchronized static Object interpret(String command, Map<String, Field> fieldNameToFieldMap, AbstractResult object, boolean useRecursion, ResultOptions resultOptions) {

		if (jsEngine == null) {
			ScriptEngineManager sem = new ScriptEngineManager();
			jsEngine = sem.getEngineByName("JavaScript");
			System.out.println("JS Engine: " + jsEngine.getClass().getCanonicalName());
			Bindings b = jsEngine.createBindings();
			b.put("nn", new SystemApi());
			jsEngine.setBindings(b, ScriptContext.GLOBAL_SCOPE);
		}

		command = command.replace("\\%", "{PERCENT}");

		try {
			final Matcher mc = PATTERN_CONTROL.matcher(command);
			while (mc.find()) {
				String toReplace = "";

				final String controlCommand = mc.group(1);
				//System.out.println("found control command: " + controlCommand + ", clause: " + mc.group(2));
				if ("IF".equals(controlCommand)) {
					if (controlIf(mc.group(2), object)) {
						toReplace = String.valueOf(interpret(mc.group(3), fieldNameToFieldMap, object, true, resultOptions));
					}
				}
				else if ("SWITCH".equals(controlCommand)) {
					toReplace = String.valueOf(interpret(controlSwitch(mc.group(2), object, mc.group(3)), fieldNameToFieldMap, object, true, resultOptions));
				}

				command = command.replace(mc.group(0), (toReplace != null ? toReplace.trim() : ""));
			}
		}
		catch (final ScriptException e) {
			e.printStackTrace();
			return null;
		}

		Pattern p;
		if (!useRecursion) {
			p = PATTERN_COMMAND;
		}
		else {
			p = PATTERN_RECURSIVE_COMMAND;

			Matcher m = p.matcher(command);
			while (m.find()) {
				String replace = m.group(0);
				//System.out.println("found: " + replace);
				String toReplace = String.valueOf(interpret(replace, fieldNameToFieldMap, object, false, resultOptions));
				//System.out.println("replacing: " + m.group(0) + " -> " + toReplace);
				command = command.replace(m.group(0), toReplace);
			}

			command = command.replace("{PERCENT}", "%");
			return command;
		}

		Matcher m = p.matcher(command);
		command = command.replace("{PERCENT}", "%");

		String scriptCommand;
		String[] args;

		if (m.find()) {
			if (m.groupCount() != 2) {
				return command;
			}
			scriptCommand = m.group(1);

			if (!COMMAND_EVAL.equals(scriptCommand)) {
				args = m.group(2).trim().split("\\s");
			}
			else {
				args = new String[] {m.group(2).trim()};
			}
		}
		else {
			return command;
		}

		try {
			if (COMMAND_RANDOM.equals(scriptCommand)) {
				return random(args);
			}
			else if (COMMAND_PARAM.equals(scriptCommand)) {
				return parse(args, fieldNameToFieldMap, object, resultOptions);
			}
			else if (COMMAND_EVAL.equals(scriptCommand)) {
				return eval(args, object);
			}
			else if (COMMAND_RANDOM_URL.equals(scriptCommand)) {
				return randomUrl(args);
			}
			else {
				return command;
			}
		}
		catch (ScriptException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 *
	 * @param args
	 * @return
	 * @throws ScriptException
	 */
	private static int random(String[] args) throws ScriptException {
		Random rand = new Random();
		if (args.length > 2 || args.length < 1) {
			throw new ScriptException(ScriptException.ERROR_INVALID_ARGUMENT_COUNT + " RANDOM: " + args.length);
		}

		try {
			switch (args.length) {
				case 1:
					int val = Integer.valueOf(args[0]) + 1;
					return rand.nextInt(val);

				case 2:
					int min = Integer.valueOf(args[0]);
					int max = Integer.valueOf(args[1]) + 1;
					return (rand.nextInt(max - min) + min);

				default:
					throw new ScriptException(ScriptException.ERROR_BAD_ARGUMENTS + " RANDOM: " + Helperfunctions.join(", ", args));
			}
		}
		catch (Exception e) {
			throw new ScriptException(ScriptException.ERROR_UNKNOWN + " RANDOM: " + e.getMessage());
		}
	}

	/**
	 *
	 * @param args
	 * @return
	 * @throws ScriptException
	 */
	private static String randomUrl(String[] args) throws ScriptException {
		if (args.length != 3) {
			throw new ScriptException(ScriptException.ERROR_INVALID_ARGUMENT_COUNT + " RANDOMURL: " + args.length);
		}

		try {
			return SystemApi.getRandomUrl(args[0], args[2], Integer.valueOf(args[1]));
		}
		catch (Exception e) {
			throw new ScriptException(ScriptException.ERROR_UNKNOWN + " RANDOMURL: " + e.getMessage());
		}
	}

	/**
	 *
	 * @param args
	 * @param object
	 * @return
	 * @throws ScriptException
	 */
	private static Object eval(String[] args, AbstractResult object) throws ScriptException {
		try {
			boolean isJsObject = false;

			final Bindings bindings = jsEngine.createBindings();
			bindings.putAll(object.getResultMap());
			//final Bindings bindings = new SimpleBindings(object.getResultMap());

			//System.out.println(object.getResultMap().toString());
			jsEngine.eval("var result=null; " + args[0], bindings);

			EvalResult evalResult = null;
			final Object result = bindings.get("result");

			if (result != null) {
				if (jsEngine.getClass().getCanonicalName().equals("jdk.nashorn.api.scripting.NashornScriptEngine")) {
					if (result.getClass().getCanonicalName().equals("jdk.nashorn.api.scripting.ScriptObjectMirror")) {
						isJsObject = true;
					}
				}
				else {
					if (result.getClass().getCanonicalName().equals("sun.org.mozilla.javascript.NativeObject")
							|| result.getClass().getCanonicalName().equals("sun.org.mozilla.javascript.internal.NativeObject")) {
						isJsObject = true;
					}
				}
			}

			if (isJsObject) {
				if (!alredayLookedForGetter && jsEngineNativeObjectGetter == null) {
					alredayLookedForGetter = true;
					System.out.println("js getter is null, trying to get methody with reflections...");
					try {
						jsEngineNativeObjectGetter = result.getClass().getMethod("get", Object.class);
						System.out.println("method found: " + jsEngineNativeObjectGetter.getName());
					}
					catch (Exception e) {
						System.out.println("method not found: " + e.getMessage());
					}
				}

				if (jsEngineNativeObjectGetter != null) {
					final String type = (String) jsEngineNativeObjectGetter.invoke(result, "type");
					final String key = (String) jsEngineNativeObjectGetter.invoke(result, "key");

					//System.out.println(type + " " + key);

					evalResult = new EvalResult(EvalResult.EvalResultType.valueOf(type.toUpperCase(Locale.US)), key);

					//System.out.println("Result: " + evalResult);
				}
			}

			return evalResult == null ? (result == null ? "" : result) : evalResult;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ScriptException(e.getMessage() + " " + args[0]);
		}
	}

	/**
	 *
	 * @param args
	 * @return
	 * @throws ScriptException
	 */
	private static Object parse(String[] args, Map<String, Field> fieldNameToFieldMap, Object object, ResultOptions options) throws ScriptException {

		if (object == null) {
			throw new ScriptException(ScriptException.ERROR_RESULT_IS_NULL + " PARSE");
		}

		if (args.length < 1) {
			throw new ScriptException(ScriptException.ERROR_INVALID_ARGUMENT_COUNT + " PARSE: " + args.length);
		}
		if (fieldNameToFieldMap == null) {
			throw new ScriptException(ScriptException.ERROR_PARSER_IS_NULL + " PARSE");
		}

		try {
			Pattern p = PATTERN_ARRAY;
			Matcher m = p.matcher(args[0]);

			if (m.find()) {
				String param = m.group(1);
				int index = Integer.valueOf(m.group(2));
				Object array = getFieldValue(fieldNameToFieldMap, param, object);

				Object indexedObject = null;
				if (array != null) {
					if (List.class.isAssignableFrom(array.getClass())) {
						indexedObject = ((List<?>) array).get(index);
					}
					else if (Collection.class.isAssignableFrom(array.getClass())) {
						Iterator<?> iterator = ((Collection<?>) array).iterator();
						int counter = 0;
						while (iterator.hasNext()) {
							Object o = iterator.next();
							if ((counter++) == index) {
								indexedObject = o;
								break;
							}
						}
					}

					if (args.length > 1) {
						String[] nextArgs = new String[args.length - 1];
						nextArgs = Arrays.copyOfRange(args, 1, args.length);
						return parse(nextArgs, fieldNameToFieldMap, indexedObject, options);
					}
					else {
						return indexedObject;
					}
				}
			}
			else {
				Object value = getFieldValue(fieldNameToFieldMap, args[0], object);
				if (args.length > 1) {
					try {
						long divisor = Long.parseLong(args[1]);
						int precision = 2;
						boolean groupingUsed = false;
						if (args.length > 2) {
							precision = Integer.parseInt(args[2]);
						}
						if (args.length > 3) {
							groupingUsed = ("t".equals(args[3].toLowerCase()) || "true".equals(args[3].toLowerCase()));
						}
						NumberFormat format = (options != null ? DecimalFormat.getInstance(options.getLocale()) : DecimalFormat.getInstance());
						format.setMaximumFractionDigits(precision);
						format.setGroupingUsed(groupingUsed);
						format.setRoundingMode(RoundingMode.HALF_UP);
						//System.out.println("Converting number: " + args[0] + "=" + String.valueOf(value));
						BigDecimal number = new BigDecimal(String.valueOf(value));
						return format.format(number.divide(new BigDecimal(divisor)));
					}
					catch (Exception e) {
						//can not return parsed element
					}
				}
				//System.out.println("PARAM object: " + args[0] + " -> " + value + " of " + object.toString());
				return value;
			}
		} catch (Throwable t) {
			throw new ScriptException(ScriptException.ERROR_UNKNOWN + " PARSE: " + t.getMessage());
		}

		return null;
	}

	public static boolean controlIf(String clause, AbstractResult object) throws ScriptException {
		final Bindings bindings = new SimpleBindings(object.getResultMap());
		try {
			final Object result = jsEngine.eval(clause, bindings);
			return Boolean.parseBoolean(result.toString());

		} catch (javax.script.ScriptException e) {
			throw new ScriptException(ScriptException.ERROR_UNKNOWN + " IF: " + e.getMessage());
		}
	}

	public static String controlSwitch(String clause, AbstractResult object, String switchBody) throws ScriptException {
		final Bindings bindings = new SimpleBindings(object.getResultMap());
		Object result = null;
		try {
			result = jsEngine.eval(clause, bindings);
		} catch (javax.script.ScriptException e) {
			throw new ScriptException(ScriptException.ERROR_UNKNOWN + " SWITCH: " + e.getMessage());
		}

		try {
			if (result != null) {
				final String switchCase = result.toString();
				final Matcher m = PATTERN_CONTROL_SWITCH.matcher(switchBody);
				while (m.find()) {
					final String caseOption = jsEngine.eval(m.group(2), bindings).toString();
					if ((m.group(1).equals("CASE") && switchCase.equals(caseOption)) || m.group(1).equals("DEFAULT")) {
						return m.group(3);
					}
				}
			}
		} catch (javax.script.ScriptException e) {
			throw new ScriptException(ScriptException.ERROR_UNKNOWN + " CASE: " + e.getMessage());
		}

		return "";
	}

	public static Map<String, Object> jsonToMap(final JSONObject json) {
		final Iterator<String> jsonKeys = json.keys();
		final Map<String, Object> map = new HashMap<>();

		while (jsonKeys.hasNext()) {
			final String jsonKey = jsonKeys.next();
			map.put(jsonKey, json.opt(jsonKey));
		}

		return map;
	}

	private static Object getFieldValue(final Map<String, Field> fieldNameToFieldMap, final String param, final Object object) throws ScriptException {
		Field field = fieldNameToFieldMap.get(param);
		if (field != null) {
			try {
				field.setAccessible(true);
				return field.get(object);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new ScriptException("Could not get field: " + field.getClass().getCanonicalName() + "." + field.getName() + "\n", e);
			}
		}

		return null;
	}
}
