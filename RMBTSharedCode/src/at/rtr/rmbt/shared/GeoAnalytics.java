/*******************************************************************************
 * Copyright 2013-2016 alladin-IT GmbH
 * Copyright 2013-2016 Rundfunk und Telekom Regulierungs-GmbH (RTR-GmbH)
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
package at.rtr.rmbt.shared;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;


public class GeoAnalytics {


	public static class TestDistance {
		private double totalDistance;
		private double maxAccuracy;

		/**
		 * Calculate distance of between geo_locations of a single test in meter
		 * @param openTestUuid
		 * @param conn
		 * @return
		 */
		public TestDistance(UUID openTestUuid, java.sql.Connection conn) {

			try {
				final ResultSet rs;
				//get distance from GPS locations (iOS does not inform about provider)
				PreparedStatement ps = conn.prepareStatement("select max(g.accuracy) max_accuracy,st_lengthSpheroid(st_transform(st_makeline(g.location " +
						"order by g.time_ns),4326),'SPHEROID[\"WGS 84\",6378137,298.257223563]') as distance " +
						"from geo_location as g  where g.open_test_uuid= ? and (g.provider='gps' or g.provider='' or g.provider is null)\n" +
						"group by g.open_test_uuid;");
				ps.setObject(1, openTestUuid, Types.OTHER);
				rs = ps.executeQuery();
				if (rs != null && rs.next()) {
					totalDistance = rs.getLong("distance");
					maxAccuracy = rs.getLong("max_accuracy");
					// take accuracy into account
					if (maxAccuracy > totalDistance)
						totalDistance = 0;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				totalDistance = 0;
			}
		}

		public double getTotalDistance() {
			return this.totalDistance;
		}
	}
}
