package at.rtr.rmbt.statisticServer.export;

import at.rtr.rmbt.shared.ExtendedHandlebars;
import at.rtr.rmbt.shared.ResourceManager;
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
import com.google.common.base.Strings;
import com.google.common.io.Resources;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.restlet.data.*;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.*;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import javax.imageio.ImageIO;
import javax.ws.rs.GET;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

@Api(value="/export/pdf")
public class PdfExportResource extends ServerResource {
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
    public Representation request(final Representation entity) throws IOException {
        addAllowOrigin();

        //load locale, if possible
        String language = settings.getString("RMBT_DEFAULT_LANGUAGE");
        if (getRequest().getAttributes().containsKey("lang")) {
            language = getRequest().getAttributes().get("lang").toString();
            final List<String> langs = Arrays.asList(settings.getString("RMBT_SUPPORTED_LANGUAGES").split(",\\s*"));

            if (langs.contains(language)) {
                labels = ResourceManager.getSysMsgBundle(new Locale(language));
            }
        }
        final String pdfFilename = labels.getString("RESULT_PDF_FILENAME");

        String tempPath = settings.getString("PDF_TEMP_PATH");
        //allow only fetching files
        if (getRequest().getAttributes().containsKey("filename")) {
            String filename = getRequest().getAttributes().get("filename").toString();
            File retFile = new File(tempPath + filename + ".pdf");

            if (!retFile.exists()) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return null;
            }
            ByteArrayRepresentation ret = new ByteArrayRepresentation(Files.readAllBytes(retFile.toPath()), MediaType.APPLICATION_PDF);
            Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT);
            disposition.setFilename(pdfFilename);
            ret.setDisposition(disposition);
            return ret;
        }

        final Form getParameters;
        final Map<String, List<String>> multivalueParams = new HashMap<>();
        if (getRequest().getMethod().equals(Method.POST)) {
            // HTTP POST

            //handle multipart forms
            if (MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(), true)) {
                getParameters = new Form();

                // 1. Create a factory for disk-based file items
                DiskFileItemFactory factory = new DiskFileItemFactory();
                factory.setSizeThreshold(10 * 1024 * 1024);

                // 2. Create a new file upload handler
                RestletFileUpload upload = new RestletFileUpload(factory);
                List<FileItem> items;

                try {
                    items = upload.parseRequest(getRequest());
                    for (FileItem item : items) {
                        if (item.isFormField() && item.getFieldName() != null && !Strings.isNullOrEmpty(item.getString("utf-8"))) {
                            getParameters.set(item.getFieldName(), item.getString("utf-8"));
                        }
                        else if (!item.isFormField() && item.getFieldName() != null && item.getInputStream() != null && item.getSize() > 0){
                            //it is really a file - parse it, add it as base64 input
                            String contentType = item.getContentType();
                            byte[] bytes = IOUtils.toByteArray(item.getInputStream());
                            String base64Str = Base64.encodeBase64String(bytes);
                            String dataUri = "data:" + contentType + ";base64," + base64Str;

                            if (item.getFieldName().endsWith("[]")) {
                                String fieldName = item.getFieldName().replaceAll("\\[\\]","");
                                if (!multivalueParams.containsKey(fieldName)) {
                                    multivalueParams.put(fieldName, new LinkedList<String>());
                                }
                                multivalueParams.get(fieldName).add(dataUri);
                            }
                            else {
                                getParameters.set(item.getFieldName(), dataUri);
                            }
                        }
                    }
                } catch (FileUploadException e) {
                    e.printStackTrace();
                }
            }
            else {
                getParameters = new Form(entity);
            }

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
        if (qp.getWhereParams().size() < 1 ||
                (qp.getWhereParams().size() == 1 && (!qp.getWhereParams().containsKey("open_test_uuid") &&
                !qp.getWhereParams().containsKey("test_uuid") &&
                !qp.getWhereParams().containsKey("loop_uuid")))) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return new StringRepresentation("submit open_test_uuid or loop_uuid");
        }

        OpenTestDAO dao = new OpenTestDAO(conn, settings, capabilities);
        OpenTestSearchDTO searchResult = dao.getOpenTestSearchResults(qp, 0, MAX_RESULTS, new HashSet<String>());

        Map<String, Object> data = new HashMap<>();
        data.put("date",new SimpleDateFormat("d.M.yyyy H:mm:ss", Locale.GERMAN).format(new Date()));
        data.put("tests", searchResult.getResults());

        //add all params to the model
        data.putAll(getParameters.getValuesMap());
        data.putAll(multivalueParams);

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

        //add further parameters, i.e. logos
        InputStream resourceAsStream = getClass().getResourceAsStream("logo.png");
        if (resourceAsStream != null) {
            try {
                BufferedImage img2 = ImageIO.read(resourceAsStream);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(img2, "png", os);
                os.flush();
                String imageAsBase64 = Base64.encodeBase64String(os.toByteArray());
                data.put("logo", imageAsBase64);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //add translation files
        if (labels != null) {
            Map<String, String> labelsMap = new HashMap<>();
            Set<String> keys = labels.keySet();
            for (String key : keys) {
                if (key.startsWith("key_")) {
                    labelsMap.put(key.substring(4), labels.getString(key));
                }
            }
            labelsMap.put("lang", language);
            data.put("Lang", labelsMap);
        }

        String fullTemplate;
        try {
            Context context = Context
                    .newBuilder(data)
                    .push(new JacksonAwareSnakeCaseJavaBeanResolver())
                    .build();
            fullTemplate = template.apply(context);
            fullTemplate = fullTemplate.replace("<script type=\"text/x-handlebars\" id=\"template\">", "");

            String uuid = UUID.randomUUID().toString();
            //create temp file
            Path htmlFile = Files.createTempFile("nt" + uuid, ".pdf.html");
            Files.write(htmlFile, fullTemplate.getBytes("utf-8"));
            Logger.getLogger(PdfExportResource.class.getName()).fine("Generating PDF from: " + htmlFile);

            Path pdfTarget = new File(tempPath + uuid + ".pdf").toPath();

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

            //depending on Accepts-Header, return file or json with link to file
            if (getClientInfo().getAcceptedMediaTypes().size() > 0 &&
                    getClientInfo().getAcceptedMediaTypes().get(0).getMetadata() == MediaType.APPLICATION_JSON) {

                JSONObject retJson = new JSONObject();
                retJson.put("file", uuid + ".pdf");

                return new JsonRepresentation(retJson.toString());
            }
            else {
                FileRepresentation ret = new FileRepresentation(pdfTarget.toFile(), MediaType.APPLICATION_PDF);
                Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT);
                disposition.setFilename(pdfFilename);
                ret.setDisposition(disposition);
                return ret;
            }
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
