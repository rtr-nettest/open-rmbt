/*******************************************************************************
 * Copyright 2013-2015 alladin-IT GmbH
 * Copyright 2013-2015 Rundfunk und Telekom Regulierungs-GmbH (RTR-GmbH)
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
package at.rtr.rmbt.statisticServer.export;

import at.rtr.rmbt.statisticServer.ServerResource;
import at.rtr.rmbt.statisticServer.opendata.dao.OpenTestDAO;
import at.rtr.rmbt.statisticServer.opendata.dto.OpenTestExportDTO;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.github.sett4.dataformat.xlsx.XlsxMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Api(value="/export")
public class ExportResource extends ServerResource
{
    private static final String FILENAME_CSV_HOURS = "netztest-opendata_hours-%HOURS%.csv";
    private static final String FILENAME_ZIP_HOURS = "netztest-opendata_hours-%HOURS%.zip";
    private static final String FILENAME_XLSX_HOURS = "netztest-opendata_hours-%HOURS%.xlsx";
    private static final String FILENAME_CSV = "netztest-opendata-%YEAR%-%MONTH%.csv";
    private static final String FILENAME_XLSX = "netztest-opendata-%YEAR%-%MONTH%.xlsx";
    private static final String FILENAME_ZIP = "netztest-opendata-%YEAR%-%MONTH%.zip";
    private static final String FILENAME_CSV_CURRENT = "netztest-opendata.csv";
    private static final String FILENAME_ZIP_CURRENT = "netztest-opendata.zip";
    private static final String FILENAME_XLSX_CURRENT = "netztest-opendata.xlsx";

    private static final boolean zip = true;
    
    private static long cacheThresholdMs;

    @Get
    @GET
    @Path("/export/netztest-opendata-{year}-{month}.{format}")
    @ApiOperation(httpMethod = "GET",
            value = "Export open data as CSV or XLSX",
            notes = "Bulk export open data entries",
            response = OpenTestExportDTO.class,
            produces = "text/csv",
            nickname = "export")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "year", value = "Mandatory. The year that should be exported.", dataType = "string", example = "2017", paramType = "path", required = true),
            @ApiImplicitParam(name = "month", value = "Mandatory. The year that should be exported.", dataType = "integer", example = "0", paramType = "path", required = true),
            @ApiImplicitParam(name = "format", value = "Mandatory. Either ZIP (CSV) or XLSX.", dataType = "string", example = "xlsx", paramType = "path", required = true)
    })
    public Representation request(final String entity)
    {
        //Before doing anything => check if a cached file already exists and is new enough
        String property = System.getProperty("java.io.tmpdir");
        
    	final String filename_zip;
    	final String filename_csv;
    	final String filename_xlsx;
    	
        //allow filtering by month/year
        int year = -1;
        int month = -1;
        int hours = -1;
        boolean hoursExport = false;
        boolean dateExport = false;

        String tFormat = "csv";
        if (getRequest().getAttributes().containsKey("format")) {
            tFormat = getRequest().getAttributes().get("format").toString();
        }
        final boolean xlsx = tFormat.contains("xlsx");
        
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
        	filename_xlsx = FILENAME_XLSX_HOURS.replace("%HOURS%", String.format("%03d",hours));
        	cacheThresholdMs = 5*60*1000; //5 minutes
        } else if (dateExport) {
        	filename_zip = FILENAME_ZIP.replace("%YEAR%", Integer.toString(year)).replace("%MONTH%",String.format("%02d",month));
        	filename_csv = FILENAME_CSV.replace("%YEAR%", Integer.toString(year)).replace("%MONTH%",String.format("%02d",month));
        	filename_xlsx = FILENAME_XLSX.replace("%YEAR%", Integer.toString(year)).replace("%MONTH%",String.format("%02d",month));
        	cacheThresholdMs  = 23*60*60*1000; //23 hours
        } else {	
        	filename_zip = FILENAME_ZIP_CURRENT;
        	filename_csv = FILENAME_CSV_CURRENT;
        	filename_xlsx = FILENAME_XLSX_CURRENT;
        	cacheThresholdMs = 3*60*60*1000; //3 hours
        }
        final String filename = ((xlsx)?filename_xlsx:(zip)?filename_zip:filename_csv);

        final File cachedFile = new File(property + File.separator + filename);
        final File generatingFile = new File(property + File.separator + filename + "_tmp");
        if (cachedFile.exists()) {
            
            //check if file has been recently created OR a file is currently being created
            if (((cachedFile.lastModified() + cacheThresholdMs) > (new Date()).getTime()) ||
            		(generatingFile.exists() && (generatingFile.lastModified() + cacheThresholdMs) > (new Date()).getTime())) {

                //if so, return the cached file instead of a cost-intensive new one
                final OutputRepresentation result = new OutputRepresentation(xlsx ? MediaType.APPLICATION_MSOFFICE_XLSX : zip ? MediaType.APPLICATION_ZIP
                : MediaType.TEXT_CSV) {

                    @Override
                    public void write(OutputStream out) throws IOException {
                        InputStream is = new FileInputStream(cachedFile);
                        IOUtils.copy(is, out);
                        out.close();
                    }
                    
                };
                if (xlsx || zip) {
                    final Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT);
                    disposition.setFilename(filename);
                    result.setDisposition(disposition);
                }
                return result;
        
            }
        }

        OpenTestDAO openTestDAO = new OpenTestDAO(conn,settings,capabilities);
        final List<OpenTestExportDTO> results = openTestDAO.getOpenTestExport(hoursExport, dateExport, year, month, hours);
        
        final OutputRepresentation result = new OutputRepresentation(xlsx ? MediaType.APPLICATION_MSOFFICE_XLSX : zip ? MediaType.APPLICATION_ZIP
                : MediaType.TEXT_CSV)
        {
            @Override
            public void write(OutputStream out) throws IOException
            {
                //cache in file => create temporary temporary file (to 
                // handle errors while fulfilling a request)
                String property = System.getProperty("java.io.tmpdir");
                final File cachedFile = new File(property + File.separator + filename + "_tmp");
                OutputStream outf = new FileOutputStream(cachedFile);
                
                if (zip && !xlsx)
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



                if (xlsx) {
                    XlsxMapper mapper = new XlsxMapper();
                    mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
                    CsvSchema schema = mapper.schemaFor(OpenTestExportDTO.class).withHeader();
                    SequenceWriter sequenceWriter = mapper.writer(schema).writeValues(outf);
                    sequenceWriter.writeAll(results);
                    sequenceWriter.flush();
                    sequenceWriter.close();
                }
                else {
                    final CsvMapper cm = new CsvMapper();
                    final CsvSchema schema;
                    cm.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
                    cm.enable(CsvGenerator.Feature.STRICT_CHECK_FOR_QUOTING);
                    schema = CsvSchema.builder().setLineSeparator("\r\n").setUseHeader(true)
                            .addColumnsFrom(cm.schemaFor(OpenTestExportDTO.class)).build();
                    cm.writer(schema).writeValue(outf, results);
                }
                
                if (zip)
                    outf.close();
                
                //if we reach this code, the data is now cached in a temporary tmp-file
                //so, rename the file for "production use2
                //concurrency issues should be solved by the operating system
                File newCacheFile = new File(property + File.separator + filename);
                Files.move(cachedFile.toPath(), newCacheFile.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                
                FileInputStream fis = new FileInputStream(newCacheFile);
                IOUtils.copy(fis, out);
                fis.close();
                out.close();
            }
        };
        if (xlsx || zip) {
            final Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT);
            disposition.setFilename(filename);
            result.setDisposition(disposition);
        }
        
        return result;
    }
}
