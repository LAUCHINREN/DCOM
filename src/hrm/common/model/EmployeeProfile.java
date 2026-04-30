package hrm.common.model;

import java.io.Serializable;
import java.time.LocalDate;

public class EmployeeProfile implements Serializable {

    private String empId;
    private String firstName;
    private String lastName;
    private String contactNum;
    private String email;
    private LocalDate hireDate;
    private String identificationNum;

    // Emergency contact (from employee_family)
    private boolean isEmergencyContact;
    private String emergencyName;
    private String emergencyContact;
    private String relationship;
    private String gender;
    private LocalDate dob;

    private String famId;
    private String familyLastName;

    public EmployeeProfile() {}

    public String getEmpId() { return empId; }
    public void setEmpId(String empId) { this.empId = empId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getContactNum() { return contactNum; }
    public void setContactNum(String contactNum) { this.contactNum = contactNum; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean getEmergencyContactStatus() { return isEmergencyContact; }
    public void setEmergencyContactStatus(boolean emergencyStatus) { this.isEmergencyContact = emergencyStatus; }

    public String getEmergencyName() { return emergencyName; }
    public void setEmergencyName(String emergencyName) { this.emergencyName = emergencyName; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getFamId() { return famId; }
    public void setFamId(String famId) { this.famId = famId; }

    public String getFamilyLastName() { return familyLastName; }
    public void setFamilyLastName(String familyLastName) { this.familyLastName = familyLastName; }

    public String getIdentificationNum() { return identificationNum; }

    public void setIdentificationNum(String identificationNum) {
        this.identificationNum = identificationNum;
    }
}