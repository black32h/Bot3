package org.example;

public interface LoanTerms {
    // Доступні терміни кредиту в місяцях
    int[] terms = {12, 24, 36, 48, 60, 72, 84}; // Додано 84 місяці

    // Метод для отримання доступних термінів
    default int[] getTerms() {
        return terms;
    }

    // Метод для перевірки дійсності терміна
    default boolean isValidTerm(int term) {
        for (int t : terms) {
            if (t == term) {
                return true; // Якщо термін є дійсним, повертаємо true
            }
        }
        return false; // Інакше повертаємо false
    }
}
