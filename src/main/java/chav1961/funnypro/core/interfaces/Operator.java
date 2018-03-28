package chav1961.funnypro.core.interfaces;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import chav1961.funnypro.core.interfaces.IFProEntity.EntityType;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Operator {
	String text();
	EntityType[] parameters();
}
