package com.example.demo.service;

import com.example.demo.entity.Invoice;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ReceiptPdfService {
    public byte[] generateReceipt(Invoice invoice) {

        try (
                PDDocument document = new PDDocument();
                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream()
        ) {

            PDPage page =
                    new PDPage(PDRectangle.A4);

            document.addPage(page);

            try (
                    PDPageContentStream content =
                            new PDPageContentStream(
                                    document,
                                    page
                            )
            ) {

                PDType1Font titleFont =
                        new PDType1Font(
                                Standard14Fonts.FontName.HELVETICA_BOLD
                        );

                PDType1Font normalFont =
                        new PDType1Font(
                                Standard14Fonts.FontName.HELVETICA
                        );

                float y = 750;

                content.beginText();
                content.setFont(titleFont, 22);
                content.newLineAtOffset(220, y);
                content.showText("PAYMENT RECEIPT");
                content.endText();

                y -= 60;

                content.beginText();
                content.setFont(normalFont, 12);
                content.newLineAtOffset(80, y);

                content.showText(
                        "Invoice ID: #" + invoice.getId()
                );

                content.newLineAtOffset(0, -25);

                content.showText(
                        "Customer ID: #" +
                                invoice.getSubscription()
                                        .getCustomerId()
                );

                content.newLineAtOffset(0, -25);

                content.showText(
                        "Amount: ETB " +
                                String.format(
                                        "%.2f",
                                        invoice.getAmountCents()
                                                / 100.0
                                )
                );

                content.newLineAtOffset(0, -25);

                content.showText(
                        "Status: " +
                                invoice.getStatus()
                );

                if (invoice.getPaidAt() != null) {

                    content.newLineAtOffset(0, -25);

                    content.showText(
                            "Paid At: " +
                                    invoice.getPaidAt()
                    );
                }

                content.newLineAtOffset(0, -50);

                content.setFont(titleFont, 14);

                content.showText(
                        "Thank you for your payment."
                );

                content.endText();
            }

            document.save(outputStream);

            return outputStream.toByteArray();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to generate PDF receipt",
                    e
            );
        }
    }
}
