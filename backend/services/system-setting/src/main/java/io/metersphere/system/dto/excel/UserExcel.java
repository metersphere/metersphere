package io.metersphere.system.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserExcel {
    @NotBlank(message = "{user.name.not_blank}")
    @Size(min = 1, max = 255, message = "{user.name.length_range}")
    @ExcelProperty(index = 0)
    private String name;

    @NotBlank(message = "{user.email.not_blank}")
    @Size(min = 1, max = 64, message = "{user.email.length_range}")
    @Email(message = "{user.email.invalid}")
    @ExcelProperty(index = 1)
    private String email;

    @ExcelProperty(index = 2)
    @Size(min = 1, max = 11, message = "{user.phone.error}")
    @Pattern(regexp = "^[0-9]*[1-9][0-9]*$", message = "{user.phone.error}")
    private String phone;

    @ExcelProperty("workspace")
    private String workspaceName;
}
