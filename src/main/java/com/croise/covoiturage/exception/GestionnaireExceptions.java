package com.croise.covoiturage.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GestionnaireExceptions {

    private static final Logger log = LoggerFactory.getLogger(GestionnaireExceptions.class);

    // --- 400 : erreurs de validation métier (ex: montée après descente) ---
    @ExceptionHandler(ValidationMetierException.class)
    public ProblemDetail gererValidationMetier(ValidationMetierException ex, HttpServletRequest request) {
        log.warn("Erreur de validation métier : {}", ex.getMessage());
        return construireProblemDetail(HttpStatus.BAD_REQUEST, "Erreur de validation", ex.getMessage(), request);
    }

    // --- 400 : erreurs de validation Bean Validation (@NotBlank, @Email, @Min, @Max...) ---
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail gererValidationBean(MethodArgumentNotValidException ex, HttpServletRequest request) {

        String messages = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.warn("Erreur de validation des champs : {}", messages);
        return construireProblemDetail(HttpStatus.BAD_REQUEST, "Erreur de validation", messages, request);
    }

    // --- 403 : accès interdit ---
    @ExceptionHandler(AccesInterditException.class)
    public ProblemDetail gererAccesInterdit(AccesInterditException ex, HttpServletRequest request) {
        log.warn("Accès interdit : {}", ex.getMessage());
        return construireProblemDetail(HttpStatus.FORBIDDEN, "Accès interdit", ex.getMessage(), request);
    }

    // --- 404 : ressource introuvable ---
    @ExceptionHandler(RessourceNotFoundException.class)
    public ProblemDetail gererRessourceIntrouvable(RessourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Ressource introuvable : {}", ex.getMessage());
        return construireProblemDetail(HttpStatus.NOT_FOUND, "Ressource introuvable", ex.getMessage(), request);
    }

    // --- 409 : règle métier violée ---
    @ExceptionHandler(RegleMetierException.class)
    public ProblemDetail gererRegleMetier(RegleMetierException ex, HttpServletRequest request) {
        log.warn("Règle métier violée : {}", ex.getMessage());
        return construireProblemDetail(HttpStatus.CONFLICT, "Règle métier violée", ex.getMessage(), request);
    }

    // --- 500 : toute autre erreur imprévue ---
    @ExceptionHandler(Exception.class)
    public ProblemDetail gererErreurGenerique(Exception ex, HttpServletRequest request) {
        log.error("Erreur interne imprévue", ex);
        return construireProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erreur interne",
                "Une erreur inattendue est survenue",
                request);
    }

    // --- Méthode utilitaire pour construire le ProblemDetail de façon cohérente ---
    private ProblemDetail construireProblemDetail(
            HttpStatus statut, String titre, String message, HttpServletRequest request) {

        ProblemDetail probleme = ProblemDetail.forStatusAndDetail(statut, message);
        probleme.setTitle(titre);
        probleme.setProperty("timestamp", LocalDateTime.now());
        probleme.setProperty("path", request.getRequestURI());

        return probleme;
    }
}