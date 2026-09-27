package com.tigo.shop.user.validation;

/** Reglas compartidas entre los DTO de registro, cambio y restablecimiento de contraseña. */
public final class ValidationRules {

    /** Exige usuario, "@", dominio y un TLD de al menos 2 letras (a diferencia de @Email, que acepta "a@b"). */
    public static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    public static final String EMAIL_MESSAGE = "Formato de email inválido";

    /** Mínimo 8 caracteres, al menos una letra y un número. */
    public static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d).{8,72}$";
    public static final String PASSWORD_MESSAGE =
            "La contraseña debe tener entre 8 y 72 caracteres e incluir letras y números";

    private ValidationRules() {
    }
}
