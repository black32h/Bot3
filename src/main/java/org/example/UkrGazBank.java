package org.example;

public class UkrGazBank {

    // Основний метод для розрахунку умов автокредиту в УкрГазБанку
    public static String calculate(double carPrice, double downPaymentPercentage, int loanTerm) {
        // Розраховуємо суму першого внеску на основі відсотка
        double downPayment = carPrice * downPaymentPercentage / 100;
        // Визначаємо суму кредиту (ціна автомобіля мінус перший внесок)
        double loanAmount = carPrice - downPayment;

        // Перевірка: перший внесок не повинен бути більшим або рівним вартості автомобіля
        if (loanAmount <= 0) {
            return "Перший внесок не може бути більшим або дорівнювати вартості автомобіля.";
        }

        // Отримуємо процентну ставку для пільгового періоду (до 24 або 36 місяців)
        double interestRateFirstPeriod = getInterestRateFirstPeriod(downPaymentPercentage, loanTerm);
        // Отримуємо процентну ставку після пільгового періоду (після 24 або 36 місяців)
        double interestRateAfterFirstPeriod = getInterestRateAfterFirstPeriod(downPaymentPercentage, loanTerm);

        // Логіка розрахунку для пільгового періоду
        int firstPeriod = getFirstPeriod(downPaymentPercentage, loanTerm); // Пільговий період залежно від внеску та терміну
        double monthlyPaymentFirstPeriod = calculateMonthlyPayment(loanAmount, interestRateFirstPeriod, firstPeriod);
        double totalPaymentFirstPeriod = monthlyPaymentFirstPeriod * firstPeriod;

        // Остаток суми кредиту після пільгового періоду
        double remainingLoanAmount = loanAmount;
        // Залишковий термін після пільгового періоду
        int remainingTerm = loanTerm - firstPeriod;

        // Логіка розрахунку для залишкового періоду
        double monthlyPaymentAfterFirstPeriod = 0;
        if (remainingTerm > 0) {
            monthlyPaymentAfterFirstPeriod = calculateMonthlyPayment(remainingLoanAmount, interestRateAfterFirstPeriod, remainingTerm);
        }

        // Розраховуємо витрати на КАСКО (6.99% від вартості авто щорічно)
        double kascoAnnual = 0.0699 * carPrice;
        // Загальні витрати на КАСКО за весь термін кредиту
        double totalKasco = kascoAnnual * (loanTerm / 12.0);
        // Щомісячний платіж по КАСКО
        double monthlyKasco = kascoAnnual / 12;
        // Одноразова комісія (відсутня в умовах)
        double commission = 0;

        // Загальна сума всіх виплат за кредитом
        double totalPayment = totalPaymentFirstPeriod + (monthlyPaymentAfterFirstPeriod * Math.max(remainingTerm, 0)) + totalKasco + commission;

        // Формуємо результат з усіма розрахунками
        return String.format("УкрГазБанк:\n" +
                        "Сума кредиту: %.2f грн\n" +
                        "Щомісячний платіж: %.2f грн (перші %d місяців)\n" +
                        "Щомісячний платіж після %d місяців: %.2f грн\n" +
                        "Щомісячний платіж по КАСКО: %.2f грн\n" +
                        "Річна вартість КАСКО: %.2f грн\n" +
                        "Загальна вартість КАСКО за весь термін: %.2f грн\n" +
                        "Одноразова комісія: %.2f грн\n" +
                        "Загальна сума виплат: %.2f грн\n" +
                        "Примітка: КАСКО становить 6.99%% від вартості автомобіля.\n",
                loanAmount, monthlyPaymentFirstPeriod, firstPeriod, firstPeriod, monthlyPaymentAfterFirstPeriod,
                monthlyKasco, kascoAnnual, totalKasco, commission, totalPayment);
    }

    // Метод для отримання процентної ставки для пільгового періоду на основі першого внеску та терміну кредитування
    private static double getInterestRateFirstPeriod(double downPaymentPercentage, int loanTerm) {
        if (loanTerm <= 24) {
            if (downPaymentPercentage >= 50) return 0.01;
            return 0; // Якщо внесок і термін не підходять для пільгового періоду
        } else if (loanTerm <= 36) {
            if (downPaymentPercentage >= 50) return 2.9;
            if (downPaymentPercentage >= 40) return 5.9;
            if (downPaymentPercentage >= 30) return 6.9;
            if (downPaymentPercentage >= 20) return 7.9;
        }
        return 0;
    }

    // Метод для отримання процентної ставки після пільгового періоду (після 24 або 36 місяців)
    private static double getInterestRateAfterFirstPeriod(double downPaymentPercentage, int loanTerm) {
        if (loanTerm > 36) return 29.9; // Фіксована ставка після 36 місяців
        if (loanTerm > 24) return 23.9; // Ставка після 24 місяців
        if (loanTerm > 12 && downPaymentPercentage >= 50) return 18.0;
        return 18.5;
    }

    // Метод для визначення пільгового періоду на основі авансового внеску та терміну
    private static int getFirstPeriod(double downPaymentPercentage, int loanTerm) {
        if (downPaymentPercentage >= 50 && loanTerm > 12) return 12; // 1 рік
        if (loanTerm > 24) return 24; // 2 роки
        return 36; // Максимум 3 роки
    }

    // Метод для розрахунку щомісячного платежу на основі суми кредиту, процентної ставки та терміну
    private static double calculateMonthlyPayment(double loanAmount, double interestRate, int months) {
        if (months <= 0) return 0; // Перевірка на кількість місяців
        double monthlyRate = interestRate / 100 / 12; // Місячна ставка
        // Формула ануїтету для розрахунку щомісячного платежу
        return (loanAmount * monthlyRate) / (1 - Math.pow(1 + monthlyRate, -months));
    }
}
