/**投稿する画像のサイズが大きすぎないかチェックするインターフェイス
 */
package com.example.pictgram.validation.constraints;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;

@Documented
@Constraint(validatedBy = ImageByteValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@ReportAsSingleViolation
public @interface ImageByte {

	String message() default "{com.example.pictgram.validation.constraints.ImageSize.message}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	int max();

}
