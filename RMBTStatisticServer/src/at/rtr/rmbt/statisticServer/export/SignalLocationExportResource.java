package at.rtr.rmbt.statisticServer.export;

import at.rtr.rmbt.statisticServer.ServerResource;
import at.rtr.rmbt.statisticServer.opendata.dao.OpenTestDAO;
import at.rtr.rmbt.statisticServer.opendata.dto.OpenTestExportDTO;
import at.rtr.rmbt.statisticServer.opendata.dto.SignalLocationDTO;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.github.sett4.dataformat.xlsx.XlsxMapper;
import io.swagger.annotations.Api;
import org.apache.commons.io.IOUtils;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import javax.ws.rs.GET;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Api(value="/export/signal")
public class SignalLocationExportResource  extends ServerResource {
    private static final String FILENAME_CSV_HOURS = "netztest-signal-opendata_hours-%HOURS%.csv";
    private static final String FILENAME_ZIP_HOURS = "netztest-signal-opendata_hours-%HOURS%.zip";
    private static final String FILENAME_XLSX_HOURS = "netztest-signal-opendata_hours-%HOURS%.xlsx";
    private static final String FILENAME_CSV = "netztest-signal-opendata-%YEAR%-%MONTH%.csv";
    private static final String FILENAME_XLSX = "netztest-signal-opendata-%YEAR%-%MONTH%.xlsx";
    private static final String FILENAME_ZIP = "netztest-signal-opendata-%YEAR%-%MONTH%.zip";
    private static final String FILENAME_CSV_CURRENT = "netztest-signal-opendata.csv";
    private static final String FILENAME_ZIP_CURRENT = "netztest-signal-opendata.zip";
    private static final String FILENAME_XLSX_CURRENT = "netztest-signal-opendata.xlsx";

    private static long cacheThresholdMs;

    @Get
    @GET
    public Representation request(final String entity) throws SQLException {
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

        final String filename_zip;
        final String filename_csv;
        final String filename_xlsx;

        String tFormat = "csv";
        if (getRequest().getAttributes().containsKey("format")) {
            tFormat = getRequest().getAttributes().get("format").toString();
        }
        final boolean xlsx = tFormat.contains("xlsx");

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
        final String filename = ((xlsx)?filename_xlsx:filename_zip);

        OpenTestDAO openTestDAO = new OpenTestDAO(conn,settings,capabilities);
        final List<SignalLocationDTO> results = openTestDAO.getSignalLocationExport(hoursExport, dateExport, year, month, hours);

        final OutputRepresentation result = new OutputRepresentation(xlsx ? MediaType.APPLICATION_MSOFFICE_XLSX : MediaType.APPLICATION_ZIP)
        {
            @Override
            public void write(OutputStream out) throws IOException
            {
                //cache in file => create temporary temporary file (to
                // handle errors while fulfilling a request)
                String property = System.getProperty("java.io.tmpdir");
                final File cachedFile = new File(property + File.separator + filename + "_tmp");
                OutputStream outf = new FileOutputStream(cachedFile);

                if (!xlsx)
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
                    CsvSchema schema = mapper.schemaFor(SignalLocationDTO.class).withHeader();
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
                            .addColumnsFrom(cm.schemaFor(SignalLocationDTO.class)).build();
                    cm.writer(schema).writeValue(outf, results);
                }

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
        if (xlsx) {
            final Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT);
            disposition.setFilename(filename);
            result.setDisposition(disposition);
        }

        return result;
    }
}
