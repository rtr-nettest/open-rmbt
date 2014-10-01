/*******************************************************************************
 * Copyright 2013-2014 alladin-IT GmbH
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
package at.alladin.rmbt.qos.testscript;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

import at.alladin.rmbt.qos.AbstractResult;
import at.alladin.rmbt.qos.ResultOptions;
import at.alladin.rmbt.shared.Helperfunctions;
import at.alladin.rmbt.shared.hstoreparser.Hstore;
import at.alladin.rmbt.shared.hstoreparser.HstoreParseException;
import at.alladin.rmbt.shared.hstoreparser.HstoreParser;

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
	
	public final static Pattern PATTERN_ARRAY = Pattern.compile("([^\\[]*)\\[([0-9]*)\\]");
	
	public final static Pattern PATTERN_COMMAND = Pattern.compile("%([A-Z]*)(.*)%");
	
	public final static Pattern PATTERN_RECURSIVE_COMMAND = Pattern.compile("([%%])(?:(?=(\\\\?))\\2.)*?\\1");
	
	private static ScriptEngine jsEngine;
	
	/**
	 * 
	 * @param command
	 * @return
	 */
	public static Object interprete(String command, ResultOptions resultOptions) {
		return interprete(command, null, null, false, resultOptions);
	}
		
	/**
	 * 
	 * @param command
	 * @return
	 */
	public static <T> Object interprete(String command, Hstore hstore, AbstractResult<T> object, boolean useRecursion, ResultOptions resultOptions) {

		if (jsEngine == null) {
			ScriptEngineManager sem = new ScriptEngineManager();
			jsEngine = sem.getEngineByName("JavaScript");
			
			Bindings b = jsEngine.createBindings();
			b.put("nn", new SystemApi());
			jsEngine.setBindings(b, ScriptContext.GLOBAL_SCOPE);
		}
		
		command = command.replace("\\%", "{PERCENT}");
		
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
				String toReplace = String.valueOf(interprete(replace, hstore, object, false, resultOptions));
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
				return parse(args, hstore, object, resultOptions);
			}
			else if (COMMAND_EVAL.equals(scriptCommand)) {
				return eval(args, hstore, object);
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
	 * @param hstore
	 * @param object
	 * @return
	 * @throws ScriptException
	 */
	private static Object eval(String[] args, Hstore hstore, Object object) throws ScriptException {
		try {
			HstoreParser<?> parser = hstore.getParser(object.getClass());
			Bindings bindings = new SimpleBindings(parser.getValueMap(object));
			jsEngine.eval(args[0], bindings);
			Object result = bindings.get("result");
			return result == null ? "" : result;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ScriptException(e.getMessage());
		}
	}
	
	/**
	 * 
	 * @param args
	 * @return
	 * @throws ScriptException
	 */
	private static Object parse(String[] args, Hstore hstore, Object object, ResultOptions options) throws ScriptException {

		if (object == null) {
			throw new ScriptException(ScriptException.ERROR_RESULT_IS_NULL + " PARSE");
		}

		HstoreParser<?> parser = hstore.getParser(object.getClass());
		
		if (args.length < 1) {
			throw new ScriptException(ScriptException.ERROR_INVALID_ARGUMENT_COUNT + " PARSE: " + args.length);
		}
		if (parser == null) {
			throw new ScriptException(ScriptException.ERROR_PARSER_IS_NULL + " PARSE");
		}

		try {
			Pattern p = PATTERN_ARRAY;
			Matcher m = p.matcher(args[0]);
			
			if (m.find()) {
				String param = m.group(1);
				int index = Integer.valueOf(m.group(2));
				Object array = parser.getValue(param, object);
				
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
						return parse(nextArgs, hstore, indexedObject, options);
					}
					else {
						return indexedObject;
					}					
				}
			}
			else {
				Object value = parser.getValue(args[0], object);
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
						BigDecimal number = new BigDecimal(String.valueOf(value));
						return format.format(number.divide(new BigDecimal(divisor)));
					}
					catch (Exception e) {
						//can not return parsed element
						e.printStackTrace();
					}
				}
				//System.out.println("PARAM object: " + args[0] + " -> " + value + " of " + object.toString());
				return value;				
			}
		} catch (HstoreParseException e) {
			throw new ScriptException(ScriptException.ERROR_UNKNOWN + " PARSE: " + e.getMessage());
		} catch (Throwable t) {
			throw new ScriptException(ScriptException.ERROR_UNKNOWN + " PARSE: " + t.getMessage());
		}
		
		return null;
	}
}
