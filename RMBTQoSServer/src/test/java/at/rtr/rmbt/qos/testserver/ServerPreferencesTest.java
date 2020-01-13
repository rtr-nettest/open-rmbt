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

package at.rtr.rmbt.qos.testserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.util.Iterator;

import org.junit.Test;

import at.rtr.rmbt.qos.testserver.ServerPreferences.UdpPort;

/**
 * 
 * @author Lukasz Budryk (lb@alladin.at)
 *
 */
public class ServerPreferencesTest {

	public void assertDefaultSeverPreferences(final ServerPreferences sp) {
		assertNotNull("ServerPreferences != null", sp);
		assertEquals("Amount of interfaces to bind to = 1", 1, sp.getInetAddrBindToSet().size());
		final Iterator<InetAddress> ifSet = sp.getInetAddrBindToSet().iterator();
		assertEquals("Interface address [0] to bind to = 127.0.0.1", "127.0.0.1", ifSet.next().getHostAddress());
		assertEquals("Server port = 5233", 5233, sp.getServerPort());
		assertTrue("SSL/TLS flag is set to true", sp.useSsl());
		assertNotNull(sp.toString());		
	}
	
	@Test
	public void loadPreferencesFromDefaultResourcesPath() throws TestServerException {
		ServerPreferences sp = new ServerPreferences();
		assertDefaultSeverPreferences(sp);
	}

	@Test
	public void loadPreferencesFromDefaultResourcesPathProvidingNullArgumentsArray() throws TestServerException {
		ServerPreferences sp = new ServerPreferences((String[]) null);
		assertDefaultSeverPreferences(sp);
	}

	@Test
	public void loadPreferencesFromInputStream() throws TestServerException {
		ServerPreferences sp = new ServerPreferences(getClass().getResourceAsStream("config.properties"));
		assertNotNull("ServerPreferences != null", sp);
		assertEquals("Amount of interfaces to bind to = 1", 1, sp.getInetAddrBindToSet().size());
		final Iterator<InetAddress> ifSet = sp.getInetAddrBindToSet().iterator();
		assertEquals("Interface address [0] to bind to = 127.0.0.1", "127.0.0.1", ifSet.next().getHostAddress());
		assertEquals("Server port = 25001", 25001, sp.getServerPort());
		assertFalse("SSL/TLS flag is set to false", sp.useSsl());		
	}

	@Test
	public void loadPreferencesFromSpecificPath() throws TestServerException, URISyntaxException {
		final ServerPreferences sp = new ServerPreferences(new String[] {
			"-f", "config.properties"});		
		assertDefaultSeverPreferences(sp);
	}

	public void loadPreferencesWithNonExistingConfigFileAndFallbackToDefaultSettings() throws TestServerException {
		ServerPreferences sp = new ServerPreferences(new String[] {
				"-f","nonExisitingConfigFile_r7j94dz0kxt35uihz5guiuih32uqi.properties"});
		assertDefaultSeverPreferences(sp);
	}
	
	@Test
	public void loadPreferencesWithArguments() throws TestServerException {
		ServerPreferences sp = new ServerPreferences(new String[] {
				"-ip", "0.0.0.0",
				"-ip", "0.0.0.1",
				"-p", "8080", 
				"-s",
				"-v",
				"-vv",
				"-k", "SECRET_KEY",
				"-ic",
				"-u", "10000", "10010",
				"-t", "111"});
		assertNotNull("ServerPreferences != null", sp);
		assertEquals("Amount of interfaces to bind to != 2", 2, sp.getInetAddrBindToSet().size());
		final Iterator<InetAddress> ifSet = sp.getInetAddrBindToSet().iterator();
		assertEquals("Interface address [0] to bind to != 0.0.0.0", "0.0.0.0", ifSet.next().getHostAddress());
		assertEquals("Interface address [1] to bind to != 0.0.0.1", "0.0.0.1", ifSet.next().getHostAddress());
		assertEquals("Server port != 8080", 8080, sp.getServerPort());
		assertTrue("SSL/TLS flag is set to false", sp.useSsl());		
		assertEquals("Verbose level != 2", 2, sp.getVerboseLevel());
		assertTrue("IP check is false", sp.isIpCheck());
		assertEquals("UDP port range min port != 10000", 10000, sp.getUdpPortMin());
		assertEquals("UDP port range max port != 10010", 10010, sp.getUdpPortMax());
		assertEquals("Max threads != 111", 111, sp.getMaxThreads());
		assertEquals("Secret key != 'SECRET_KEY'", "SECRET_KEY", sp.getSecretKey());
	}

