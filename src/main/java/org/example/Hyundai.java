package org.example;

public class Hyundai extends Car {

    // Конструктор класу Hyundai, який викликає конструктор базового класу Car.
    public Hyundai(double price, double downPayment, int loanTerm, double interestRate, double commission) {
        super("Hyundai", price, downPayment, loanTerm, interestRate, commission); // Викликаємо конструктор суперкласу Car
    }
}
