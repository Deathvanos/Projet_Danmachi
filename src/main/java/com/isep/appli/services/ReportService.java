package com.isep.appli.services;

import com.isep.appli.dbModels.*;
import com.isep.appli.models.FormattedReport;
import com.isep.appli.repositories.ReportRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

@Service
public class ReportService {
    @PersistenceContext
    private EntityManager entityManager;
    private final ReportRepository reportRepository;
    private final PersonnageService personnageService;
    private final FamiliaService familiaService;
    private final MessageService messageService;
    private final UserService userService;

    ReportService(ReportRepository reportRepository, PersonnageService personnageService, FamiliaService familiaService, MessageService messageService, UserService userService) {
        this.reportRepository = reportRepository;
        this.personnageService = personnageService;
        this.familiaService = familiaService;
        this.messageService = messageService;
        this.userService = userService;
    }

    public Report getById(Long id) {
        return reportRepository.findReportById(id);
    }

    public Report save(Report report) {
        return reportRepository.save(report);
    }

    public void deleteReportById(long id) {
        Report report = getById(id);
        reportRepository.delete(report);
    }

    public Page<FormattedReport> getAllFormattedReportSortedByDate(Pageable pageable) {
        Page<Report> reports = reportRepository.findAll(pageable);

        List<FormattedReport> formattedReports = reports.stream()
                .map(this::convertToFormattedReport)
                .collect(Collectors.toList());

        return new PageImpl<>(formattedReports, pageable, reports.getTotalElements());
    }

    public FormattedReport convertToFormattedReport(Report report) {
        Personnage personnageReported = report.getObjectReportedType().equals("PERSONNAGE") ? personnageService.getPersonnageById(report.getObjectReportedId()) : null;
        Familia familiaReported = report.getObjectReportedType().equals("FAMILIA") ? entityManager.find(Familia.class, report.getObjectReportedId()) : null;
        Personnage leaderFamiliaReported = report.getObjectReportedType().equals("FAMILIA") ? familiaService.findLeader(familiaReported) : null;
        Personnage senderMessageReported = report.getObjectReportedType().equals("MESSAGE") ? messageService.findById(report.getObjectReportedId()).getSender() : null;

        return new FormattedReport(
                report.getId(),
                userService.getUserById(report.getUserFromId()),
                report.getObjectReportedType(),
                personnageReported,
                familiaReported,
                leaderFamiliaReported,
                senderMessageReported,
                messageService.formatDate(report.getDate()),
                report.getComment()
        );
    }

    public String getObjectToReport(String typeObjectToReport, Long objectId) {
        String objectToReport;
        switch (typeObjectToReport) {
            case "PERSONNAGE":
                Personnage personnage = personnageService.getPersonnageById(objectId);
                objectToReport = personnage.getFirstName() + " " + personnage.getLastName();
                break;
            case "FAMILIA":
                Familia familia = entityManager.find(Familia.class, objectId);
                Personnage leader = familiaService.findLeader(familia);
                objectToReport = "la familia de " + leader.getFirstName() + " " + leader.getLastName();
                break;
            case "MESSAGE":
                Message message = messageService.findById(objectId);
                Personnage sender = message.getSender();
                objectToReport = "le message de " + sender.getFirstName() + " " + sender.getLastName();
                break;
            default:
                objectToReport = "Organisme inconnu";
        }
        return objectToReport;
    }
}