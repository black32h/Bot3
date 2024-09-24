package org.example;

public class Ford extends Car {

    // Конструктор класу Ford, який викликає конструктор базового класу Car.
    public Ford(double price, double downPayment, int loanTerm, double interestRate, double commission) {
        super("Ford", price, downPayment, loanTerm, interestRate, commission); // Викликаємо конструктор суперкласу Car
    }
}
