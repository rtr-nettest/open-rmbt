/***************************************************************************
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
 ***************************************************************************/

package at.rtr.rmbt.qos.testserver.tcp.competences.sip;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import at.rtr.rmbt.qos.testserver.tcp.competences.SleepAction;
import com.google.common.base.Strings;

import at.rtr.rmbt.qos.testserver.tcp.competences.Action;
import at.rtr.rmbt.qos.testserver.tcp.competences.Competence;
import at.rtr.rmbt.qos.testserver.tcp.competences.RepeatAction;
import at.rtr.rmbt.qos.testserver.tcp.competences.ResponseAction;
import at.rtr.rmbt.util.net.sip.SipMessage;
import at.rtr.rmbt.util.net.sip.SipRequestMessage;
import at.rtr.rmbt.util.net.sip.SipResponseMessage;
import at.rtr.rmbt.util.net.sip.SipResponseMessage.SipResponseType;
import at.rtr.rmbt.util.net.sip.SipUtil;

/**
 * 
 * @author lb@alladin.at
 *
 */
public class SipCompetence implements Competence {

	@Override
	public boolean appliesTo(final String data) {
		//try to parse first line
		return (SipUtil.parseRequestData(data) != null || SipUtil.parseResponseData(data) != null);
	}

	@Override
	public String readFullRequest(String firstLine, BufferedReader br) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append(firstLine).append("\n");
		String line = null;
		while((line = br.readLine()) != null) {
			sb.append(line).append("\n");
			if (Strings.isNullOrEmpty(line)) {
				break;
			}
		}
		return sb.toString();
	}	

	@Override
	public List<Action> processRequest(final String sipData) {
		try {
			final List<Action> resultList = new ArrayList<>();
			
			SipMessage msg = SipUtil.parseResponseData(sipData);
			
			if (msg == null) {
				msg = SipUtil.parseRequestData(sipData);
			}
			
			if (msg != null) {
				if (msg instanceof SipResponseMessage) {
					//we have a SIP response message
					switch (((SipResponseMessage) msg).getType()) {
					case OK:
						break;
					case RINGING:
						break;
					case TRYING:
						break;
					}
				}
				else if (msg instanceof SipRequestMessage) {
					//we have a SIP request message
					switch (((SipRequestMessage) msg).getType()) {
					case ACK:
						//ACK does not demand a response
						break;
					case BYE:
						resultList.add(new ResponseAction(
								new SipResponseMessage(SipResponseType.OK, (SipRequestMessage) msg).getData()));
						break;
					case INVITE:
						resultList.add(new ResponseAction(
								new SipResponseMessage(SipResponseType.TRYING, (SipRequestMessage) msg).getData()));
						resultList.add(new SleepAction(100));
						resultList.add(new ResponseAction(
								new SipResponseMessage(SipResponseType.RINGING, (SipRequestMessage) msg).getData()));
						resultList.add(new RepeatAction());
						break;
					}
				}
			}

			return resultList;
		}
		catch (final Exception e) {
			e.printStackTrace();
			return null;
		}		
	}
}
