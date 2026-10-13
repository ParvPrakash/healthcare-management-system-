package com.healthcare.util;

import com.healthcare.model.Appointment;
import com.healthcare.model.MedicalRecord;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.io.ByteArrayOutputStream;

/**
 * PDF Report Generator powered by OpenPDF.
 * Generates official clinical summaries and prescriptions.
 */
public final class PdfReportGenerator {
    private static final Logger logger = LoggerFactory.getLogger(PdfReportGenerator.class);

    private PdfReportGenerator() {}

    /**
     * Generates a PDF summary for an appointment and optional clinical record.
     */
    public static byte[] generateAppointmentPdf(Appointment appointment, MedicalRecord record, String clinicName) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 54, 36);

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // Colors
            Color peachDark = new Color(165, 83, 58);
            Color peachLight = new Color(255, 245, 238);
            Color textDark = new Color(58, 43, 37);

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, peachDark);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, textDark);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10, textDark);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, textDark);

            // Clinic Header
            Paragraph header = new Paragraph(clinicName != null ? clinicName : "CareFirst Healthcare Clinic", titleFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            Paragraph subHeader = new Paragraph("Official Appointment & Clinical Consultation Summary", FontFactory.getFont(FontFactory.HELVETICA, 11, Color.GRAY));
            subHeader.setAlignment(Element.ALIGN_CENTER);
            subHeader.setSpacingAfter(20);
            document.add(subHeader);

            // Appointment Info Table
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingBefore(10);
            infoTable.setSpacingAfter(15);

            addCell(infoTable, "Appointment ID:", boldFont, peachLight);
            addCell(infoTable, "APP-" + appointment.getId(), bodyFont, peachLight);

            addCell(infoTable, "Date & Time:", boldFont, Color.WHITE);
            addCell(infoTable, appointment.getAppointmentDate() + " (" + appointment.getFormattedTime() + ")", bodyFont, Color.WHITE);

            addCell(infoTable, "Status:", boldFont, peachLight);
            addCell(infoTable, appointment.getStatus().name(), bodyFont, peachLight);

            addCell(infoTable, "Patient Name:", boldFont, Color.WHITE);
            addCell(infoTable, appointment.getPatientName() + (appointment.getPatientBloodGroup() != null ? " [Blood Group: " + appointment.getPatientBloodGroup() + "]" : ""), bodyFont, Color.WHITE);

            addCell(infoTable, "Attending Doctor:", boldFont, peachLight);
            addCell(infoTable, "Dr. " + appointment.getDoctorName() + " (" + appointment.getDoctorSpecialization() + ")", bodyFont, peachLight);

            addCell(infoTable, "Consultation Reason:", boldFont, Color.WHITE);
            addCell(infoTable, appointment.getReason() != null ? appointment.getReason() : "General Consultation", bodyFont, Color.WHITE);

            document.add(infoTable);

            // Medical Record Section (if available)
            if (record != null) {
                Paragraph recordTitle = new Paragraph("Medical Assessment & Diagnosis", headerFont);
                recordTitle.setSpacingBefore(15);
                recordTitle.setSpacingAfter(8);
                document.add(recordTitle);

                PdfPTable recordTable = new PdfPTable(2);
                recordTable.setWidthPercentage(100);

                addCell(recordTable, "Diagnosis:", boldFont, peachLight);
                addCell(recordTable, record.getDiagnosis() != null ? record.getDiagnosis() : "None", bodyFont, peachLight);

                addCell(recordTable, "Blood Pressure:", boldFont, Color.WHITE);
                addCell(recordTable, record.getVitalsBloodPressure() != null ? record.getVitalsBloodPressure() : "N/A", bodyFont, Color.WHITE);

                addCell(recordTable, "Heart Rate:", boldFont, peachLight);
                addCell(recordTable, record.getVitalsHeartRate() != null ? record.getVitalsHeartRate() + " bpm" : "N/A", bodyFont, peachLight);

                addCell(recordTable, "Temperature:", boldFont, Color.WHITE);
                addCell(recordTable, record.getVitalsTemperature() != null ? record.getVitalsTemperature() + " °F" : "N/A", bodyFont, Color.WHITE);

                addCell(recordTable, "Prescription:", boldFont, peachLight);
                addCell(recordTable, record.getPrescription() != null ? record.getPrescription() : "None prescribed", bodyFont, peachLight);

                addCell(recordTable, "Doctor Clinical Notes:", boldFont, Color.WHITE);
                addCell(recordTable, record.getNotes() != null ? record.getNotes() : "N/A", bodyFont, Color.WHITE);

                document.add(recordTable);
            }

            // Doctor signature & disclaimer
            Paragraph footer = new Paragraph("\n\n\nAttending Physician Signature: ___________________________\n\n* This is an official digital health summary generated by CareFirst Healthcare System.",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY));
            footer.setSpacingBefore(30);
            document.add(footer);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            logger.error("Failed to generate PDF document", e);
            return new byte[0];
        }
    }

    private static void addCell(PdfPTable table, String text, Font font, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(6);
        cell.setBorderColor(new Color(243, 227, 216));
        table.addCell(cell);
    }
}
