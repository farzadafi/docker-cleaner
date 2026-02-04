package ir.farzadafi.report;

import ir.farzadafi.model.enumeration.SmellType;

public record SmellFinding(SmellType type,
                           String message,
                           int line) {
}
