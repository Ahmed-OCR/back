package com.openclassrooms.rentals.service.impl;

import com.openclassrooms.rentals.dto.request.RentalRequest;
import com.openclassrooms.rentals.dto.response.MessageResponse;
import com.openclassrooms.rentals.dto.response.RentalResponse;
import com.openclassrooms.rentals.dto.response.RentalsResponse;
import com.openclassrooms.rentals.entity.RentalEntity;
import com.openclassrooms.rentals.exception.RentalNotFoundException;
import com.openclassrooms.rentals.mapper.RentalMapper;
import com.openclassrooms.rentals.repository.RentalRepository;
import com.openclassrooms.rentals.service.RentalService;
import com.openclassrooms.rentals.util.MessageUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class RentalServiceImpl implements RentalService {

	private final RentalRepository rentalRepository;

	@Override
	public RentalsResponse findAllRentals() {
		List<RentalEntity> rentalsEntity = this.rentalRepository.findAll();
		List<RentalResponse> rentalsResponse = RentalMapper.toRentalResponse(rentalsEntity);
		return new RentalsResponse(rentalsResponse);
	}

	@Override
	public Optional<RentalEntity> findById(int id) {
		Optional<RentalEntity> rental = this.rentalRepository.findById(id);
		if (rental.isEmpty()) {
			throw new RentalNotFoundException("Location introuvable avec l'ID : " + id);
		}
		return rental;
	}

	@Override
	public ResponseEntity<MessageResponse> createRental(int id, MultipartFile picture, RentalRequest request, HttpServletRequest httpServletRequest) {
		if (!this.validateRentaRequest(request, true)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}

		RentalEntity rental = RentalMapper.mapToRental(id, picture, request);
		try {
			String imageUrl = saveImage(picture, httpServletRequest);
			rental.setPicture(imageUrl);
			this.rentalRepository.save(rental);
			return ResponseEntity.status(HttpStatus.CREATED).body(MessageUtil.returnMessage("Rental created !"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
	}

	@Override
	public ResponseEntity<MessageResponse> updateRental(RentalRequest request, int id) {
		if (!this.validateRentaRequest(request, false)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
		Optional<RentalEntity> optionalRental = rentalRepository.findById(id);
		if (optionalRental.isPresent()) {
			RentalEntity existingRental = optionalRental.get();
			// Copie toutes les propriétés de la location vers la location existante
			BeanUtils.copyProperties(request, existingRental, "id");
			this.rentalRepository.save(existingRental);
			return ResponseEntity.status(HttpStatus.CREATED).body(MessageUtil.returnMessage("Rental updated !"));
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
	}

	public boolean validateRentaRequest(RentalRequest rentalRequest, boolean validatePicture) {
		if (rentalRequest == null
				|| rentalRequest.getName() == null || rentalRequest.getName().isEmpty()
				|| rentalRequest.getSurface() <= 0
				|| rentalRequest.getPrice() <= 0
				|| (validatePicture && (rentalRequest.getPicture() == null || rentalRequest.getPicture().isEmpty()))
				|| rentalRequest.getDescription() == null || rentalRequest.getDescription().isEmpty()) {
			return false;
		}
		return true;
	}

	public String saveImage(MultipartFile file, HttpServletRequest httpServletRequest) {
//		String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
		String fileName = file.getOriginalFilename();
		String uploadDir = "/images/";

		File directory = new File("." + uploadDir); // On part du repertoire courant, d'ou le "." dans le chemin
		if (!directory.exists()) {
			directory.mkdirs();
		}
		File uploadedFile = new File(directory, fileName);

		try {
			FileUtils.copyInputStreamToFile(file.getInputStream(), uploadedFile);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		// Construction du chemin de l'image
		return  httpServletRequest.getScheme() + "://" + httpServletRequest.getServerName() + ":" +
				httpServletRequest.getServerPort() + httpServletRequest.getContextPath() +
				httpServletRequest.getServletPath() + uploadDir + fileName;
	}
}

