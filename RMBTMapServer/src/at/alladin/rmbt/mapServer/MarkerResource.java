/*******************************************************************************
 * Copyright 2013-2016 alladin-IT GmbH
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
package at.alladin.rmbt.mapServer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import at.alladin.rmbt.mapServer.MapServerOptions.MapFilter;
import at.alladin.rmbt.mapServer.MapServerOptions.MapOption;
import at.alladin.rmbt.mapServer.MapServerOptions.SQLFilter;
import at.alladin.rmbt.shared.Classification;
import at.alladin.rmbt.shared.Helperfunctions;
import at.alladin.rmbt.shared.ResourceManager;
import at.alladin.rmbt.shared.SignificantFormat;
import at.alladin.rmbt.util.capability.ClassificationCapability;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

public class MarkerResource extends ServerResource
{
	private static int MAX_PROVIDER_LENGTH = 22;
	private static int CLICK_RADIUS = 10;

	@Post("json")
	public String request(final String entity)
	{
		addAllowOrigin();

		JSONObject request = null;

		final JSONObject answer = new JSONObject();

		if (entity != null && !entity.isEmpty())
			// try parse the string to a JSON object
			try
		{
				request = new JSONObject(entity);
				readCapabilities(request);

				String lang = request.optString("language");

				// Load Language Files for Client

				final List<String> langs = Arrays.asList(settings.getString("RMBT_SUPPORTED_LANGUAGES").split(",\\s*"));

				if (langs.contains(lang))
					labels = ResourceManager.getSysMsgBundle(new Locale(lang));
				else
					lang = settings.getString("RMBT_DEFAULT_LANGUAGE");

				//                System.out.println(request.toString(4));

				// client requests specific open_test_uuid

				UUID requestOpenTestUUID = null;

				if (request.has("open_test_uuid")) {
					String requestOpenTestUUIDString = null;
					requestOpenTestUUIDString =  request.optString("open_test_uuid");

					if (requestOpenTestUUIDString != null)
						try
					{
							requestOpenTestUUID = UUID.fromString(requestOpenTestUUIDString);
					}
					catch (final Exception e)
					{
						requestOpenTestUUID = null;
					}
				}

				int zoom=1;
				double geo_x = 0;
				double geo_y = 0;
				int size = 0;
				boolean useXY = false;
				boolean useLatLon = false;

				if (request.has("coords")) {
					final JSONObject coords = request.getJSONObject("coords");

					if (coords.has("x") && coords.has("y"))
						useXY = true;
					else if (coords.has("lat") && coords.has("lon"))
						useLatLon = true;

					if (coords.has("z") && (useXY || useLatLon))
					{
						zoom = coords.optInt("z");
						if (useXY)
						{
							geo_x = coords.optDouble("x");
							geo_y = coords.optDouble("y");
						}
						else if (useLatLon)
						{
							final double tmpLat = coords.optDouble("lat");
							final double tmpLon = coords.optDouble("lon");
							geo_x = GeoCalc.lonToMeters(tmpLon);
							geo_y = GeoCalc.latToMeters(tmpLat);
							//                        System.out.println(String.format("using %f/%f", geo_x, geo_y));
						}

						if (coords.has("size"))
							size = coords.getInt("size");

					}
				}    
				if (requestOpenTestUUID != null || (zoom != 0 && geo_x != 0 && geo_y != 0))                   	 
				{
					double radius = 0;
					if (size > 0)
						radius = size * GeoCalc.getResFromZoom(256, zoom); // TODO use real tile size
						else
							radius = CLICK_RADIUS * GeoCalc.getResFromZoom(256, zoom);  // TODO use real tile size
					final double geo_x_min = geo_x - radius;
					final double geo_x_max = geo_x + radius;
					final double geo_y_min = geo_y - radius;
					final double geo_y_max = geo_y + radius;

					String hightlightUUIDString = null;
					UUID highlightUUID = null;

					String optionStr=null;
					if (request.has("options")) {
						final JSONObject mapOptionsObj = request.getJSONObject("options");
						optionStr = mapOptionsObj.optString("map_options");
					}
					if (optionStr == null || optionStr.length() == 0) // set
						// default
						optionStr = "mobile/download";

					final MapOption mo = MapServerOptions.getMapOptionMap().get(optionStr);

					final List<SQLFilter> filters = new ArrayList<SQLFilter>(MapServerOptions.getDefaultMapFilters());
					filters.add(MapServerOptions.getAccuracyMapFilter());


					if (request.has("filter")){
						final JSONObject mapFilterObj = request.getJSONObject("filter");

						final Iterator<?> keys = mapFilterObj.keys();

						while (keys.hasNext())
						{
							final String key = (String) keys.next();
							if (mapFilterObj.get(key) instanceof Object)
								if (key.equals("highlight")) {
									hightlightUUIDString =  mapFilterObj.getString(key);
								}
								else if (key.equals("four_color")) {
									// clients supporting four color classification will add this key
									capabilities.getClassificationCapability().setCount(mapFilterObj.getBoolean(key) ? 4 : ClassificationCapability.DEFAULT_CLASSIFICATON_COUNT);
								}
								else
								{
									final MapFilter mapFilter = MapServerOptions.getMapFilterMap().get(key);
									if (mapFilter != null)
										filters.add(mapFilter.getFilter(mapFilterObj.getString(key)));
								}
						}
					}

					if (hightlightUUIDString != null)
						try
					{
							highlightUUID = UUID.fromString(hightlightUUIDString);
					}
					catch (final Exception e)
					{
						highlightUUID = null;
					}

					if (conn != null)
					{
						PreparedStatement ps = null;
						ResultSet rs = null;

						final StringBuilder whereSQL = new StringBuilder(mo.sqlFilter);
						if (requestOpenTestUUID == null)
							for (final SQLFilter sf : filters)
								whereSQL.append(" AND ").append(sf.where);
						else
							whereSQL.setLength(0);

						final String sql = String
								.format("SELECT"
										+ (useLatLon ? " geo_lat lat, geo_long lon"
												: " ST_X(t.location) x, ST_Y(t.location) y")
												+ ", t.time, t.timezone, t.speed_download, t.speed_upload, t.ping_median, t.network_type,"
												+ " t.signal_strength, t.lte_rsrp, t.wifi_ssid, t.network_operator_name, t.network_operator,"
												+ " t.network_sim_operator, t.roaming_type, t.public_ip_as_name, " //TODO: sim_operator obsoleted by sim_name
												+ " pMob.shortname mobile_provider_name," // TODO: obsoleted by mobile_network_name
												+ " prov.shortname provider_text, t.open_test_uuid,"
												+ " COALESCE(mnwk.shortname,mnwk.name) mobile_network_name,"
												+ " COALESCE(msim.shortname,msim.name) mobile_sim_name"
												+ (highlightUUID == null ? "" : " , c.uid, c.uuid")
												+ " FROM v_test2 t"
												+ " LEFT JOIN mccmnc2name mnwk ON t.mobile_network_id=mnwk.uid"
												+ " LEFT JOIN mccmnc2name msim ON t.mobile_sim_id=msim.uid"
												+ " LEFT JOIN provider prov"
												+ " ON t.provider_id=prov.uid"
												+ " LEFT JOIN provider pMob"
												+ " ON t.mobile_provider_id=pMob.uid"
												+ (highlightUUID == null ? ""
														: " LEFT JOIN client c ON (t.client_id=c.uid AND t.uuid=?)")
														+ " WHERE"
														+ " %s"
														+ (requestOpenTestUUID != null ? 
																" t.open_test_uuid=? "
																:" AND location && ST_SetSRID(ST_MakeBox2D(ST_Point(?,?), ST_Point(?,?)), 900913)")
																+ " ORDER BY" + (highlightUUID == null ? "" : " c.uid ASC,")
																+ " t.uid DESC" + " LIMIT 5", whereSQL);

						//System.out.println("SQL: " + sql);
						ps = conn.prepareStatement(sql);

						int i = 1;

						if (highlightUUID != null)
							ps.setObject(i++, highlightUUID);

						// filter by location if not selected by open_test_uuid
						if (requestOpenTestUUID == null){ 
							for (final SQLFilter sf : filters)
								i = sf.fillParams(i, ps);
							ps.setDouble(i++, geo_x_min);
							ps.setDouble(i++, geo_y_min);
							ps.setDouble(i++, geo_x_max);
							ps.setDouble(i++, geo_y_max); }
						else
							ps.setObject(i++, requestOpenTestUUID);

						//System.out.println("SQL: " + ps.toString());

						if (ps.execute())
						{

							final Locale locale = new Locale(lang);
							final Format format = new SignificantFormat(2, locale);

							final JSONArray resultList = new JSONArray();

							rs = ps.getResultSet();

							while (rs.next())
							{
								final JSONObject jsonItem = new JSONObject();

								JSONArray jsonItemList = new JSONArray();

								// RMBTClient Info
								if (highlightUUID != null && rs.getString("uuid") != null)
									jsonItem.put("highlight", true);

								final double res_x = rs.getDouble(1);
								final double res_y = rs.getDouble(2);
								final String openTestUUID = rs.getObject("open_test_uuid").toString();

								jsonItem.put("lat", res_x);
								jsonItem.put("lon", res_y);
								jsonItem.put("open_test_uuid", "O" + openTestUUID);
								// marker.put("uid", uid);

								final Date date = rs.getTimestamp("time");
								final String tzString = rs.getString("timezone");
								final TimeZone tz = TimeZone.getTimeZone(tzString);
								final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
										DateFormat.MEDIUM, locale);
								dateFormat.setTimeZone(tz);
								jsonItem.put("time_string", dateFormat.format(date));

								//time as UNIX time (UTC) e.g. 1445361731053
								final long time = date.getTime();
								jsonItem.put("time", time);

								final int fieldDown = rs.getInt("speed_download");
								JSONObject singleItem = new JSONObject();
								singleItem.put("title", labels.getString("RESULT_DOWNLOAD"));
								final String downloadString = String.format("%s %s",
										format.format(fieldDown / 1000d), labels.getString("RESULT_DOWNLOAD_UNIT"));
								singleItem.put("value", downloadString);
								singleItem.put("classification",
										Classification.classify(Classification.THRESHOLD_DOWNLOAD, fieldDown, capabilities.getClassificationCapability().getCount()));


								jsonItemList.put(singleItem);

								final int fieldUp = rs.getInt("speed_upload");
								singleItem = new JSONObject();
								singleItem.put("title", labels.getString("RESULT_UPLOAD"));
								final String uploadString = String.format("%s %s", format.format(fieldUp / 1000d),
										labels.getString("RESULT_UPLOAD_UNIT"));
								singleItem.put("value", uploadString);
								singleItem.put("classification",
										Classification.classify(Classification.THRESHOLD_UPLOAD, fieldUp, capabilities.getClassificationCapability().getCount()));

								jsonItemList.put(singleItem);

								final long fieldPing = rs.getLong("ping_median");
								final int pingValue = (int) Math.round(rs.getDouble("ping_median") / 1000000d);
								singleItem = new JSONObject();
								singleItem.put("title", labels.getString("RESULT_PING"));
								final String pingString = String.format("%s %s", format.format(pingValue),
										labels.getString("RESULT_PING_UNIT"));
								singleItem.put("value", pingString);
								singleItem.put("classification",
										Classification.classify(Classification.THRESHOLD_PING, fieldPing, capabilities.getClassificationCapability().getCount()));

								jsonItemList.put(singleItem);

								final int networkType = rs.getInt("network_type");

								final String signalField = rs.getString("signal_strength");
								if (signalField != null && signalField.length() != 0)
								{
									final int signalValue = rs.getInt("signal_strength");
									final int[] threshold = networkType == 99 || networkType == 0 ? Classification.THRESHOLD_SIGNAL_WIFI
											: Classification.THRESHOLD_SIGNAL_MOBILE;
									singleItem = new JSONObject();
									singleItem.put("title", labels.getString("RESULT_SIGNAL"));
									singleItem.put("value",
											signalValue + " " + labels.getString("RESULT_SIGNAL_UNIT"));
									singleItem.put("classification",
											Classification.classify(threshold, signalValue, capabilities.getClassificationCapability().getCount()));
									jsonItemList.put(singleItem);
								}

								final String lteRsrpField = rs.getString("lte_rsrp");
								if (lteRsrpField != null && lteRsrpField.length() != 0)
								{
									final int lteRsrpValue = rs.getInt("lte_rsrp");
									final int[] threshold = Classification.THRESHOLD_SIGNAL_RSRP;
									singleItem = new JSONObject();
									singleItem.put("title", labels.getString("RESULT_LTE_RSRP"));
									singleItem.put("value",
											lteRsrpValue + " " + labels.getString("RESULT_LTE_RSRP_UNIT"));
									singleItem.put("classification",
											Classification.classify(threshold, lteRsrpValue, capabilities.getClassificationCapability().getCount()));
									jsonItemList.put(singleItem);
								}


								jsonItem.put("measurement", jsonItemList);

								jsonItemList = new JSONArray();

								singleItem = new JSONObject();
								singleItem.put("title", labels.getString("RESULT_NETWORK_TYPE"));
								singleItem.put("value", Helperfunctions.getNetworkTypeName(networkType));

								jsonItemList.put(singleItem);


								if (networkType == 98 || networkType == 99) // mobile wifi or browser
								{
									String providerText = Objects.firstNonNull(rs.getString("provider_text"),rs.getString("public_ip_as_name"));
									if (! Strings.isNullOrEmpty(providerText))
									{
										if (providerText.length() > (MAX_PROVIDER_LENGTH +3)) {
											providerText = providerText.substring(0, MAX_PROVIDER_LENGTH) + "...";
										}

										singleItem = new JSONObject();
										singleItem.put("title", labels.getString("RESULT_PROVIDER"));
										singleItem.put("value", providerText);
										jsonItemList.put(singleItem);
									}
									if (networkType == 99)  // mobile wifi
									{
										if (highlightUUID != null && rs.getString("uuid") != null) // own test
										{
											final String ssid = rs.getString("wifi_ssid");
											if (ssid != null && ssid.length() != 0)
											{
												singleItem = new JSONObject();
												singleItem.put("title", labels.getString("RESULT_WIFI_SSID"));
												singleItem.put("value", ssid.toString());
												jsonItemList.put(singleItem);
											}
										}
									}
								}
								else // mobile
								{
									String networkOperator = rs.getString("network_operator");
									String mobileNetworkName = rs.getString("mobile_network_name");
									String simOperator = rs.getString("network_sim_operator");
									String mobileSimName = rs.getString("mobile_sim_name");
									final int roamingType = rs.getInt("roaming_type");
									//network
									if (! Strings.isNullOrEmpty(networkOperator))
									{
										final String mobileNetworkString;
										if (roamingType != 2) { //not international roaming - display name of home network
											if (Strings.isNullOrEmpty(mobileSimName))
												mobileNetworkString = networkOperator;
											else
												mobileNetworkString = String.format("%s (%s)", mobileSimName, networkOperator);
										}
										else { //international roaming - display name of network
											if (Strings.isNullOrEmpty(mobileSimName))
												mobileNetworkString = networkOperator;
											else
												mobileNetworkString = String.format("%s (%s)", mobileNetworkName, networkOperator);
										}

										singleItem = new JSONObject();
										singleItem.put("title", labels.getString("RESULT_MOBILE_NETWORK"));
										singleItem.put("value", mobileNetworkString);
										jsonItemList.put(singleItem);
									}
									//home network (sim)
									else if (!Strings.isNullOrEmpty(simOperator)) {
										final String mobileNetworkString;

										if (Strings.isNullOrEmpty(mobileSimName))
											mobileNetworkString = simOperator;
										else
											mobileNetworkString = String.format("%s (%s)", mobileSimName, simOperator);

										/*
                                        	if (!Strings.isNullOrEmpty(mobileProviderName)) {
                                        		mobileNetworkString = mobileProviderName;
                                        	} else {
                                        		mobileNetworkString = simOperator;
                                        	}
										 */

										singleItem = new JSONObject();
										singleItem.put("title", labels.getString("RESULT_HOME_NETWORK"));
										singleItem.put("value", mobileNetworkString);
										jsonItemList.put(singleItem);
									}

									if (roamingType > 0)
									{
										singleItem = new JSONObject();
										singleItem.put("title", labels.getString("RESULT_ROAMING"));
										singleItem.put("value", Helperfunctions.getRoamingType(labels, roamingType));
										jsonItemList.put(singleItem);
									}
								}

								jsonItem.put("net", jsonItemList);

								resultList.put(jsonItem);

								if (resultList.length() == 0)
									System.out.println("Error getting Results.");
								// errorList.addError(MessageFormat.format(labels.getString("ERROR_DB_GET_CLIENT"),
								// new Object[] {uuid}));

							}

							answer.put("measurements", resultList);
						}
						else
							System.out.println("Error executing SQL.");
					}
					else
						System.out.println("No Database Connection.");
				}

				else
					System.out.println("Expected request is missing.");

		}
		catch (final JSONException e)
		{
			System.out.println("Error parsing JSDON Data " + e.toString());
		}
		catch (final SQLException e)
		{
			e.printStackTrace();
		}
		else
			System.out.println("No Request.");

		return answer.toString();

	}

	@Get("json")
	public String retrieve(final String entity)
	{
		return request(entity);
	}

}
