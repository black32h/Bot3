package org.example;

public class Kia extends Car {

    // Конструктор класу Kia, який викликає конструктор базового класу Car.
    public Kia(double price, double downPayment, int loanTerm, double interestRate, double commission) {
        super("Kia", price, downPayment, loanTerm, interestRate, commission); // Викликаємо конструктор суперкласу Car
    }
}
