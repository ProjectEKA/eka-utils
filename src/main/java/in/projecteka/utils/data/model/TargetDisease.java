package in.projecteka.utils.data.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TargetDisease {
    String code;
    String name;

    public static TargetDisease parse(String details) {
        String[] parts = details.split(",");
        return new TargetDisease(parts[0].trim(), parts[1].trim());
    }

}
