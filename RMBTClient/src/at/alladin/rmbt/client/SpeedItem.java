/*******************************************************************************
 * Copyright 2013-2015 alladin-IT GmbH
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
package at.alladin.rmbt.client;

import org.json.JSONException;
import org.json.JSONObject;

public class SpeedItem
{
    public final boolean upload;
    public final int thread;
    public final long time;
    public final long bytes;
    
    public SpeedItem(final boolean upload, final int thread, final long time, final long bytes)
    {
        this.upload = upload;
        this.thread = thread;
        this.time = time;
        this.bytes = bytes;
    }
    
    public JSONObject toJSON() throws JSONException
    {
        final JSONObject result = new JSONObject();
        result.put("direction", upload ? "upload" : "download");
        result.put("thread", thread);
        result.put("time", time);
        result.put("bytes", bytes);
        return result;
    }

	@Override
	public String toString() {
		return "SpeedItem [upload=" + upload + ", thread=" + thread + ", time="
				+ time + ", bytes=" + bytes + "]";
	}
}
