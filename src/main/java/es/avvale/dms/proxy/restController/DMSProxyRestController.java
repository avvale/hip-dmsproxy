package es.avvale.dms.proxy.restController;

import es.avvale.dms.proxy.facade.DMSProxyFacade;
import es.avvale.dms.proxy.restClient.DMSConnection;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.OutputStream;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping(value = "/download")
@Log4j2
public class DMSProxyRestController {

    @Autowired
    private DMSProxyFacade dmsProxyFacade;


    public static final String ATTACHMENT_FILE_NAME = "attachment;filename=";


    @GetMapping("/{objectId}")
    public void download(@PathVariable(value = "objectId", required = true) String objectId, Locale locale,
                         HttpServletResponse response, HttpServletRequest request)
            throws Exception {
        OutputStream out = response.getOutputStream();
        try {
            Map<String,Object> map = dmsProxyFacade.downloadPdf(objectId);
            byte[] pdf = (byte[]) map.get("file");
            String filename =  map.get("filename").toString();
            out = response.getOutputStream();
            response.setContentType(MediaType.APPLICATION_PDF_VALUE);
            response.addHeader(HttpHeaders.CONTENT_DISPOSITION, ATTACHMENT_FILE_NAME + filename);
            out.write(pdf);
            out.flush();
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            if (out != null) {
                out.close();
            }
            response.flushBuffer();
        }
    }

    @GetMapping("/open/{objectId}")
    public void open(@PathVariable(value = "objectId", required = true) String objectId, Locale locale,
                         HttpServletResponse response)
            throws Exception {
        OutputStream out = response.getOutputStream();
        try {
            Map<String,Object> map = dmsProxyFacade.downloadPdf(objectId);
            byte[] pdf = (byte[]) map.get("file");
            String filename =  map.get("filename").toString();
            out = response.getOutputStream();
            response.setContentType(MediaType.APPLICATION_PDF_VALUE);
            //response.addHeader(HttpHeaders.CONTENT_DISPOSITION, ATTACHMENT_FILE_NAME + filename);
            out.write(pdf);
            out.flush();
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            if (out != null) {
                out.close();
            }
            response.flushBuffer();
        }
    }
}
