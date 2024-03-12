package com.openclassrooms.rentals.dto.response;

import com.openclassrooms.rentals.entity.RentalEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RentalResponse {
	List<RentalEntity> rentals;
}
