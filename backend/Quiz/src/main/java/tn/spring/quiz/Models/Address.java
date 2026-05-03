package tn.spring.quiz.Models;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    private String Pays;
    private String Region;
    private String Ville;
    private String PostalCode;
    private String Rue;
}
