package com.example.complaintmanagement.service;

import com.example.complaintmanagement.dto.ComplaintResponse;
import com.example.complaintmanagement.entity.Complaint;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Export complaints to CSV using OpenCSV.
     */
    public void exportCsv(PrintWriter writer, List<ComplaintResponse> complaints) {
        try (CSVWriter csvWriter = new CSVWriter(writer)) {
            String[] header = {
                "Complaint ID", "Title", "Category", "Priority",
                "Department", "Status", "Date Submitted", "Citizen Name", "Citizen Email"
            };
            csvWriter.writeNext(header);

            for (ComplaintResponse c : complaints) {
                String[] data = {
                    c.getComplaintNumber() != null ? c.getComplaintNumber() : String.valueOf(c.getId()),
                    c.getTitle(),
                    c.getCategory() != null ? c.getCategory().name() : "",
                    c.getPriority() != null ? c.getPriority().name() : "",
                    c.getDepartmentName() != null ? c.getDepartmentName() : "Unassigned",
                    c.getStatus() != null ? c.getStatus().name() : "",
                    c.getCreatedAt() != null ? c.getCreatedAt().format(DATE_FORMATTER) : "",
                    c.getUserName() != null ? c.getUserName() : "",
                    c.getUserEmail() != null ? c.getUserEmail() : ""
                };
                csvWriter.writeNext(data);
            }
            csvWriter.flush();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export CSV report", e);
        }
    }

    /**
     * Export monthly summary report to PDF using OpenPDF.
     */
    public void exportPdf(OutputStream outputStream, List<ComplaintResponse> complaints) {
        Document document = new Document(PageSize.A4.rotate());
        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Font styles
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLUE);
            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.DARK_GRAY);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);

            // Document Header
            Paragraph title = new Paragraph("ResolverCMS — Monthly Complaint Summary Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph("Generated Total Records: " + complaints.size(), subTitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(15);
            document.add(subtitle);

            // Table setup (8 columns)
            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3f, 5f, 3f, 2.5f, 3.5f, 2.5f, 3.5f, 3.5f});

            String[] headers = {"ID", "Title", "Category", "Priority", "Department", "Status", "Date", "User"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(new Color(41, 128, 185));
                cell.setPadding(6);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            for (ComplaintResponse c : complaints) {
                table.addCell(new Phrase(c.getComplaintNumber(), cellFont));
                table.addCell(new Phrase(c.getTitle(), cellFont));
                table.addCell(new Phrase(c.getCategory() != null ? c.getCategory().name() : "", cellFont));
                table.addCell(new Phrase(c.getPriority() != null ? c.getPriority().name() : "", cellFont));
                table.addCell(new Phrase(c.getDepartmentName() != null ? c.getDepartmentName() : "N/A", cellFont));
                table.addCell(new Phrase(c.getStatus() != null ? c.getStatus().name() : "", cellFont));
                table.addCell(new Phrase(c.getCreatedAt() != null ? c.getCreatedAt().format(DATE_FORMATTER) : "", cellFont));
                table.addCell(new Phrase(c.getUserName() != null ? c.getUserName() : "", cellFont));
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export PDF report", e);
        }
    }

    /**
     * Generate downloadable PDF Complaint Acknowledgement Receipt.
     */
    public void generateReceiptPdf(OutputStream outputStream, ComplaintResponse c) {
        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new Color(30, 60, 114));
            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.DARK_GRAY);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.DARK_GRAY);

            Paragraph header = new Paragraph("ResolverCMS — Official Complaint Receipt", titleFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            Paragraph sub = new Paragraph("Complaint Acknowledgement & Tracking Slip", subTitleFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(20);
            document.add(sub);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1f, 2f});
            table.setSpacingBefore(10);
            table.setSpacingAfter(20);

            addTableRow(table, "Complaint Number:", c.getComplaintNumber(), labelFont, valueFont);
            addTableRow(table, "Submission Date:", c.getCreatedAt() != null ? c.getCreatedAt().format(DATE_FORMATTER) : "", labelFont, valueFont);
            addTableRow(table, "Citizen Name:", c.getUserName(), labelFont, valueFont);
            addTableRow(table, "Citizen Email:", c.getUserEmail(), labelFont, valueFont);
            addTableRow(table, "Category:", c.getCategory() != null ? c.getCategory().name() : "", labelFont, valueFont);
            addTableRow(table, "Priority:", c.getPriority() != null ? c.getPriority().name() : "", labelFont, valueFont);
            addTableRow(table, "Assigned Department:", c.getDepartmentName() != null ? c.getDepartmentName() : "Pending Assignment", labelFont, valueFont);
            addTableRow(table, "Current Status:", c.getStatus() != null ? c.getStatus().name() : "", labelFont, valueFont);
            addTableRow(table, "SLA Deadline:", c.getSlaDeadline() != null ? c.getSlaDeadline().format(DATE_FORMATTER) : "N/A", labelFont, valueFont);
            addTableRow(table, "Issue Title:", c.getTitle(), labelFont, valueFont);
            addTableRow(table, "Issue Description:", c.getDescription(), labelFont, valueFont);

            document.add(table);

            Paragraph footer = new Paragraph("Keep this receipt for your records. You can track your complaint status anytime on our website using Complaint Number: " + c.getComplaintNumber(), FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, Color.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF receipt", e);
        }
    }

    private void addTableRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, labelFont));
        c1.setPadding(8);
        c1.setBackgroundColor(new Color(240, 243, 246));
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(value != null ? value : "-", valueFont));
        c2.setPadding(8);
        table.addCell(c2);
    }
}
