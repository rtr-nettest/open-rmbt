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
package at.alladin.rmbt.statisticServer.export;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.IOUtils;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import at.alladin.rmbt.statisticServer.ServerResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ExportResource extends ServerResource
{
    private static final String FILENAME_CSV_HOURS = "netztest-opendata_hours-%HOURS%.csv";
    private static final String FILENAME_ZIP_HOURS = "netztest-opendata_hours-%HOURS%.zip";
    private static final String FILENAME_CSV = "netztest-opendata-%YEAR%-%MONTH%.csv";
    private static final String FILENAME_ZIP = "netztest-opendata-%YEAR%-%MONTH%.zip";
    private static final String FILENAME_CSV_CURRENT = "opendata.csv";
    private static final String FILENAME_ZIP_CURRENT = "netztest-opendata.zip";
    
    private static final CSVFormat csvFormat = CSVFormat.RFC4180;
    private static final boolean zip = true;
    
    private static long cacheThresholdMs;

    @Get
    public Representation request(final String entity)
    {
        //Before doing anything => check if a cached file already exists and is new enough
        String property = System.getProperty("java.io.tmpdir");
        
    	final String filename_zip;
    	final String filename_csv;
    	
        //allow filtering by month/year
        int year = -1;
        int month = -1;
        int hours = -1;
        boolean hoursExport = false;
        boolean dateExport = false;
        
        if (getRequest().getAttributes().containsKey("hours")) { // export by hours
        	try {
        		hours= Integer.parseInt(getRequest().getAttributes().get("hours").toString());
        	} catch (NumberFormatException ex) {
        		//Nothing -> just fall back
        	}
        	if (hours <= 7*24 && hours >= 1) {  //limit to 1 week (avoid DoS)
        		hoursExport = true;
        	}
        } 
        else if (!hoursExport && getRequest().getAttributes().containsKey("year")) {  // export by month/year 
        	try {
        		year= Integer.parseInt(getRequest().getAttributes().get("year").toString());
        		month = Integer.parseInt(getRequest().getAttributes().get("month").toString());
        	} catch (NumberFormatException ex) {
        		//Nothing -> just fall back
        	}
        	if (year < 2099 && month > 0 && month <= 12 && year > 2000) {
        		dateExport = true;
        	} 
        } 
        
        if (hoursExport) {
        	filename_zip = FILENAME_ZIP_HOURS.replace("%HOURS%", String.format("%03d",hours));
        	filename_csv = FILENAME_CSV_HOURS.replace("%HOURS%", String.format("%03d",hours));
        	cacheThresholdMs = 5*60*1000; //5 minutes
        } else if (dateExport) {
        	filename_zip = FILENAME_ZIP.replace("%YEAR%", Integer.toString(year)).replace("%MONTH%",String.format("%02d",month));
        	filename_csv = FILENAME_CSV.replace("%YEAR%", Integer.toString(year)).replace("%MONTH%",String.format("%02d",month));
        	cacheThresholdMs  = 23*60*60*1000; //23 hours
        } else {	
        	filename_zip = FILENAME_ZIP_CURRENT;
        	filename_csv = FILENAME_CSV_CURRENT;
        	cacheThresholdMs = 3*60*60*1000; //3 hours
        }


        final File cachedFile = new File(property + File.separator + ((zip)?filename_zip:filename_csv));
        final File generatingFile = new File(property + File.separator + ((zip)?filename_zip:filename_csv) + "_tmp");
        if (cachedFile.exists()) {
            
            //check if file has been recently created OR a file is currently being created
            if (((cachedFile.lastModified() + cacheThresholdMs) > (new Date()).getTime()) ||
            		(generatingFile.exists() && (generatingFile.lastModified() + cacheThresholdMs) > (new Date()).getTime())) {

                //if so, return the cached file instead of a cost-intensive new one
                final OutputRepresentation result = new OutputRepresentation(zip ? MediaType.APPLICATION_ZIP
                : MediaType.TEXT_CSV) {

                    @Override
                    public void write(OutputStream out) throws IOException {
                        InputStream is = new FileInputStream(cachedFile);
                        IOUtils.copy(is, out);
                        out.close();
                    }
                    
                };
                if (zip)
                {
                    final Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT);
                    disposition.setFilename(filename_zip);
                    result.setDisposition(disposition);
                }
                return result;
        
            }
        }
        
        final String timeClause;
        
        if (dateExport)
        	timeClause = " AND (EXTRACT (month FROM t.time AT TIME ZONE 'UTC') = " + month + 
        	") AND (EXTRACT (year FROM t.time AT TIME ZONE 'UTC') = " + year + ") ";
        else if (hoursExport)
        	timeClause = " AND time > now() - interval '" + hours + " hours' ";
        else 
        	timeClause = " AND time > current_date - interval '31 days' ";
         
        
        final String sql = "SELECT" +
                " ('P' || t.open_uuid) open_uuid," +
                " ('O' || t.open_test_uuid) open_test_uuid," + 
                " to_char(t.time AT TIME ZONE 'UTC', 'YYYY-MM-DD HH24:MI:SS') time_utc," +
                " nt.group_name cat_technology," +
                " nt.name network_type," +
                " (CASE WHEN (t.geo_accuracy < ?) AND (t.geo_provider != 'manual') AND (t.geo_provider != 'geocoder') THEN" +
                " t.geo_lat" +
                " WHEN (t.geo_accuracy < ?) THEN" +
                " ROUND(t.geo_lat*1111)/1111" +
                " ELSE null" +
                " END) lat," + 
                " (CASE WHEN (t.geo_accuracy < ?) AND (t.geo_provider != 'manual') AND (t.geo_provider != 'geocoder') THEN" +
                " t.geo_long" +
                " WHEN (t.geo_accuracy < ?) THEN" +
                " ROUND(t.geo_long*741)/741 " +
                " ELSE null" +
                " END) long," + 
                " (CASE WHEN ((t.geo_provider = 'manual') OR (t.geo_provider = 'geocoder')) THEN" +
                " 'rastered'" + //make raster transparent
                " ELSE t.geo_provider" +
                " END) loc_src," + 
                " (CASE WHEN (t.geo_accuracy < ?) AND (t.geo_provider != 'manual') AND (t.geo_provider != 'geocoder') " +
                " THEN round(t.geo_accuracy::float * 10)/10 " +
                " WHEN (t.geo_accuracy < 100) AND ((t.geo_provider = 'manual') OR (t.geo_provider = 'geocoder')) THEN 100" + // limit accuracy to 100m
                " WHEN (t.geo_accuracy < ?) THEN round(t.geo_accuracy::float * 10)/10" +
                " ELSE null END) loc_accuracy, " +
                " (CASE WHEN (t.zip_code < 1000 OR t.zip_code > 9999) THEN null ELSE t.zip_code END) zip_code," +
                " t.gkz gkz," +
                " t.country_location country_location," + 
                " t.speed_download download_kbit," +
                " t.speed_upload upload_kbit," +
                " round(t.ping_median::float / 100000)/10 ping_ms," +
                " t.lte_rsrp," +
                " t.lte_rsrq," +
                " ts.name server_name," +
                " duration test_duration," +
                " num_threads," +
                " t.plattform platform," +
                " COALESCE(adm.fullname, t.model) model," +
                " client_software_version client_version," +
                " network_operator network_mcc_mnc," +
                " network_operator_name network_name," +
                " network_sim_operator sim_mcc_mnc," +
                " nat_type," +
                " public_ip_asn asn," +
                " client_public_ip_anonymized ip_anonym," +
                " (ndt.s2cspd*1000)::int ndt_download_kbit," +
                " (ndt.c2sspd*1000)::int ndt_upload_kbit," +
                " COALESCE(t.implausible, false) implausible," +
                " t.signal_strength" +
                " FROM test t" +
                " LEFT JOIN network_type nt ON nt.uid=t.network_type" +
                " LEFT JOIN device_map adm ON adm.codename=t.model" +
                " LEFT JOIN test_server ts ON ts.uid=t.server_id" +
                " LEFT JOIN test_ndt ndt ON t.uid=ndt.test_id" +
                " WHERE " +
                " t.deleted = false" + 
                timeClause +
                " AND status = 'FINISHED'" +
                " ORDER BY t.uid";
        
        final String[] columns;
        final List<String[]> data = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            ps = conn.prepareStatement(sql);
            
            //insert filter for accuracy
            double accuracy = Double.parseDouble(settings.getString("RMBT_GEO_ACCURACY_DETAIL_LIMIT"));
            ps.setDouble(1, accuracy);
            ps.setDouble(2, accuracy);
            ps.setDouble(3, accuracy);
            ps.setDouble(4, accuracy);
            ps.setDouble(5, accuracy);
            ps.setDouble(6, accuracy);
            
            if (!ps.execute())
                return null;
            rs = ps.getResultSet();
            
            final ResultSetMetaData meta = rs.getMetaData();
            final int colCnt = meta.getColumnCount();
            columns = new String[colCnt];
            for (int i = 0; i < colCnt; i++)
                columns[i] = meta.getColumnName(i + 1);
            
            while (rs.next())
            {
                final String[] line = new String[colCnt];
                
                for (int i = 0; i < colCnt; i++)
                {
                    final Object obj = rs.getObject(i + 1);
                    line[i] = obj == null ? null : obj.toString();
                }
                
                data.add(line);
            }
        }
        catch (final SQLException e)
        {
            e.printStackTrace();
            return null;
        }
        finally
        {
            try
            {
                if (rs != null)
                    rs.close();
                if (ps != null)
                    ps.close();
            }
            catch (final SQLException e)
            {
                e.printStackTrace();
            }
        }
        
        final OutputRepresentation result = new OutputRepresentation(zip ? MediaType.APPLICATION_ZIP
                : MediaType.TEXT_CSV)
        {
            @Override
            public void write(OutputStream out) throws IOException
            {
                //cache in file => create temporary temporary file (to 
                // handle errors while fulfilling a request)
                String property = System.getProperty("java.io.tmpdir");
                final File cachedFile = new File(property + File.separator + ((zip)?filename_zip:filename_csv) + "_tmp");
                OutputStream outf = new FileOutputStream(cachedFile);
                
                if (zip)
                {
                    final ZipOutputStream zos = new ZipOutputStream(outf);
                    final ZipEntry zeLicense = new ZipEntry("LIZENZ.txt");
                    zos.putNextEntry(zeLicense);
                    final InputStream licenseIS = getClass().getResourceAsStream("DATA_LICENSE.txt");
                    IOUtils.copy(licenseIS, zos);
                    licenseIS.close();
                    
                    final ZipEntry zeCsv = new ZipEntry(filename_csv);
                    zos.putNextEntry(zeCsv);
                    outf = zos;
                }
                
                final OutputStreamWriter osw = new OutputStreamWriter(outf);
                final CSVPrinter csvPrinter = new CSVPrinter(osw, csvFormat);
                
                for (final String c : columns)
                    csvPrinter.print(c);
                csvPrinter.println();
                
                for (final String[] line : data)
                {
                    for (final String f : line)
                        csvPrinter.print(f);
                    csvPrinter.println();
                }
                csvPrinter.flush();
                
                if (zip)
                    outf.close();
                
                //if we reach this code, the data is now cached in a temporary tmp-file
                //so, rename the file for "production use2
                //concurrency issues should be solved by the operating system
                File newCacheFile = new File(property + File.separator + ((zip)?filename_zip:filename_csv));
                Files.move(cachedFile.toPath(), newCacheFile.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                
                FileInputStream fis = new FileInputStream(newCacheFile);
                IOUtils.copy(fis, out);
                fis.close();
                out.close();
            }
        };
        if (zip)
        {
            final Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT);
            disposition.setFilename(filename_zip);
            result.setDisposition(disposition);
        }
        
        return result;
    }
}
