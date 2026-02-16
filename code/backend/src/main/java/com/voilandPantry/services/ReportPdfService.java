package com.voilandPantry.services;

import com.itextpdf.html2pdf.HtmlConverter;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.io.ByteArrayOutputStream;
import java.util.Map;

@Service
public class ReportPdfService {

    private final TemplateEngine templateEngine;

    public ReportPdfService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] generatePdfFromHtml(String templateName, Map<String, Object> data) {
        Context context = new Context();
        context.setVariables(data);
        
        // Render the Thymeleaf template to a String
        String htmlContent = templateEngine.process(templateName, context);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // Convert the HTML String to PDF bytes
        HtmlConverter.convertToPdf(htmlContent, outputStream);
        
        return outputStream.toByteArray();
    }
}
