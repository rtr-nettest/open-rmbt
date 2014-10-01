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
package at.alladin.rmbt.android.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.Date;

/**
 * 
 * @author lb
 *
 */
public class DebugPrintStream extends PrintStream {

	public DebugPrintStream(File file) throws FileNotFoundException {
		super(file);
	}

	@Override
	public void println(Object o) {
		showLocation(false, (String) o.toString());
	    //super.println(o);
	}

	 
	@Override
	public void println(String str) {
		showLocation(false, str);
	    //super.println(x);
	}
	
	@Override
	public void print(Object o) {
		showLocation(false, o.toString());
		//super.print(o);
	}
	 
	@Override
	public synchronized void print(String str) {
		showLocation(false, str);
		//super.print(str);
	}
	
	private void showLocation(boolean fullDetail, String str) {
		System.out.print(str);
		if (str.trim().length() > 0) {
			String message = "";
			StackTraceElement[] element = Thread.currentThread().getStackTrace();
			if (fullDetail) {			
				message = MessageFormat.format("({2} - {0}:{1, number,#}) : ", element[3].getFileName(), element[3].getLineNumber(), new Date());
			}
			else {
				message = MessageFormat.format("({0}) : ", new Date());
			}
			
			if ("at".equals(str.trim())) {
				super.print("\t at");	
			}
			else {
				super.print(message + str + "\n");
			}
			
		}
	}
}
