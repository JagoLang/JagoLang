package jago.exception.internal;

public  class InternalException extends RuntimeException {

   public InternalException(String s) {
       super(s + Messages.INTERNAL_ERROR);
   }
}
