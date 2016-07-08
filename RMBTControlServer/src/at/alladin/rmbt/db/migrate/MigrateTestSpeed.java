/*******************************************************************************
 * Copyright 2015, 2016 alladin-IT GmbH
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
 *******************************************************************************/

 /* 
  * 
  * 
  * dead code - was used for migration of speed_items to JSON (within test table)
  * 
  * 
  * 
package at.alladin.rmbt.db.migrate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.restlet.resource.Get;

import at.alladin.rmbt.controlServer.ServerResource;
import at.alladin.rmbt.shared.model.SpeedItems;
import at.alladin.rmbt.shared.model.SpeedItems.SpeedItem;

import com.google.gson.Gson;

public class MigrateTestSpeed extends ServerResource
{
    @Get
    public String request()
    {
        try
        {
            final Gson gson = getGson(false);
            
            final PreparedStatement ps1 = conn.prepareStatement("SELECT uid FROM test t WHERE speed_items IS NULL AND EXISTS(SELECT 1 FROM test_speed WHERE test_id=t.uid) LIMIT 100000");
            final PreparedStatement ps2 = conn.prepareStatement("SELECT * FROM test_speed WHERE test_id=? ORDER BY upload, thread, time");
            final PreparedStatement ps3 = conn.prepareStatement("UPDATE test SET speed_items = ?::json WHERE uid = ?");
            final ResultSet rs1 = ps1.executeQuery();
            int counter = 0;
            while (rs1.next())
            {
                final long uid = rs1.getLong(1);
                
                ps2.setLong(1, uid);
                final ResultSet rs2 = ps2.executeQuery();
                
                final SpeedItems result = new SpeedItems();
                while (rs2.next())
                {
                    final boolean upload = rs2.getBoolean("upload");
                    final int thread = rs2.getInt("thread");
                    final long time = rs2.getLong("time");
                    final long bytes = rs2.getLong("bytes");
                    
                    final SpeedItem speedItem = new SpeedItem(time, bytes);
                    if (upload)
                        result.addSpeedItemUpload(speedItem, thread);
                    else
                        result.addSpeedItemDownload(speedItem, thread);
                }
                
                final String json = gson.toJson(result);
                ps3.setString(1, json);
                ps3.setLong(2, uid);
                ps3.executeUpdate();
                
                Thread.sleep(1);
                
//                return Long.toString(uid);
//                return gson.toJson(result);
                
                counter++;
                if (counter % 100 == 0)
                    System.out.println("migrated: " + counter);
            }
            
            return "done: " + counter;
        }
        catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
*/
