package com.example.pictgram.validation.constraints;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ImageNotEmptyValidator implements ConstraintValidator<ImageNotEmpty, MultipartFile> {
	
	@Override
	public void initialize(ImageNotEmpty annotation) {	
	}
	
	@Override
	public boolean isValid(MultipartFile image, ConstraintValidatorContext context) {
		if(image.isEmpty()) {
			return false;
		}
		return true;
	}
}
