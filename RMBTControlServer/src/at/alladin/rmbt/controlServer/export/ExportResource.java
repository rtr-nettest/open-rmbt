/*******************************************************************************
 * Copyright 2013 alladin-IT OG
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
package at.alladin.rmbt.controlServer.export;

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

import at.alladin.rmbt.controlServer.ServerResource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ExportResource extends ServerResource
{
    private static final String FILENAME_CSV = "opendata.csv";
    private static final String FILENAME_ZIP = "netztest-opendata.zip";
    
    private static final CSVFormat csvFormat = CSVFormat.RFC4180;
    private static final boolean zip = true;
    
    private static final long cacheThresholdMs = 60*60*1000; //1 hour

    @Get
    public Representation request(final String entity)
    {
        //Before doing anything => check if a cached file already exists and is new enough
        String property = System.getProperty("java.io.tmpdir");
        final File cachedFile = new File(property + File.separator + ((zip)?FILENAME_ZIP:FILENAME_CSV));
        final File generatingFile = new File(property + File.separator + ((zip)?FILENAME_ZIP:FILENAME_CSV) + "_tmp");
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
                    disposition.setFilename(FILENAME_ZIP);
                    result.setDisposition(disposition);
                }
                return result;
        
            }
        }
        
        final String sql = "SELECT" +
                " ('P' || t.open_uuid) open_uuid," +
                " ('O' || t.open_test_uuid) open_test_uuid," + 
                " to_char(t.time AT TIME ZONE 'UTC', 'YYYY-MM-DD HH24:MI') \"time\"," +
                " nt.group_name cat_technology," +
                " nt.name network_type," +
                " t.geo_lat lat," +
                " t.geo_long long," +
                " t.geo_provider loc_src," +
                " t.zip_code," +
                " t.speed_download download_kbit," +
                " t.speed_upload upload_kbit," +
                " (t.ping_shortest::float / 1000000) ping_ms," +
                " t.signal_strength," +
                " ts.name server_name," +
                " duration test_duration," +
                " num_threads," +
                " plattform," +
                " COALESCE(adm.fullname, t.model) model," +
                " client_software_version client_version," +
                " network_operator network_mcc_mnc," +
                " network_operator_name network_name," +
                " network_sim_operator sim_mcc_mnc," +
                " nat_type \"connection\"," +
                " public_ip_asn asn," +
                " client_public_ip_anonymized ip_anonym," +
                " (ndt.s2cspd*1000)::int ndt_download_kbit," +
                " (ndt.c2sspd*1000)::int ndt_upload_kbit" +
                " FROM test t" +
                " LEFT JOIN network_type nt ON nt.uid=t.network_type" +
                " LEFT JOIN android_device_map adm ON adm.codename=t.model" +
                " LEFT JOIN test_server ts ON ts.uid=t.server_id" +
                " LEFT JOIN test_ndt ndt ON t.uid=ndt.test_id" +
                " WHERE " +
                " t.deleted = false" +
                " AND time > '2012-12-22 01:00'" +
                " AND status = 'FINISHED'" +
                " ORDER BY t.uid";
        
        final String[] columns;
        final List<String[]> data = new ArrayList<String[]>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            ps = conn.prepareStatement(sql);
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
                final File cachedFile = new File(property + File.separator + ((zip)?FILENAME_ZIP:FILENAME_CSV) + "_tmp");
                OutputStream outf = new FileOutputStream(cachedFile);
                
                if (zip)
                {
                    final ZipOutputStream zos = new ZipOutputStream(outf);
                    final ZipEntry zeLicense = new ZipEntry("LIZENZ.txt");
                    zos.putNextEntry(zeLicense);
                    final InputStream licenseIS = getClass().getResourceAsStream("DATA_LICENSE.txt");
                    IOUtils.copy(licenseIS, zos);
                    licenseIS.close();
                    
                    final ZipEntry zeCsv = new ZipEntry(FILENAME_CSV);
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
                File newCacheFile = new File(property + File.separator + ((zip)?FILENAME_ZIP:FILENAME_CSV));
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
            disposition.setFilename(FILENAME_ZIP);
            result.setDisposition(disposition);
        }
        
        return result;
    }
}
