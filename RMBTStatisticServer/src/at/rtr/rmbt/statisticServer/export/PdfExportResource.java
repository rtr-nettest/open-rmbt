package at.rtr.rmbt.statisticServer.export;

import at.rtr.rmbt.shared.ExtendedHandlebars;
import at.rtr.rmbt.statisticServer.ServerResource;
import at.rtr.rmbt.statisticServer.opendata.QueryParser;
import at.rtr.rmbt.statisticServer.opendata.dao.OpenTestDAO;
import at.rtr.rmbt.statisticServer.opendata.dto.OpenTestDTO;
import at.rtr.rmbt.statisticServer.opendata.dto.OpenTestDetailsDTO;
import at.rtr.rmbt.statisticServer.opendata.dto.OpenTestSearchDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.google.common.base.CaseFormat;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.restlet.data.*;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import javax.ws.rs.GET;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

@Api(value="/export/pdf")
public class PdfExportResource extends ServerResource {
    public static final String FILENAME_PDF = "testergebnis.pdf";

    public static final int MAX_RESULTS = 1000; //max results for pdf

    @Post
    @Get
    @GET
    @javax.ws.rs.Path("/export/pdf")
    @ApiOperation(httpMethod = "GET",
            value = "Export open data as PDF",
            produces = "application/pdf",
            nickname = "exportPdf")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "open_test_uuid", value = "The UUID of the test.", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "loop_uuid", value = "The loop UUID of a single loop test", dataType = "string", paramType = "query")
    })
    public Representation request(final String entity) {
        addAllowOrigin();
        final Form getParameters;
        if (getRequest().getMethod().equals(Method.POST)) {
            // HTTP POST
            getParameters = new Form(entity);
        }
        else {
            // HTTP GET
            getParameters = getRequest().getResourceRef().getQueryAsForm();
        }

        //load template
        Handlebars handlebars = new ExtendedHandlebars();
        Template template = null;
        try {
            String html = Resources.toString(getClass().getClassLoader().getResource("at/rtr/rmbt/res/export_de.hbs.html"), Charsets.UTF_8);
            template = handlebars.compileInline(html);
        } catch (IOException e) {
            e.printStackTrace();
        }

        final QueryParser qp = new QueryParser();

        //parse the input query
        final List<String> invalidElements = qp.parseQuery(getParameters);

        //only accept open_test_uuid and loop_uuid as input parameters
        if (qp.getWhereParams().size() == 1 && (!qp.getWhereParams().containsKey("open_test_uuid") &&  !qp.getWhereParams().containsKey("loop_uuid"))) {
            System.out.println(qp.getWhereParams().keySet());
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return new StringRepresentation("submit open_test_uuid or loop_uuid");
        }

        OpenTestDAO dao = new OpenTestDAO(conn, settings, capabilities);
        OpenTestSearchDTO searchResult = dao.getOpenTestSearchResults(qp, 0, MAX_RESULTS, new HashSet<String>());

        Map<String, Object> data = new HashMap<>();
        data.put("date",new SimpleDateFormat("d.M.yyyy H:mm:ss", Locale.GERMAN).format(new Date()));
        data.put("tests", searchResult.getResults());

        //if the loop uuid is given - add this to the inputs
        if (qp.getWhereParams().containsKey("loop_uuid")) {
            data.put("loop_uuid",qp.getWhereParams().get("loop_uuid").get(0).getValue());
        }

        //if no measurements - don't generate the application
        if (searchResult.getResults() == null || searchResult.getResults().isEmpty()) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return new EmptyRepresentation();
        }

        //get details for single results - set more detailled info
        Logger.getLogger(PdfExportResource.class.getName()).fine("Gathering extended test results");
        ListIterator<OpenTestDTO> testIterator = searchResult.getResults().listIterator();
        while (testIterator.hasNext()) {
            OpenTestDTO result = testIterator.next();
            OpenTestDetailsDTO singleTest = dao.getSingleOpenTestDetails(result.getOpenTestUuid(), 0);
            testIterator.set(singleTest);
        }


        String fullTemplate;
        try {
            Context context = Context
                    .newBuilder(data)
                    .push(new JacksonAwareSnakeCaseJavaBeanResolver())
                    .build();
            fullTemplate = template.apply(context);
            fullTemplate = fullTemplate.replace("<script type=\"text/x-handlebars\" id=\"template\">", "");

            //create temp file
            Path htmlFile = Files.createTempFile("nt", ".pdf.html");
            Files.write(htmlFile, fullTemplate.getBytes("utf-8"));
            Logger.getLogger(PdfExportResource.class.getName()).fine("Generating PDF from: " + htmlFile);

            Path pdfTarget = Files.createTempFile(htmlFile.getFileName().toString(),".pdf");
            PdfConverter pdfConverter;
            switch (settings.getString("PDF_CONVERTER")) {
                case "weasyprint":
                    pdfConverter = new WeasyprintPdfConverter(settings.getString("WEASYPRINT_PATH"));
                    break;
                case "prince":
                    pdfConverter = new PrincePdfConverter((settings.getString("PRINCE_PATH")));
                    break;
                default:
                    throw new RuntimeException("invalid pdfgenerator");
            }

            pdfConverter.convertHtml(htmlFile,pdfTarget);
            Logger.getLogger(PdfExportResource.class.getName()).fine("PDF generated: " + pdfTarget);

            FileRepresentation ret = new FileRepresentation(pdfTarget.toFile(), MediaType.APPLICATION_PDF);
            Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT);
            disposition.setFilename(FILENAME_PDF);
            ret.setDisposition(disposition);
            return ret;
        } catch (IOException e) {
            e.printStackTrace();
            setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
            return new EmptyRepresentation();
        }
    }


    /**
     * Handlebars resolver that
     *   (i) utilized Jackson @JsonProperty annotations
     *   (ii) translates JavaBeans with camelCase to snake_case for value lookup
     */
    public static class JacksonAwareSnakeCaseJavaBeanResolver extends JavaBeanValueResolver {
        public JacksonAwareSnakeCaseJavaBeanResolver() {
            super();
        }

        /**
         * Get the property name for a given method,
         *  either from the @JsonProperty annotation, or from translating to snake_case
         * @param member
         * @return
         */
        @Override
        protected String memberName(final java.lang.reflect.Method member) {
            if (member.getDeclaringClass().isInstance(new HashMap<>())) {
                return super.memberName(member);
            }

            JsonProperty annotation = member.getAnnotation(JsonProperty.class);
            if (annotation != null) {
                return annotation.value();
            }

            String withoutGetterIs = super.memberName(member);

            String otherName = CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE).convert(withoutGetterIs);
            return otherName;
        }

        /**
         * Is invoked to check if the methode actually is a getter or setter
         * @param method Method to check
         * @param name Translated name of the method (already being snake_case)
         * @return
         */
        @Override
        public boolean matches(final java.lang.reflect.Method method, final String name) {
            if (method.getDeclaringClass().isInstance(new HashMap<>())) {
                return super.matches(method, name);
            }

            //if it matches the annotation - it matches
            JsonProperty annotation = method.getAnnotation(JsonProperty.class);
            if (annotation != null && name.equals(annotation.value())) {
                return true;
            }

            //name is here the "translated" name - translate back to get if it matches
            String otherName = CaseFormat.LOWER_UNDERSCORE.converterTo(CaseFormat.LOWER_CAMEL).convert(name);
            return super.matches(method, otherName) || super.matches(method, name);
        }
    };

    public interface PdfConverter {
        /**
         * Convert the given HTML source file to given target pdf
         * @param htmlSource
         * @param pdfTarget
         * @throws IOException
         */
        void convertHtml(Path htmlSource, Path pdfTarget) throws IOException;
    }

    public class PrincePdfConverter implements PdfConverter {
        private final String path;

        public PrincePdfConverter(String path) {
            this.path = path;
        }

        @Override
        public void convertHtml(Path htmlSource, Path pdfTarget) throws IOException {
            String princePath = path;
            ProcessBuilder princeProcessBuilder = new ProcessBuilder(princePath,
                    htmlSource.toAbsolutePath().toString(),
                    "-o",
                    pdfTarget.toAbsolutePath().toString());
            Process princeProcess = princeProcessBuilder.start();
            try {
                princeProcess.waitFor();
                Logger.getLogger(PdfExportResource.class.getName()).fine("PDF generation with Prince finished");
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
        }
    }

    public class WeasyprintPdfConverter implements PdfConverter {
        private final String path;

        public WeasyprintPdfConverter(String path) {
            this.path = path;
        }

        @Override
        public void convertHtml(Path htmlSource, Path pdfTarget) throws IOException {
            String weasyPath = path;
            ProcessBuilder weasyProcessBuilder = new ProcessBuilder(weasyPath,
                    htmlSource.toAbsolutePath().toString(),
                    pdfTarget.toAbsolutePath().toString());
            Process weasyProcess = weasyProcessBuilder.start();
            try {
                weasyProcess.waitFor();
                Logger.getLogger(PdfExportResource.class.getName()).fine("PDF generation with weasyprint finished");
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
        }
    }
}
