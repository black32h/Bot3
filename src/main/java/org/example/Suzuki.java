package org.example;

public class Suzuki extends Car {
    // Конструктор класу Hyundai, який викликає конструктор базового класу Car.
    public Suzuki(double price, double downPayment, int loanTerm, double interestRate, double commission) {
        super("Suzuki", price, downPayment, loanTerm, interestRate, commission); // Викликаємо конструктор суперкласу Car
    }
}


