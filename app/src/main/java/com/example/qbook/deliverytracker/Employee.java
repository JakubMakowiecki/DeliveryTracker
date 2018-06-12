package com.example.qbook.deliverytracker;


public class Employee {

    private int employeeId;
    private String employeeName;
    private String employeeSurname;

    public Employee(int employeeId, String employeeName, String employeeSurname) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.employeeSurname = employeeSurname;
    }
    public int getEmployeeId() {
        return employeeId;
    }

    public String getEmployeeIdString() {
        return String.valueOf(employeeId);
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeSurname() {
        return employeeSurname;
    }

    public void setEmployeeSurname(String employeeSurname) {
        this.employeeSurname = employeeSurname;
    }
}
