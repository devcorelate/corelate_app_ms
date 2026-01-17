package com.corelate.app.dto;

import com.corelate.app.dto.BaseEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomerDto extends BaseEntity {
    @NotEmpty(message = "Name can not be empty")
    @Size(min = 2, max = 30, message = "The length of the first name should be between 5 and 30")
    private String first_name;

    @NotEmpty(message = "Name can not be empty")
    @Size(min = 2, max = 30, message = "The length of the last name should be between 5 and 30")
    private String last_name;

    @NotEmpty(message = "Name can not be empty")
    @Size(min = 2, max = 30, message = "The length of the middle name should be between 5 and 30")
    private String middle_name;

    @NotEmpty(message = "Email address can not be empty")
    @Email(message = "Email should be valid")
    private String email;

    @NotEmpty(message = "Mobile Number can not be empty")
    @Pattern(regexp = "(^$|[0-9]{11})", message = "Mobile number must be 11 digits starts with 0")
    private String mobileNumber;

    @NotEmpty(message = "Country can not be empty")
    @Size(min = 3, max = 20, message = "The length of the country should be between 3 and 20")
    private String country;

    @NotEmpty(message = "Province can not be empty")
    @Size(min = 3, max = 20, message = "The length of the province should be between 3 and 20")
    private String province;

    @NotEmpty(message = "Province can not be empty")
    @Size(min = 3, max = 20, message = "The length of the province should be between 3 and 20")
    private String provinceName;

    @NotEmpty(message = "City can not be empty")
    @Size(min = 3, max = 20, message = "The length of the city should be between 3 and 20")
    private String city;

    @NotEmpty(message = "Zipcode can not be empty")
    @Pattern(regexp = "(^$|\\d{4})", message = "Zipcode should be a 4-digit number or empty")
    private String zipcode;

    @NotEmpty(message = "Name can not be empty")
    @Size(min = 5, max = 100, message = "The length of the address should be between 5 and 100")
    private String address1;

    private String address2;

    private AccountDto accountDto;
}
