/*******************************************************************************
 * Copyright 2019 alladin-IT GmbH
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

package at.rtr.rmbt.qos.testserver.mock.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.net.InetAddresses;

import at.rtr.rmbt.qos.testserver.mock.DataInputStreamMockup;
import mockit.Delegate;
import mockit.Expectations;

/**
 * 
 * @author Lukasz Budryk (lb@alladin.at)
 *
 */
public class SocketCommunicationExpectationsUtil {
	
	public static Expectations createExpectationWithOutputStream(final Socket socket, 
			final OutputStream serverResponseOutputStream,
			final String message) throws IOException {
		
		return new Expectations() {
			{
				socket.getInetAddress(); 
				result = InetAddresses.forString("1.1.1.1");
				
				socket.getInputStream(); 
				result = new ByteArrayInputStream(message.getBytes());

				socket.getOutputStream();
				result = serverResponseOutputStream;
			}
		};
	}

	public static Expectations createExpectationWithMultipleOutputStreams(final Socket socket, 
			final OutputStream[] serverResponseOutputStream,
			final String message) throws IOException {
		
		return new Expectations() {
			{
				socket.getInetAddress();
				result = InetAddresses.forString("1.1.1.1");
				
				socket.getInputStream();
				result = new ByteArrayInputStream(message.getBytes());

				socket.getOutputStream();
				result = serverResponseOutputStream;
			}
		};
	}

	public static Expectations createExpectationWithMultipleResultStringsUsingDataInputStream(final Socket socket, 
			final FilterOutputStream fos,
			//final InputStream dis,
			final AtomicReference<String[]> results,
			final String...messages) throws IOException {
		
		final AtomicInteger index = new AtomicInteger(0);
		final AtomicInteger indexAvailable = new AtomicInteger(0);
		final DataInputStreamMockup dis = new DataInputStreamMockup(messages);
		
		return new Expectations() {
			{
				socket.getInetAddress();
				result = InetAddresses.forString("1.1.1.1");
				
				socket.getInputStream();
				result = new ByteArrayInputStream(new String("").getBytes());
				
				fos.write((byte[]) any);
				result = new Delegate() {
					public void delegate(byte b[]) throws IOException {
						results.get()[index.getAndAdd(1)] = new String(b);
					}
				};
			}
		};
	}

	public static Expectations createExpectationWithMultipleResultStrings(final Socket socket, 
			final FilterOutputStream fos,
			//final InputStream dis,
			final AtomicReference<String[]> results,
			final String...messages) throws IOException {
		
		final AtomicInteger index = new AtomicInteger(0);
		final AtomicInteger indexAvailable = new AtomicInteger(0);
		
		final StringBuilder sb = new StringBuilder();
		for (String msg : messages) {
			sb.append(msg);
		}
		
		return new Expectations() {
			{
				socket.getInetAddress();
				result = InetAddresses.forString("1.1.1.1");
				
				socket.getInputStream();
				result = new ByteArrayInputStream(sb.toString().getBytes());
				
				fos.write((byte[]) any);
				result = new Delegate() {
					public void delegate(byte b[]) throws IOException {
						results.get()[index.getAndAdd(1)] = new String(b);
					}
				};
			}
		};
	}

	public static byte[] concatStringsToByes(final byte[] delimiter, final String[] messages) {		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for (int i = 0; i < messages.length; i++) {
			try {
				baos.write(messages[i].getBytes());
				if (i+1 < messages.length) {
					baos.write(delimiter);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}		
		return baos.toByteArray();
	}

	public static Expectations createExpectationWithThrownException(final Socket socket, 
			final Exception exceptionToThrow,
			final String message) throws IOException {
		
		return new Expectations() {
			{
				socket.getInetAddress(); 
				result = InetAddresses.forString("1.1.1.1");
				
				socket.getInputStream(); 
				result = new ByteArrayInputStream(message.getBytes());

				socket.getOutputStream();
				result = exceptionToThrow;
			}
		};
	}

	public static Expectations createExpectationWithThrownExceptionOnClose(final Socket socket, 
			final Exception exceptionToThrow) throws IOException {
		
		return new Expectations() {
			{
				socket.close();
				result = new IOException();
				result = null;
			}
		};
	}

}