	@Test
	public void loadPreferencesWithDifferentUdpPortsIncludingNio() throws TestServerException {
		ServerPreferences sp = new ServerPreferences(getClass().getResourceAsStream("config_udp.properties"));
		assertNotNull("ServerPreferences != null", sp);
		
		assertEquals("Amount of UDP ports to bind to != 2", 2, sp.getUdpPortSet().size());
		
		final Iterator<UdpPort> ifSet = sp.getUdpPortSet().iterator();
		
		UdpPort port = ifSet.next();
		assertEquals("UDP Port number [0] != 1", 1, port.port);
		assertFalse("UDP Port number [0] == nio", port.isNio);
		assertNotNull(port.toString());
		
		port = ifSet.next();
		assertEquals("UDP Port number [1] != 2", 2, port.port);
		assertTrue("UDP Port number [1] != nio", port.isNio);
		assertNotNull(port.toString());
	}
	
	@Test(expected=TestServerException.class)
	public void loadPreferencesWithIllegalArgumentsThenThrowTestServerException() throws TestServerException {
		ServerPreferences sp = new ServerPreferences(new String[] {
				"-illegalArgument1,", "-illegalArgument2"});
	}

	@Test(expected=TestServerException.class)
	public void loadIllegalPreferencesWithDifferentUdpPortsIncludingNioThenThrowTestServerException() throws TestServerException {
		ServerPreferences sp = new ServerPreferences(getClass().getResourceAsStream("config_udp_err.properties"));
	}

	@Test(expected=TestServerException.class)
	public void loadIllegalPreferencesWithZeroThreadsSetThenThrowTestServerException() throws TestServerException {
		ServerPreferences sp = new ServerPreferences(new String[] {
				"-t","0"});
	}

	@Test(expected=TestServerException.class)
	public void loadIllegalPreferencesWithMaxUdpPortLowerThanMinUdpPortThenThrowTestServerException() throws TestServerException {
		ServerPreferences sp = new ServerPreferences(new String[] {
				"-u","10", "1"});
	}

	@Test
	public void loadPreferencesWithoutValidInterfaceThenSetDefaultInterface() throws TestServerException {
		ServerPreferences sp = new ServerPreferences(new String[] {
				"-vv"});
		
		assertNotNull("ServerPreferences != null", sp);
		assertEquals("Amount of interfaces to bind to != 1", 1, sp.getInetAddrBindToSet().size());
		final Iterator<InetAddress> ifSet = sp.getInetAddrBindToSet().iterator();
		assertEquals("Interface address [0] to bind to != 0.0.0.0", "0.0.0.0", ifSet.next().getHostAddress());
	}

	@Test
	public void loadPreferencesWithDifferentTcpSipCompetences() throws TestServerException {
		ServerPreferences sp = new ServerPreferences(getClass().getResourceAsStream("config_tcp_sip.properties"));
		assertNotNull("ServerPreferences != null", sp);
		
		assertEquals("Amount of additional TCP competences != 3", 3, sp.getTcpCompetenceMap().size());
		assertTrue("Port 5060 is missing SIP competence", sp.getTcpCompetenceMap().get(5060).hasSipCompetence());
		assertTrue("Port 5061 is missing SIP competence", sp.getTcpCompetenceMap().get(5061).hasSipCompetence());
		assertTrue("Port 5062 is missing SIP competence", sp.getTcpCompetenceMap().get(5062).hasSipCompetence());
		assertNull("Port 5063 has additional competences", sp.getTcpCompetenceMap().get(5063));
	}
	
	@Test
	public void testWriteErrorString() {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		final PrintStream ps = new PrintStream(os);
		final PrintStream temp = System.out;
		System.setOut(ps);
		ServerPreferences.writeErrorString();
		System.setOut(temp);
		final String errString = os.toString();
		assertNotNull(errString);
	}
}
