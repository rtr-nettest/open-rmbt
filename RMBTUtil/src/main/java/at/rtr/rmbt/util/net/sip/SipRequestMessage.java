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

package at.rtr.rmbt.util.net.sip;

/**
 * 
 * @author lb@alladin.at
 *
 */
public class SipRequestMessage extends SipMessage {

	public enum SipRequestType {
		INVITE,
		ACK,
		BYE
	}

	private SipRequestType type;
	
	public SipRequestMessage(final SipRequestType type) {
		this.type = type;
	}
	
	public SipRequestMessage(final SipRequestType type, final SipResponseMessage response) {
		this.type = type;
		this.setTo(response.getFrom());
		this.setFrom(response.getTo());
		this.setVia(response.getVia());
	}
	
	public SipRequestType getType() {
		return type;
	}

	public void setType(SipRequestType type) {
		this.type = type;
	}

	@Override
	String getFirstLine() {
		return type.toString() + " " + getTo() + " " + SIP_PROTOCOL_STRING;
	}

	@Override
	public String toString() {
		return "SipRequestMessage [type=" + type + ", toString()=" + super.toString() + "]";
	}
}
