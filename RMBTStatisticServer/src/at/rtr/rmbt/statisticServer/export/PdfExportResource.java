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
import org.restlet.data.Disposition;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

public class PdfExportResource extends ServerResource {
    public static final String FILENAME_PDF = "testergebnis.pdf";


    @Post
    @Get
    public Representation retrieve(final String entity) {
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

        final List<String> invalidElements = qp.parseQuery(getParameters);

        OpenTestDAO dao = new OpenTestDAO(conn, settings, capabilities);
        OpenTestSearchDTO searchResult = dao.getOpenTestSearchResults(qp, 0, 10, new HashSet<String>());

        Map<String, Object> data = new HashMap<>();
        data.put("date",new SimpleDateFormat("d.M.yyyy H:mm:ss", Locale.GERMAN).format(new Date()));
        data.put("tests", searchResult.getResults());

        if (qp.getWhereParams().containsKey("loop_uuid")) {
            data.put("loop_uuid",qp.getWhereParams().get("loop_uuid").get(0).getValue());
        }

        //get details for single results - set more detailled info
        ListIterator<OpenTestDTO> testIterator = searchResult.getResults().listIterator();
        while (testIterator.hasNext()) {
            OpenTestDTO result = testIterator.next();
            Logger.getAnonymousLogger().info("Gathering test: " + result.getOpenTestUuid());
            OpenTestDetailsDTO singleTest = dao.getSingleOpenTestDetails(result.getOpenTestUuid(), 0);
            testIterator.set(singleTest);
        }


        String fullTemplate;
        try {
            Context context = Context
                    .newBuilder(data)
                    .push(new ExtendedJavaBeanResolver())
                    .build();
            fullTemplate = template.apply(context);
            fullTemplate = fullTemplate.replace("<script type=\"text/x-handlebars\" id=\"template\">", "");

            //create temp file
            Path htmlFile = Files.createTempFile("nt", ".pdf.html");
            Files.write(htmlFile, fullTemplate.getBytes("utf-8"));
            Logger.getLogger(PdfExportResource.class.getName()).info("Generating PDF from: " + htmlFile);

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
            Logger.getLogger(PdfExportResource.class.getName()).info("PDF generated: " + pdfTarget);

            FileRepresentation ret = new FileRepresentation(pdfTarget.toFile(), MediaType.APPLICATION_PDF);
            Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT);
            disposition.setFilename(FILENAME_PDF);
            ret.setDisposition(disposition);
            return ret;
        } catch (IOException e) {
            e.printStackTrace();
            return new StringRepresentation("error in template application");
        }
    }




    //need a translator to "understand" jackson annotations and translate underscore to camelcase
    public static class ExtendedJavaBeanResolver extends JavaBeanValueResolver {
        public ExtendedJavaBeanResolver() {
            super();
        }

        /**
         * Is invoked to check if the methode actually is a getter or setter
         * @param method
         * @param name
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

        /**
         * Get the property name for a given method
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
    };

    public interface PdfConverter {
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
                Logger.getLogger(PdfExportResource.class.getName()).info("PDF generation with Prince finished");
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
                Logger.getLogger(PdfExportResource.class.getName()).info("PDF generation with weasyprint finished");
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
        }
    }
}
