package org.example;

public class Citroen extends Car {

    // Конструктор класу Citroen, який викликає конструктор базового класу Car.
    public Citroen(double price, double downPayment, int loanTerm, double interestRate, double commission) {
        super("Citroen", price, downPayment, loanTerm, interestRate, commission); // Викликаємо конструктор суперкласу Car
    }
}
