/*******************************************************************************
 * Copyright 2015 alladin-IT GmbH
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
package at.alladin.rmbt.shared.model;

import java.util.UUID;

import org.joda.time.DateTime;

public class Measurement
{
    protected int uid;
    protected UUID uuid;
    protected Integer client_id;
    protected String client_version;
    protected String client_name;
    protected String client_language;
    protected String client_local_ip;
    protected String token;
    protected Integer server_id;
    protected Integer port;
    protected boolean use_ssl;
    protected DateTime time;
    protected Long speed_upload;
    protected Long speed_download;
    protected Long ping_shortest;
    protected String encryption;
    protected String client_public_ip;
    protected String plattform;
    protected String os_version;
    protected String api_level;
    protected String device;
    protected String model;
    protected String product;
    protected Integer phone_type;
    protected Integer data_state;
    protected String network_country;
    protected String network_operator;
    protected String network_operator_name;
    protected String network_sim_country;
    protected String network_sim_operator;
    protected String network_sim_operator_name;
    protected String wifi_ssid;
    protected String wifi_bssid;
    protected String wifi_network_id;
    protected Integer duration;
    protected Integer num_threads;
    protected String status;
    protected String timezone;
    protected Long bytes_download;
    protected Long bytes_upload;
    protected Long nsec_download;
    protected Long nsec_upload;
    protected String server_ip;
    protected String client_software_version;
    protected Double geo_lat;
    protected Double geo_long;
    protected Integer network_type;
    protected Integer signal_strength;
    protected String software_revision;
    protected Long client_test_counter;
    protected String nat_type;
    protected String client_previous_test_status;
    protected Long public_ip_asn;
    protected Double speed_upload_log;
    protected Double speed_download_log;
    protected Long total_bytes_download;
    protected Long total_bytes_upload;
    protected Integer wifi_link_speed;
    protected String public_ip_rdns;
    protected String public_ip_as_name;
    protected Integer test_slot;
    protected Integer provider_id;
    protected Boolean network_is_roaming;
    protected Double ping_shortest_log;
    protected Boolean run_ndt;
    protected Integer num_threads_requested;
    protected String client_public_ip_anonymized;
    protected Integer zip_code;
    protected String geo_provider;
    protected Double geo_accuracy;
    protected boolean deleted;
    protected String comment;
    protected UUID open_uuid;
    protected DateTime client_time;
    protected Integer zip_code_geo;
    protected Integer mobile_provider_id;
    protected Integer roaming_type;
    protected UUID open_test_uuid;
    protected String country_asn;
    protected String country_location;
    protected Long test_if_bytes_download;
    protected Long test_if_bytes_upload;
    protected boolean implausible;
    protected Long testdl_if_bytes_download;
    protected Long testdl_if_bytes_upload;
    protected Long testul_if_bytes_download;
    protected Long testul_if_bytes_upload;
    protected String country_geoip;
    protected Integer location_max_distance;
    protected Integer location_max_distance_gps;
    protected String network_group_name;
    protected String network_group_type;
    protected Long time_dl_ns;
    protected Long time_ul_ns;
    protected Integer num_threads_ul;
    protected DateTime timestamp;
    protected String source_ip;
    protected Integer lte_rsrp;
    protected Integer lte_rsrq;
    protected Integer mobile_network_id;
    protected Integer mobile_sim_id;
    protected Double dist_prev;
    protected Double speed_prev;
    protected String tag;
    protected Long ping_median;
    protected Double ping_median_log;
    protected String source_ip_anonymized;
    protected String client_ip_local;
    protected String client_ip_local_anonymized;
    protected String client_ip_local_type;
    protected String hidden_code;
    
    public int getUid()
    {
        return uid;
    }
    public void setUid(int uid)
    {
        this.uid = uid;
    }
    public UUID getUuid()
    {
        return uuid;
    }
    public void setUuid(UUID uuid)
    {
        this.uuid = uuid;
    }
    public Integer getClient_id()
    {
        return client_id;
    }
    public void setClient_id(Integer client_id)
    {
        this.client_id = client_id;
    }
    public String getClient_version()
    {
        return client_version;
    }
    public void setClient_version(String client_version)
    {
        this.client_version = client_version;
    }
    public String getClient_name()
    {
        return client_name;
    }
    public void setClient_name(String client_name)
    {
        this.client_name = client_name;
    }
    public String getClient_language()
    {
        return client_language;
    }
    public void setClient_language(String client_language)
    {
        this.client_language = client_language;
    }
    public String getClient_local_ip()
    {
        return client_local_ip;
    }
    public void setClient_local_ip(String client_local_ip)
    {
        this.client_local_ip = client_local_ip;
    }
    public String getToken()
    {
        return token;
    }
    public void setToken(String token)
    {
        this.token = token;
    }
    public Integer getServer_id()
    {
        return server_id;
    }
    public void setServer_id(Integer server_id)
    {
        this.server_id = server_id;
    }
    public Integer getPort()
    {
        return port;
    }
    public void setPort(Integer port)
    {
        this.port = port;
    }
    public boolean isUse_ssl()
    {
        return use_ssl;
    }
    public void setUse_ssl(boolean use_ssl)
    {
        this.use_ssl = use_ssl;
    }
    public DateTime getTime()
    {
        return time;
    }
    public void setTime(DateTime time)
    {
        this.time = time;
    }
    public Long getSpeed_upload()
    {
        return speed_upload;
    }
    public void setSpeed_upload(Long speed_upload)
    {
        this.speed_upload = speed_upload;
    }
    public Long getSpeed_download()
    {
        return speed_download;
    }
    public void setSpeed_download(Long speed_download)
    {
        this.speed_download = speed_download;
    }
    public Long getPing_shortest()
    {
        return ping_shortest;
    }
    public void setPing_shortest(Long ping_shortest)
    {
        this.ping_shortest = ping_shortest;
    }
    public String getEncryption()
    {
        return encryption;
    }
    public void setEncryption(String encryption)
    {
        this.encryption = encryption;
    }
    public String getClient_public_ip()
    {
        return client_public_ip;
    }
    public void setClient_public_ip(String client_public_ip)
    {
        this.client_public_ip = client_public_ip;
    }
    public String getPlattform()
    {
        return plattform;
    }
    public void setPlattform(String plattform)
    {
        this.plattform = plattform;
    }
    public String getOs_version()
    {
        return os_version;
    }
    public void setOs_version(String os_version)
    {
        this.os_version = os_version;
    }
    public String getApi_level()
    {
        return api_level;
    }
    public void setApi_level(String api_level)
    {
        this.api_level = api_level;
    }
    public String getDevice()
    {
        return device;
    }
    public void setDevice(String device)
    {
        this.device = device;
    }
    public String getModel()
    {
        return model;
    }
    public void setModel(String model)
    {
        this.model = model;
    }
    public String getProduct()
    {
        return product;
    }
    public void setProduct(String product)
    {
        this.product = product;
    }
    public Integer getPhone_type()
    {
        return phone_type;
    }
    public void setPhone_type(Integer phone_type)
    {
        this.phone_type = phone_type;
    }
    public Integer getData_state()
    {
        return data_state;
    }
    public void setData_state(Integer data_state)
    {
        this.data_state = data_state;
    }
    public String getNetwork_country()
    {
        return network_country;
    }
    public void setNetwork_country(String network_country)
    {
        this.network_country = network_country;
    }
    public String getNetwork_operator()
    {
        return network_operator;
    }
    public void setNetwork_operator(String network_operator)
    {
        this.network_operator = network_operator;
    }
    public String getNetwork_operator_name()
    {
        return network_operator_name;
    }
    public void setNetwork_operator_name(String network_operator_name)
    {
        this.network_operator_name = network_operator_name;
    }
    public String getNetwork_sim_country()
    {
        return network_sim_country;
    }
    public void setNetwork_sim_country(String network_sim_country)
    {
        this.network_sim_country = network_sim_country;
    }
    public String getNetwork_sim_operator()
    {
        return network_sim_operator;
    }
    public void setNetwork_sim_operator(String network_sim_operator)
    {
        this.network_sim_operator = network_sim_operator;
    }
    public String getNetwork_sim_operator_name()
    {
        return network_sim_operator_name;
    }
    public void setNetwork_sim_operator_name(String network_sim_operator_name)
    {
        this.network_sim_operator_name = network_sim_operator_name;
    }
    public String getWifi_ssid()
    {
        return wifi_ssid;
    }
    public void setWifi_ssid(String wifi_ssid)
    {
        this.wifi_ssid = wifi_ssid;
    }
    public String getWifi_bssid()
    {
        return wifi_bssid;
    }
    public void setWifi_bssid(String wifi_bssid)
    {
        this.wifi_bssid = wifi_bssid;
    }
    public String getWifi_network_id()
    {
        return wifi_network_id;
    }
    public void setWifi_network_id(String wifi_network_id)
    {
        this.wifi_network_id = wifi_network_id;
    }
    public Integer getDuration()
    {
        return duration;
    }
    public void setDuration(Integer duration)
    {
        this.duration = duration;
    }
    public Integer getNum_threads()
    {
        return num_threads;
    }
    public void setNum_threads(Integer num_threads)
    {
        this.num_threads = num_threads;
    }
    public String getStatus()
    {
        return status;
    }
    public void setStatus(String status)
    {
        this.status = status;
    }
    public String getTimezone()
    {
        return timezone;
    }
    public void setTimezone(String timezone)
    {
        this.timezone = timezone;
    }
    public Long getBytes_download()
    {
        return bytes_download;
    }
    public void setBytes_download(Long bytes_download)
    {
        this.bytes_download = bytes_download;
    }
    public Long getBytes_upload()
    {
        return bytes_upload;
    }
    public void setBytes_upload(Long bytes_upload)
    {
        this.bytes_upload = bytes_upload;
    }
    public Long getNsec_download()
    {
        return nsec_download;
    }
    public void setNsec_download(Long nsec_download)
    {
        this.nsec_download = nsec_download;
    }
    public Long getNsec_upload()
    {
        return nsec_upload;
    }
    public void setNsec_upload(Long nsec_upload)
    {
        this.nsec_upload = nsec_upload;
    }
    public String getServer_ip()
    {
        return server_ip;
    }
    public void setServer_ip(String server_ip)
    {
        this.server_ip = server_ip;
    }
    public String getClient_software_version()
    {
        return client_software_version;
    }
    public void setClient_software_version(String client_software_version)
    {
        this.client_software_version = client_software_version;
    }
    public Double getGeo_lat()
    {
        return geo_lat;
    }
    public void setGeo_lat(Double geo_lat)
    {
        this.geo_lat = geo_lat;
    }
    public Double getGeo_long()
    {
        return geo_long;
    }
    public void setGeo_long(Double geo_long)
    {
        this.geo_long = geo_long;
    }
    public Integer getNetwork_type()
    {
        return network_type;
    }
    public void setNetwork_type(Integer network_type)
    {
        this.network_type = network_type;
    }
    public Integer getSignal_strength()
    {
        return signal_strength;
    }
    public void setSignal_strength(Integer signal_strength)
    {
        this.signal_strength = signal_strength;
    }
    public String getSoftware_revision()
    {
        return software_revision;
    }
    public void setSoftware_revision(String software_revision)
    {
        this.software_revision = software_revision;
    }
    public Long getClient_test_counter()
    {
        return client_test_counter;
    }
    public void setClient_test_counter(Long client_test_counter)
    {
        this.client_test_counter = client_test_counter;
    }
    public String getNat_type()
    {
        return nat_type;
    }
    public void setNat_type(String nat_type)
    {
        this.nat_type = nat_type;
    }
    public String getClient_previous_test_status()
    {
        return client_previous_test_status;
    }
    public void setClient_previous_test_status(String client_previous_test_status)
    {
        this.client_previous_test_status = client_previous_test_status;
    }
    public Long getPublic_ip_asn()
    {
        return public_ip_asn;
    }
    public void setPublic_ip_asn(Long public_ip_asn)
    {
        this.public_ip_asn = public_ip_asn;
    }
    public Double getSpeed_upload_log()
    {
        return speed_upload_log;
    }
    public void setSpeed_upload_log(Double speed_upload_log)
    {
        this.speed_upload_log = speed_upload_log;
    }
    public Double getSpeed_download_log()
    {
        return speed_download_log;
    }
    public void setSpeed_download_log(Double speed_download_log)
    {
        this.speed_download_log = speed_download_log;
    }
    public Long getTotal_bytes_download()
    {
        return total_bytes_download;
    }
    public void setTotal_bytes_download(Long total_bytes_download)
    {
        this.total_bytes_download = total_bytes_download;
    }
    public Long getTotal_bytes_upload()
    {
        return total_bytes_upload;
    }
    public void setTotal_bytes_upload(Long total_bytes_upload)
    {
        this.total_bytes_upload = total_bytes_upload;
    }
    public Integer getWifi_link_speed()
    {
        return wifi_link_speed;
    }
    public void setWifi_link_speed(Integer wifi_link_speed)
    {
        this.wifi_link_speed = wifi_link_speed;
    }
    public String getPublic_ip_rdns()
    {
        return public_ip_rdns;
    }
    public void setPublic_ip_rdns(String public_ip_rdns)
    {
        this.public_ip_rdns = public_ip_rdns;
    }
    public String getPublic_ip_as_name()
    {
        return public_ip_as_name;
    }
    public void setPublic_ip_as_name(String public_ip_as_name)
    {
        this.public_ip_as_name = public_ip_as_name;
    }
    public Integer getTest_slot()
    {
        return test_slot;
    }
    public void setTest_slot(Integer test_slot)
    {
        this.test_slot = test_slot;
    }
    public Integer getProvider_id()
    {
        return provider_id;
    }
    public void setProvider_id(Integer provider_id)
    {
        this.provider_id = provider_id;
    }
    public Boolean getNetwork_is_roaming()
    {
        return network_is_roaming;
    }
    public void setNetwork_is_roaming(Boolean network_is_roaming)
    {
        this.network_is_roaming = network_is_roaming;
    }
    public Double getPing_shortest_log()
    {
        return ping_shortest_log;
    }
    public void setPing_shortest_log(Double ping_shortest_log)
    {
        this.ping_shortest_log = ping_shortest_log;
    }
    public Boolean getRun_ndt()
    {
        return run_ndt;
    }
    public void setRun_ndt(Boolean run_ndt)
    {
        this.run_ndt = run_ndt;
    }
    public Integer getNum_threads_requested()
    {
        return num_threads_requested;
    }
    public void setNum_threads_requested(Integer num_threads_requested)
    {
        this.num_threads_requested = num_threads_requested;
    }
    public String getClient_public_ip_anonymized()
    {
        return client_public_ip_anonymized;
    }
    public void setClient_public_ip_anonymized(String client_public_ip_anonymized)
    {
        this.client_public_ip_anonymized = client_public_ip_anonymized;
    }
    public Integer getZip_code()
    {
        return zip_code;
    }
    public void setZip_code(Integer zip_code)
    {
        this.zip_code = zip_code;
    }
    public String getGeo_provider()
    {
        return geo_provider;
    }
    public void setGeo_provider(String geo_provider)
    {
        this.geo_provider = geo_provider;
    }
    public Double getGeo_accuracy()
    {
        return geo_accuracy;
    }
    public void setGeo_accuracy(Double geo_accuracy)
    {
        this.geo_accuracy = geo_accuracy;
    }
    public boolean isDeleted()
    {
        return deleted;
    }
    public void setDeleted(boolean deleted)
    {
        this.deleted = deleted;
    }
    public String getComment()
    {
        return comment;
    }
    public void setComment(String comment)
    {
        this.comment = comment;
    }
    public UUID getOpen_uuid()
    {
        return open_uuid;
    }
    public void setOpen_uuid(UUID open_uuid)
    {
        this.open_uuid = open_uuid;
    }
    public DateTime getClient_time()
    {
        return client_time;
    }
    public void setClient_time(DateTime client_time)
    {
        this.client_time = client_time;
    }
    public Integer getZip_code_geo()
    {
        return zip_code_geo;
    }
    public void setZip_code_geo(Integer zip_code_geo)
    {
        this.zip_code_geo = zip_code_geo;
    }
    public Integer getMobile_provider_id()
    {
        return mobile_provider_id;
    }
    public void setMobile_provider_id(Integer mobile_provider_id)
    {
        this.mobile_provider_id = mobile_provider_id;
    }
    public Integer getRoaming_type()
    {
        return roaming_type;
    }
    public void setRoaming_type(Integer roaming_type)
    {
        this.roaming_type = roaming_type;
    }
    public UUID getOpen_test_uuid()
    {
        return open_test_uuid;
    }
    public void setOpen_test_uuid(UUID open_test_uuid)
    {
        this.open_test_uuid = open_test_uuid;
    }
    public String getCountry_asn()
    {
        return country_asn;
    }
    public void setCountry_asn(String country_asn)
    {
        this.country_asn = country_asn;
    }
    public String getCountry_location()
    {
        return country_location;
    }
    public void setCountry_location(String country_location)
    {
        this.country_location = country_location;
    }
    public Long getTest_if_bytes_download()
    {
        return test_if_bytes_download;
    }
    public void setTest_if_bytes_download(Long test_if_bytes_download)
    {
        this.test_if_bytes_download = test_if_bytes_download;
    }
    public Long getTest_if_bytes_upload()
    {
        return test_if_bytes_upload;
    }
    public void setTest_if_bytes_upload(Long test_if_bytes_upload)
    {
        this.test_if_bytes_upload = test_if_bytes_upload;
    }
    public boolean isImplausible()
    {
        return implausible;
    }
    public void setImplausible(boolean implausible)
    {
        this.implausible = implausible;
    }
    public Long getTestdl_if_bytes_download()
    {
        return testdl_if_bytes_download;
    }
    public void setTestdl_if_bytes_download(Long testdl_if_bytes_download)
    {
        this.testdl_if_bytes_download = testdl_if_bytes_download;
    }
    public Long getTestdl_if_bytes_upload()
    {
        return testdl_if_bytes_upload;
    }
    public void setTestdl_if_bytes_upload(Long testdl_if_bytes_upload)
    {
        this.testdl_if_bytes_upload = testdl_if_bytes_upload;
    }
    public Long getTestul_if_bytes_download()
    {
        return testul_if_bytes_download;
    }
    public void setTestul_if_bytes_download(Long testul_if_bytes_download)
    {
        this.testul_if_bytes_download = testul_if_bytes_download;
    }
    public Long getTestul_if_bytes_upload()
    {
        return testul_if_bytes_upload;
    }
    public void setTestul_if_bytes_upload(Long testul_if_bytes_upload)
    {
        this.testul_if_bytes_upload = testul_if_bytes_upload;
    }
    public String getCountry_geoip()
    {
        return country_geoip;
    }
    public void setCountry_geoip(String country_geoip)
    {
        this.country_geoip = country_geoip;
    }
    public Integer getLocation_max_distance()
    {
        return location_max_distance;
    }
    public void setLocation_max_distance(Integer location_max_distance)
    {
        this.location_max_distance = location_max_distance;
    }
    public Integer getLocation_max_distance_gps()
    {
        return location_max_distance_gps;
    }
    public void setLocation_max_distance_gps(Integer location_max_distance_gps)
    {
        this.location_max_distance_gps = location_max_distance_gps;
    }
    public String getNetwork_group_name()
    {
        return network_group_name;
    }
    public void setNetwork_group_name(String network_group_name)
    {
        this.network_group_name = network_group_name;
    }
    public String getNetwork_group_type()
    {
        return network_group_type;
    }
    public void setNetwork_group_type(String network_group_type)
    {
        this.network_group_type = network_group_type;
    }
    public Long getTime_dl_ns()
    {
        return time_dl_ns;
    }
    public void setTime_dl_ns(Long time_dl_ns)
    {
        this.time_dl_ns = time_dl_ns;
    }
    public Long getTime_ul_ns()
    {
        return time_ul_ns;
    }
    public void setTime_ul_ns(Long time_ul_ns)
    {
        this.time_ul_ns = time_ul_ns;
    }
    public Integer getNum_threads_ul()
    {
        return num_threads_ul;
    }
    public void setNum_threads_ul(Integer num_threads_ul)
    {
        this.num_threads_ul = num_threads_ul;
    }
    public DateTime getTimestamp()
    {
        return timestamp;
    }
    public void setTimestamp(DateTime timestamp)
    {
        this.timestamp = timestamp;
    }
    public String getSource_ip()
    {
        return source_ip;
    }
    public void setSource_ip(String source_ip)
    {
        this.source_ip = source_ip;
    }
    public Integer getLte_rsrp()
    {
        return lte_rsrp;
    }
    public void setLte_rsrp(Integer lte_rsrp)
    {
        this.lte_rsrp = lte_rsrp;
    }
    public Integer getLte_rsrq()
    {
        return lte_rsrq;
    }
    public void setLte_rsrq(Integer lte_rsrq)
    {
        this.lte_rsrq = lte_rsrq;
    }
    public Integer getMobile_network_id()
    {
        return mobile_network_id;
    }
    public void setMobile_network_id(Integer mobile_network_id)
    {
        this.mobile_network_id = mobile_network_id;
    }
    public Integer getMobile_sim_id()
    {
        return mobile_sim_id;
    }
    public void setMobile_sim_id(Integer mobile_sim_id)
    {
        this.mobile_sim_id = mobile_sim_id;
    }
    public Double getDist_prev()
    {
        return dist_prev;
    }
    public void setDist_prev(Double dist_prev)
    {
        this.dist_prev = dist_prev;
    }
    public Double getSpeed_prev()
    {
        return speed_prev;
    }
    public void setSpeed_prev(Double speed_prev)
    {
        this.speed_prev = speed_prev;
    }
    public String getTag()
    {
        return tag;
    }
    public void setTag(String tag)
    {
        this.tag = tag;
    }
    public Long getPing_median()
    {
        return ping_median;
    }
    public void setPing_median(Long ping_median)
    {
        this.ping_median = ping_median;
    }
    public Double getPing_median_log()
    {
        return ping_median_log;
    }
    public void setPing_median_log(Double ping_median_log)
    {
        this.ping_median_log = ping_median_log;
    }
    public String getSource_ip_anonymized()
    {
        return source_ip_anonymized;
    }
    public void setSource_ip_anonymized(String source_ip_anonymized)
    {
        this.source_ip_anonymized = source_ip_anonymized;
    }
    public String getClient_ip_local()
    {
        return client_ip_local;
    }
    public void setClient_ip_local(String client_ip_local)
    {
        this.client_ip_local = client_ip_local;
    }
    public String getClient_ip_local_anonymized()
    {
        return client_ip_local_anonymized;
    }
    public void setClient_ip_local_anonymized(String client_ip_local_anonymized)
    {
        this.client_ip_local_anonymized = client_ip_local_anonymized;
    }
    public String getClient_ip_local_type()
    {
        return client_ip_local_type;
    }
    public void setClient_ip_local_type(String client_ip_local_type)
    {
        this.client_ip_local_type = client_ip_local_type;
    }
    public String getHidden_code()
    {
        return hidden_code;
    }
    public void setHidden_code(String hidden_code)
    {
        this.hidden_code = hidden_code;
    }
}

